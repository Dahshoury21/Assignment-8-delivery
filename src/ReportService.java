import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Part D reports — declarative Stream pipelines only.
 */
public class ReportService {

	/** 1. Total revenue for a given date range (delivered orders). */
	public BigDecimal totalRevenue(List<Order> orders, LocalDate from, LocalDate to) {
		return orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.filter(o -> {
					LocalDate d = o.getPlacedAt().toLocalDate();
					return !d.isBefore(from) && !d.isAfter(to);
				})
				.map(Order::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** 2. Top five restaurants by revenue for a given month. */
	public List<Map.Entry<Restaurant, BigDecimal>> topFiveRestaurantsByRevenue(
			List<Order> orders, YearMonth month) {
		return orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.filter(o -> YearMonth.from(o.getPlacedAt()).equals(month))
				.collect(Collectors.groupingBy(Order::getRestaurant,
						Collectors.mapping(Order::getTotal,
								Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
				.entrySet().stream()
				.sorted(Map.Entry.<Restaurant, BigDecimal>comparingByValue().reversed())
				.limit(5)
				.collect(Collectors.toList());
	}

	/** 3. Average order value per district. */
	public Map<String, BigDecimal> averageOrderValuePerDistrict(List<Order> orders) {
		return orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.collect(Collectors.groupingBy(
						o -> o.getDeliveryAddress().district(),
						Collectors.collectingAndThen(
								Collectors.mapping(Order::getTotal, Collectors.toList()),
								list -> {
									BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
									return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
								})));
	}

	/** 4. Restaurants with rating > 4.5 and at least 20 completed orders. */
	public List<Restaurant> highRatedBusyRestaurants(List<Order> orders, List<Restaurant> restaurants) {
		Map<Restaurant, Long> completed = orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.collect(Collectors.groupingBy(Order::getRestaurant, Collectors.counting()));
		return restaurants.stream()
				.filter(r -> r.getRating() > 4.5)
				.filter(r -> completed.getOrDefault(r, 0L) >= 20)
				.sorted(Comparator.comparingDouble(Restaurant::getRating).reversed())
				.collect(Collectors.toList());
	}

	/** 5. Count of orders grouped by current status. */
	public Map<OrderStatus, Long> orderCountByStatus(List<Order> orders) {
		return orders.stream()
				.collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
	}

	/** 6. Each rider’s completed deliveries and average duration, sorted by deliveries desc. */
	public List<RiderStats> riderDeliveryStats(List<Rider> riders) {
		return riders.stream()
				.map(r -> new RiderStats(r.getName(), r.getCompletedDeliveries(), r.averageDeliveryMinutes()))
				.sorted(Comparator.comparingInt(RiderStats::completedDeliveries).reversed())
				.collect(Collectors.toList());
	}

	public record RiderStats(String riderName, int completedDeliveries, double avgMinutes) {}

	/**
	 * 7. Most frequently ordered menu item.
	 * Empty Optional when no orders exist — absence expressed honestly.
	 */
	public Optional<Map.Entry<String, Long>> mostFrequentlyOrderedItem(List<Order> orders) {
		if (orders == null || orders.isEmpty()) {
			return Optional.empty();
		}
		return orders.stream()
				.flatMap(o -> o.getLineItems().stream())
				.collect(Collectors.groupingBy(li -> li.menuItem().getName(), Collectors.counting()))
				.entrySet().stream()
				.max(Map.Entry.comparingByValue());
	}

	/** 8. Customer order history newest first + lifetime spend. */
	public record CustomerHistory(List<Order> ordersNewestFirst, BigDecimal lifetimeSpend) {}

	public CustomerHistory customerHistory(List<Order> allOrders, Customer customer) {
		List<Order> history = allOrders.stream()
				.filter(o -> o.getCustomer().equals(customer))
				.sorted(Comparator.comparing(Order::getPlacedAt).reversed())
				.collect(Collectors.toList());
		BigDecimal spent = history.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.isPaid())
				.map(Order::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new CustomerHistory(history, spent);
	}

	/** 9. Peak ordering hour of the day. */
	public Optional<Integer> peakOrderingHour(List<Order> orders) {
		return orders.stream()
				.collect(Collectors.groupingBy(o -> o.getPlacedAt().getHour(), Collectors.counting()))
				.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey);
	}

	/** 10. Customers who have not ordered in the last thirty days. */
	public List<Customer> inactiveCustomers(List<Customer> customers, List<Order> orders, LocalDateTime now) {
		LocalDateTime cutoff = now.minusDays(30);
		Map<Customer, Optional<LocalDateTime>> lastOrder = customers.stream()
				.collect(Collectors.toMap(
						c -> c,
						c -> orders.stream()
								.filter(o -> o.getCustomer().equals(c))
								.map(Order::getPlacedAt)
								.max(Comparator.naturalOrder()),
						(a, b) -> a,
						LinkedHashMap::new));
		return lastOrder.entrySet().stream()
				.filter(e -> e.getValue().isEmpty() || e.getValue().get().isBefore(cutoff))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}
}
