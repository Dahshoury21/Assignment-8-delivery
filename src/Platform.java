import exception.MasrDeliveryException;
import exception.RiderBusyException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Central platform store.
 * Lookups by id are O(1) via HashMap (Part C.1). Collections returned are unmodifiable (C.8).
 */
public class Platform {
	private final Map<String, Restaurant> restaurants = new LinkedHashMap<>();
	private final Map<String, Customer> customers = new LinkedHashMap<>();
	private final Map<String, Rider> riders = new LinkedHashMap<>();
	private final Map<String, Order> orders = new LinkedHashMap<>();
	private final Map<String, PromotionStrategy> promotions = new HashMap<>();
	private final OrderDispatchQueue dispatchQueue = new OrderDispatchQueue();
	private final AtomicInteger orderSeq = new AtomicInteger(1000);

	private final NotificationService notificationService = new NotificationService();
	private final RiderDashboardObserver riderDashboard = new RiderDashboardObserver();
	private final AuditLogObserver auditLog = new AuditLogObserver();
	private final StatisticsObserver statistics = new StatisticsObserver();
	private final ReportService reports = new ReportService();

	public String nextOrderId() {
		return "ORD-" + orderSeq.incrementAndGet();
	}

	// ---------- registration ----------

	public void addRestaurant(Restaurant r) {
		if (restaurants.containsKey(r.getId())) {
			throw new IllegalArgumentException("Duplicate restaurant id: " + r.getId());
		}
		restaurants.put(r.getId(), r);
	}

	public void removeRestaurant(String id) {
		restaurants.remove(id);
	}

	public void addCustomer(Customer c) {
		if (customers.containsKey(c.getId())) {
			throw new IllegalArgumentException("Duplicate customer id: " + c.getId());
		}
		customers.put(c.getId(), c);
	}

	public void addRider(Rider r) {
		if (riders.containsKey(r.getId())) {
			throw new IllegalArgumentException("Duplicate rider id: " + r.getId());
		}
		riders.put(r.getId(), r);
	}

	public void addPromotion(PromotionStrategy promo) {
		promotions.put(promo.getCode().toUpperCase(Locale.ROOT), promo);
	}

	public Optional<PromotionStrategy> findPromotion(String code) {
		if (code == null) return Optional.empty();
		return Optional.ofNullable(promotions.get(code.trim().toUpperCase(Locale.ROOT)));
	}

	// ---------- lookups ----------

	public Optional<Restaurant> findRestaurant(String id) {
		return Optional.ofNullable(restaurants.get(id));
	}

	public Optional<Customer> findCustomer(String id) {
		return Optional.ofNullable(customers.get(id));
	}

	public Optional<Rider> findRider(String id) {
		return Optional.ofNullable(riders.get(id));
	}

	public Optional<Order> findOrder(String id) {
		return Optional.ofNullable(orders.get(id));
	}

	public Collection<Restaurant> allRestaurants() {
		return Collections.unmodifiableCollection(restaurants.values());
	}

	public Collection<Customer> allCustomers() {
		return Collections.unmodifiableCollection(customers.values());
	}

	public Collection<Rider> allRiders() {
		return Collections.unmodifiableCollection(riders.values());
	}

	public Collection<Order> allOrders() {
		return Collections.unmodifiableCollection(orders.values());
	}

	public Collection<PromotionStrategy> allPromotions() {
		return Collections.unmodifiableCollection(promotions.values());
	}

	/** Distinct cuisine categories, no duplicates (Part C.3). */
	public Set<String> distinctCuisines() {
		Set<String> set = new HashSet<>();
		for (Restaurant r : restaurants.values()) {
			set.addAll(r.getCuisines());
		}
		return Collections.unmodifiableSet(set);
	}

	/** Sorted by rating desc, then name asc (Part C.4). */
	public List<Restaurant> restaurantsByRating() {
		return restaurants.values().stream()
				.sorted(Comparator.comparingDouble(Restaurant::getRating).reversed()
						.thenComparing(Restaurant::getName))
				.collect(Collectors.toList());
	}

	public List<Restaurant> searchRestaurants(RestaurantCriterion criterion) {
		List<Restaurant> list = RestaurantCriterion.apply(new ArrayList<>(restaurants.values()), criterion);
		list.sort(Comparator.comparingDouble(Restaurant::getRating).reversed()
				.thenComparing(Restaurant::getName));
		return list;
	}

	// ---------- order lifecycle ----------

	public Order placeOrder(OrderBuilder builder) throws MasrDeliveryException {
		Order order = builder.build();
		try {
			int km = DistanceService.distanceKm(
					order.getRestaurant().getDistrict(),
					order.getDeliveryAddress().district());
			PricingCalculator.computeOrderPricing(order, km);
		} catch (MasrDeliveryException | RuntimeException ex) {
			restoreStock(order);
			throw ex;
		}
		wireObservers(order);
		orders.put(order.getId(), order);
		statistics.registerPlaced();
		return order;
	}

	private void wireObservers(Order order) {
		order.addObserver(notificationService);
		order.addObserver(riderDashboard);
		order.addObserver(auditLog);
		order.addObserver(statistics);
	}

	public void acceptOrder(Order order) {
		order.transitionTo(OrderStatus.ACCEPTED);
	}

	public void rejectOrder(Order order) {
		order.cancel();
		refundIfPaid(order);
		restoreStock(order);
	}

	public void markPreparing(Order order) {
		order.transitionTo(OrderStatus.PREPARING);
	}

	public void markReady(Order order) {
		order.transitionTo(OrderStatus.READY);
		dispatchQueue.addReadyOrder(order);
	}

	public Order pollDispatchQueue() {
		return dispatchQueue.dispatchNextOrder();
	}

	public void assignRider(Order order, Rider rider) throws RiderBusyException {
		if (order.getStatus() != OrderStatus.READY) {
			throw new IllegalStateException("Only READY orders can be assigned (current: " + order.getStatus() + ")");
		}
		rider.assignOrder(order);
		order.setAssignedRider(rider);
	}

	public void payFromWallet(Order order) throws exception.InsufficientBalanceException {
		if (order.isPaid()) {
			throw new IllegalStateException("Order already paid");
		}
		order.getCustomer().deductWallet(order.getTotal());
		order.markPaid();
	}

	public void cancelByCustomer(Order order) {
		order.cancel();
		refundIfPaid(order);
		restoreStock(order);
	}

	private void refundIfPaid(Order order) {
		if (order.isPaid()) {
			order.getCustomer().refundWallet(order.getTotal());
		}
	}

	private void restoreStock(Order order) {
		for (LineItem li : order.getLineItems()) {
			li.menuItem().restoreStock(li.stockUnits());
		}
	}

	public OrderDispatchQueue getDispatchQueue() { return dispatchQueue; }
	public AuditLogObserver getAuditLog() { return auditLog; }
	public StatisticsObserver getStatistics() { return statistics; }
	public ReportService getReports() { return reports; }

	public List<Order> ordersForRestaurantToday(Restaurant restaurant) {
		LocalDate today = LocalDate.now();
		return orders.values().stream()
				.filter(o -> o.getRestaurant().equals(restaurant))
				.filter(o -> o.getPlacedAt().toLocalDate().equals(today))
				.sorted(Comparator.comparing(Order::getPlacedAt).reversed())
				.collect(Collectors.toList());
	}

	public BigDecimal restaurantRevenueToday(Restaurant restaurant) {
		return ordersForRestaurantToday(restaurant).stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.isPaid())
				.map(Order::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public List<Order> customerHistory(Customer customer) {
		return orders.values().stream()
				.filter(o -> o.getCustomer().equals(customer))
				.sorted(Comparator.comparing(Order::getPlacedAt).reversed())
				.collect(Collectors.toList());
	}
}
