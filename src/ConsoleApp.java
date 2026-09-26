import exception.InsufficientBalanceException;
import exception.MasrDeliveryException;
import exception.RiderBusyException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
	private final Platform platform;
	private final Scanner in = new Scanner(System.in);

	public ConsoleApp(Platform platform) {
		this.platform = platform;
	}

	public void run() {
		boolean running = true;
		while (running) {
			printMainMenu();
			String choice = readLine("Choose an option: ");
			switch (choice) {
				case "1" -> customerArea();
				case "2" -> restaurantArea();
				case "3" -> riderArea();
				case "4" -> adminArea();
				case "0" -> {
					System.out.println("Goodbye.");
					running = false;
				}
				default -> System.out.println("Invalid option. Please try again.");
			}
		}
	}

	private void printMainMenu() {
		System.out.println();
		System.out.println("============================================");
		System.out.println("MASR DELIVERY — Main Menu");
		System.out.println("============================================");
		System.out.println("1. Customer");
		System.out.println("2. Restaurant");
		System.out.println("3. Rider");
		System.out.println("4. Admin & Reports");
		System.out.println("0. Exit");
		System.out.println("============================================");
	}

	// ===================== Customer =====================

	private void customerArea() {
		Optional<Customer> opt = pickCustomer();
		if (opt.isEmpty()) return;
		Customer customer = opt.get();
		boolean back = false;
		while (!back) {
			System.out.println("\n--- Customer: " + customer.getName() + " [" + customer.getLoyaltyTier() + "] ---");
			System.out.println("1. Browse restaurants");
			System.out.println("2. Search");
			System.out.println("3. View menu");
			System.out.println("4. Place order");
			System.out.println("5. Pay from wallet");
			System.out.println("6. Track order");
			System.out.println("7. Cancel order");
			System.out.println("8. Order history");
			System.out.println("9. Recent searches");
			System.out.println("0. Back");
			switch (readLine("Choose: ")) {
				case "1" -> browseRestaurants(customer);
				case "2" -> searchRestaurants(customer);
				case "3" -> viewMenu();
				case "4" -> placeOrder(customer);
				case "5" -> payFromWallet(customer);
				case "6" -> trackOrder();
				case "7" -> cancelOrder(customer);
				case "8" -> orderHistory(customer);
				case "9" -> {
					List<String> recent = customer.getRecentSearches();
					if (recent.isEmpty()) System.out.println("No recent searches.");
					else recent.forEach(q -> System.out.println("  • " + q));
				}
				case "0" -> back = true;
				default -> System.out.println("Invalid option. Please try again.");
			}
		}
	}

	private void browseRestaurants(Customer customer) {
		RestaurantCriterion criterion = RestaurantCriterion.openOnly();
		String district = readLine("Filter district (Enter to skip): ");
		if (!district.isBlank()) criterion = criterion.and(RestaurantCriterion.district(district));
		String cuisine = readLine("Filter cuisine (Enter to skip): ");
		if (!cuisine.isBlank()) criterion = criterion.and(RestaurantCriterion.cuisine(cuisine));
		String minRating = readLine("Minimum rating (Enter to skip): ");
		if (!minRating.isBlank()) {
			try {
				criterion = criterion.and(RestaurantCriterion.minRating(Double.parseDouble(minRating)));
			} catch (NumberFormatException e) {
				System.out.println("Ignoring invalid rating.");
			}
		}
		String maxPrice = readLine("Max item price ceiling (Enter to skip): ");
		if (!maxPrice.isBlank()) {
			try {
				criterion = criterion.and(RestaurantCriterion.maxMenuPrice(new BigDecimal(maxPrice)));
			} catch (NumberFormatException e) {
				System.out.println("Ignoring invalid price.");
			}
		}
		List<Restaurant> list = platform.searchRestaurants(criterion);
		printRestaurantList(list);
	}

	private void searchRestaurants(Customer customer) {
		String q = readLine("Search text: ");
		if (q.isBlank()) {
			System.out.println("Empty search.");
			return;
		}
		customer.addSearchQuery(q);
		List<Restaurant> list = platform.searchRestaurants(
				RestaurantCriterion.openOnly().and(RestaurantCriterion.nameOrCuisineContains(q)));
		if (list.isEmpty()) {
			System.out.println("Nothing found for \"" + q + "\".");
		} else {
			printRestaurantList(list);
		}
	}

	private void printRestaurantList(List<Restaurant> list) {
		if (list.isEmpty()) {
			System.out.println("No restaurants match.");
			return;
		}
		int i = 1;
		for (Restaurant r : list) {
			System.out.println(i++ + ". " + r);
		}
	}

	private void viewMenu() {
		Optional<Restaurant> r = pickRestaurantFromAll();
		if (r.isEmpty()) return;
		printMenu(r.get());
	}

	private void printMenu(Restaurant restaurant) {
		System.out.println("\nMenu — " + restaurant.getName() + " (insertion order):");
		Map<String, List<MenuItem>> byCat = new java.util.LinkedHashMap<>();
		for (MenuItem item : restaurant.getMenu()) {
			byCat.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
		}
		int n = 1;
		List<MenuItem> indexed = new ArrayList<>();
		for (var e : byCat.entrySet()) {
			System.out.println("[" + e.getKey() + "]");
			for (MenuItem item : e.getValue()) {
				System.out.println("  " + n + ". " + item + " (id=" + item.getId() + ")");
				indexed.add(item);
				n++;
			}
		}
	}

	private void placeOrder(Customer customer) {
		Optional<Restaurant> rOpt = pickRestaurantFromAll();
		if (rOpt.isEmpty()) return;
		Restaurant restaurant = rOpt.get();
		if (!restaurant.isOpen()) {
			System.out.println("Restaurant is closed.");
			return;
		}
		printMenu(restaurant);
		List<Address> addresses = customer.getAddresses();
		if (addresses.isEmpty()) {
			System.out.println("Customer has no saved addresses.");
			return;
		}
		System.out.println("Addresses:");
		for (int i = 0; i < addresses.size(); i++) {
			System.out.println((i + 1) + ". " + addresses.get(i));
		}
		int addrIdx = readInt("Address number: ", 1, addresses.size()) - 1;
		Address address = addresses.get(addrIdx);

		OrderBuilder builder = new OrderBuilder()
				.id(platform.nextOrderId())
				.customer(customer)
				.restaurant(restaurant)
				.deliveryAddress(address);

		String notes = readLine("Delivery notes (optional): ");
		if (!notes.isBlank()) builder.deliveryNotes(notes);

		boolean adding = true;
		while (adding) {
			String itemId = readLine("Menu item id (or DONE): ");
			if (itemId.equalsIgnoreCase("DONE")) {
				adding = false;
				continue;
			}
			Optional<MenuItem> item = restaurant.findMenuItem(itemId);
			if (item.isEmpty()) {
				System.out.println("Unknown item id.");
				continue;
			}
			double qty = readDouble(item.get() instanceof WeightedItem
					? "Weight in kg: " : "Quantity: ");
			try {
				builder.addLineItem(item.get(), qty);
				System.out.println("Added.");
			} catch (MasrDeliveryException ex) {
				System.out.println("Error: " + ex.getMessage());
			}
		}

		String promoCode = readLine("Promo code (optional): ");
		if (!promoCode.isBlank()) {
			Optional<PromotionStrategy> promo = platform.findPromotion(promoCode);
			if (promo.isEmpty()) {
				System.out.println("Unknown promo — order cancelled atomically.");
				return;
			}
			builder.promotion(promo.get());
		}

		try {
			Order order = platform.placeOrder(builder);
			System.out.println("\nOrder placed successfully.\n" + order.priceBreakdown());
		} catch (MasrDeliveryException | IllegalStateException ex) {
			System.out.println("Order rejected: " + ex.getMessage());
		}
	}

	private void payFromWallet(Customer customer) {
		String id = readLine("Order reference: ");
		Optional<Order> opt = platform.findOrder(id);
		if (opt.isEmpty()) {
			System.out.println("Order not found.");
			return;
		}
		Order order = opt.get();
		if (!order.getCustomer().equals(customer)) {
			System.out.println("This order does not belong to you.");
			return;
		}
		try {
			platform.payFromWallet(order);
			System.out.println("Paid. New wallet balance: " + Money.format(customer.getWalletBalance()));
		} catch (InsufficientBalanceException ex) {
			System.out.println(ex.getMessage());
		} catch (IllegalStateException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void trackOrder() {
		String id = readLine("Order reference: ");
		Optional<Order> opt = platform.findOrder(id);
		if (opt.isEmpty()) {
			System.out.println("Order not found.");
			return;
		}
		Order order = opt.get();
		long mins = order.elapsedSincePlacement().toMinutes();
		System.out.println("Status: " + order.getStatus() + " | Elapsed since placement: " + mins + " min");
	}

	private void cancelOrder(Customer customer) {
		String id = readLine("Order reference: ");
		Optional<Order> opt = platform.findOrder(id);
		if (opt.isEmpty()) {
			System.out.println("Order not found.");
			return;
		}
		Order order = opt.get();
		if (!order.getCustomer().equals(customer)) {
			System.out.println("This order does not belong to you.");
			return;
		}
		try {
			platform.cancelByCustomer(order);
			System.out.println("Order cancelled"
					+ (order.isPaid() ? " and refunded to wallet." : "."));
		} catch (RuntimeException ex) {
			System.out.println("Cancellation not permitted: " + ex.getMessage());
		}
	}

	private void orderHistory(Customer customer) {
		var hist = platform.getReports().customerHistory(new ArrayList<>(platform.allOrders()), customer);
		if (hist.ordersNewestFirst().isEmpty()) {
			System.out.println("No orders yet.");
			return;
		}
		for (Order o : hist.ordersNewestFirst()) {
			System.out.println(o.getId() + " | " + o.getPlacedAt() + " | " + o.getStatus()
					+ " | " + Money.format(o.getTotal()));
		}
		System.out.println("Lifetime total spent: " + Money.format(hist.lifetimeSpend()));
	}

	// ===================== Restaurant =====================

	private void restaurantArea() {
		Optional<Restaurant> opt = pickRestaurantFromAll();
		if (opt.isEmpty()) return;
		Restaurant restaurant = opt.get();
		boolean back = false;
		while (!back) {
			System.out.println("\n--- Restaurant: " + restaurant.getName() + " ---");
			System.out.println("1. Accept pending order");
			System.out.println("2. Reject pending order");
			System.out.println("3. Mark preparing");
			System.out.println("4. Mark ready");
			System.out.println("5. Toggle item availability");
			System.out.println("6. Add menu item");
			System.out.println("7. Remove menu item");
			System.out.println("8. Adjust daily stock");
			System.out.println("9. Today's orders & revenue");
			System.out.println("10. Open/Close restaurant");
			System.out.println("0. Back");
			switch (readLine("Choose: ")) {
				case "1" -> restaurantTransition(restaurant, OrderStatus.PLACED, "accept");
				case "2" -> restaurantReject(restaurant);
				case "3" -> restaurantTransition(restaurant, OrderStatus.ACCEPTED, "preparing");
				case "4" -> restaurantMarkReady(restaurant);
				case "5" -> toggleAvailability(restaurant);
				case "6" -> addMenuItem(restaurant);
				case "7" -> {
					String id = readLine("Item id to remove: ");
					System.out.println(restaurant.removeMenuItem(id) ? "Removed." : "Not found.");
				}
				case "8" -> adjustStock(restaurant);
				case "9" -> {
					List<Order> today = platform.ordersForRestaurantToday(restaurant);
					today.forEach(o -> System.out.println(o.getId() + " " + o.getStatus()
							+ " " + Money.format(o.getTotal())));
					System.out.println("Today revenue (paid/delivered): "
							+ Money.format(platform.restaurantRevenueToday(restaurant)));
				}
				case "10" -> {
					restaurant.setOpen(!restaurant.isOpen());
					System.out.println("Restaurant is now " + (restaurant.isOpen() ? "OPEN" : "CLOSED"));
				}
				case "0" -> back = true;
				default -> System.out.println("Invalid option. Please try again.");
			}
		}
	}

	private void restaurantTransition(Restaurant restaurant, OrderStatus expected, String action) {
		Optional<Order> opt = findRestaurantOrder(restaurant, expected);
		if (opt.isEmpty()) return;
		Order order = opt.get();
		try {
			if ("accept".equals(action)) platform.acceptOrder(order);
			else if ("preparing".equals(action)) platform.markPreparing(order);
			System.out.println("Order " + order.getId() + " → " + order.getStatus());
		} catch (RuntimeException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void restaurantReject(Restaurant restaurant) {
		Optional<Order> opt = findRestaurantOrder(restaurant, OrderStatus.PLACED);
		if (opt.isEmpty()) return;
		platform.rejectOrder(opt.get());
		System.out.println("Order rejected / cancelled.");
	}

	private void restaurantMarkReady(Restaurant restaurant) {
		Optional<Order> opt = findRestaurantOrder(restaurant, OrderStatus.PREPARING);
		if (opt.isEmpty()) return;
		platform.markReady(opt.get());
		System.out.println("Order marked READY and queued for dispatch.");
	}

	private Optional<Order> findRestaurantOrder(Restaurant restaurant, OrderStatus status) {
		List<Order> list = platform.allOrders().stream()
				.filter(o -> o.getRestaurant().equals(restaurant) && o.getStatus() == status)
				.sorted(Comparator.comparing(Order::getPlacedAt))
				.toList();
		if (list.isEmpty()) {
			System.out.println("No orders in status " + status);
			return Optional.empty();
		}
		for (int i = 0; i < list.size(); i++) {
			System.out.println((i + 1) + ". " + list.get(i).getId() + " — "
					+ list.get(i).getCustomer().getName());
		}
		int idx = readInt("Select: ", 1, list.size()) - 1;
		return Optional.of(list.get(idx));
	}

	private void toggleAvailability(Restaurant restaurant) {
		String id = readLine("Item id: ");
		restaurant.findMenuItem(id).ifPresentOrElse(item -> {
			item.setAvailable(!item.isAvailable());
			System.out.println(item.getName() + " available=" + item.isAvailable());
		}, () -> System.out.println("Item not found."));
	}

	private void addMenuItem(Restaurant restaurant) {
		String type = readLine("Type (STANDARD/COMBO/WEIGHTED): ");
		String id = readLine("Id: ");
		String name = readLine("Name: ");
		String cat = readLine("Category: ");
		int prep = readInt("Prep minutes: ", 0, 500);
		double price = readDouble("Price: ");
		int stock = readInt("Daily stock: ", 0, 10000);
		try {
			restaurant.addMenuItem(MenuItemFactory.create(
					type, id, name, cat, prep, true, Money.of(price), stock));
			System.out.println("Item added.");
		} catch (RuntimeException ex) {
			System.out.println("Failed: " + ex.getMessage());
		}
	}

	private void adjustStock(Restaurant restaurant) {
		String id = readLine("Item id: ");
		restaurant.findMenuItem(id).ifPresentOrElse(item -> {
			int stock = readInt("New daily stock: ", 0, 100000);
			item.setDailyStock(stock);
			System.out.println("Stock updated to " + stock);
		}, () -> System.out.println("Item not found."));
	}

	// ===================== Rider =====================

	private void riderArea() {
		Optional<Rider> opt = pickRider();
		if (opt.isEmpty()) return;
		Rider rider = opt.get();
		boolean back = false;
		while (!back) {
			System.out.println("\n--- Rider: " + rider + " ---");
			System.out.println("1. Go on duty");
			System.out.println("2. Go off duty");
			System.out.println("3. Take next READY order from dispatch queue");
			System.out.println("4. View assigned order");
			System.out.println("5. Mark picked up (OUT_FOR_DELIVERY)");
			System.out.println("6. Mark delivered");
			System.out.println("7. Personal stats");
			System.out.println("0. Back");
			switch (readLine("Choose: ")) {
				case "1" -> {
					rider.goOnDuty();
					System.out.println("On duty.");
				}
				case "2" -> {
					try {
						rider.goOffDuty();
						System.out.println("Off duty.");
					} catch (IllegalStateException ex) {
						System.out.println(ex.getMessage());
					}
				}
				case "3" -> assignFromQueue(rider);
				case "4" -> {
					if (rider.getActiveOrder() == null) System.out.println("No active order.");
					else System.out.println(rider.getActiveOrder().priceBreakdown());
				}
				case "5" -> {
					try {
						rider.markPickedUp();
						System.out.println("Picked up.");
					} catch (RuntimeException ex) {
						System.out.println(ex.getMessage());
					}
				}
				case "6" -> {
					try {
						rider.markDelivered();
						System.out.println("Delivered. Completions: " + rider.getCompletedDeliveries());
					} catch (RuntimeException ex) {
						System.out.println(ex.getMessage());
					}
				}
				case "7" -> System.out.println("Deliveries: " + rider.getCompletedDeliveries()
						+ " | Avg duration: " + String.format("%.1f", rider.averageDeliveryMinutes()) + " min");
				case "0" -> back = true;
				default -> System.out.println("Invalid option. Please try again.");
			}
		}
	}

	private void assignFromQueue(Rider rider) {
		Order next = platform.pollDispatchQueue();
		if (next == null) {
			System.out.println("Dispatch queue empty.");
			return;
		}
		try {
			platform.assignRider(next, rider);
			System.out.println("Assigned order " + next.getId());
		} catch (RiderBusyException | RuntimeException ex) {
			System.out.println(ex.getMessage());
			// put back if assignment failed and still READY
			if (next.getStatus() == OrderStatus.READY) {
				platform.getDispatchQueue().addReadyOrder(next);
			}
		}
	}

	// ===================== Admin =====================

	private void adminArea() {
		boolean back = false;
		while (!back) {
			System.out.println("\n--- Admin & Reports ---");
			System.out.println("1. Add restaurant");
			System.out.println("2. Remove restaurant");
			System.out.println("3. Create promotion");
			System.out.println("4. Run reports");
			System.out.println("5. Platform statistics");
			System.out.println("6. Verify Part B worked example");
			System.out.println("7. List distinct cuisines");
			System.out.println("8. Audit log");
			System.out.println("0. Back");
			switch (readLine("Choose: ")) {
				case "1" -> adminAddRestaurant();
				case "2" -> {
					String id = readLine("Restaurant id: ");
					platform.removeRestaurant(id);
					System.out.println("Removed (if existed).");
				}
				case "3" -> adminCreatePromo();
				case "4" -> runReportsMenu();
				case "5" -> {
					System.out.println("Restaurants: " + platform.allRestaurants().size());
					System.out.println("Customers:   " + platform.allCustomers().size());
					System.out.println("Riders:      " + platform.allRiders().size());
					System.out.println("Orders:      " + platform.allOrders().size());
					System.out.println("Dispatch Q:  " + platform.getDispatchQueue().size());
					System.out.println("Live stats:  " + platform.getStatistics().snapshot());
				}
				case "6" -> {
					try {
						System.out.println(SampleData.runWorkedExample(platform));
					} catch (Exception ex) {
						System.out.println("Example failed: " + ex.getMessage());
					}
				}
				case "7" -> platform.distinctCuisines().forEach(c -> System.out.println("  • " + c));
				case "8" -> platform.getAuditLog().getEntries().forEach(System.out::println);
				case "0" -> back = true;
				default -> System.out.println("Invalid option. Please try again.");
			}
		}
	}

	private void adminAddRestaurant() {
		String id = readLine("Id: ");
		String name = readLine("Name: ");
		String district = readLine("District: ");
		String cuisines = readLine("Cuisines (comma-separated): ");
		double rating = readDouble("Rating 0–5: ");
		List<String> cuisineList = List.of(cuisines.split("\\s*,\\s*"));
		try {
			platform.addRestaurant(new Restaurant(id, name, district, cuisineList, rating));
			System.out.println("Restaurant added.");
		} catch (RuntimeException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void adminCreatePromo() {
		System.out.println("1. Percentage  2. Fixed amount  3. Free delivery");
		String t = readLine("Type: ");
		String code = readLine("Code: ");
		try {
			switch (t) {
				case "1" -> {
					double pct = readDouble("Percentage (e.g. 0.20): ");
					double cap = readDouble("Max cap EGP: ");
					platform.addPromotion(new PercentageDiscountStrategy(
							code, BigDecimal.valueOf(pct), Money.of(cap),
							Money.ZERO, LocalDate.now().plusMonths(6)));
				}
				case "2" -> {
					double amt = readDouble("Amount EGP: ");
					platform.addPromotion(new FixedAmountStrategy(code, Money.of(amt)));
				}
				case "3" -> platform.addPromotion(new FreeDeliveryStrategy(code));
				default -> {
					System.out.println("Unknown type.");
					return;
				}
			}
			System.out.println("Promotion created.");
		} catch (RuntimeException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void runReportsMenu() {
		ReportService r = platform.getReports();
		List<Order> orders = new ArrayList<>(platform.allOrders());
		List<Restaurant> restaurants = new ArrayList<>(platform.allRestaurants());
		List<Customer> customers = new ArrayList<>(platform.allCustomers());
		List<Rider> riders = new ArrayList<>(platform.allRiders());

		System.out.println("1.Total revenue  2.Top5 restaurants  3.Avg AOV/district");
		System.out.println("4.High-rated busy  5.By status  6.Rider stats");
		System.out.println("7.Most frequent item  8.Customer history  9.Peak hour  10.Inactive 30d");
		switch (readLine("Report #: ")) {
			case "1" -> {
				LocalDate from = LocalDate.now().minusDays(30);
				LocalDate to = LocalDate.now();
				System.out.println("Revenue " + from + "→" + to + ": "
						+ Money.format(r.totalRevenue(orders, from, to)));
			}
			case "2" -> {
				var top = r.topFiveRestaurantsByRevenue(orders, YearMonth.now());
				if (top.isEmpty()) System.out.println("No data.");
				else top.forEach(e -> System.out.println(e.getKey().getName() + ": " + Money.format(e.getValue())));
			}
			case "3" -> r.averageOrderValuePerDistrict(orders)
					.forEach((d, v) -> System.out.println(d + ": " + Money.format(v)));
			case "4" -> {
				var list = r.highRatedBusyRestaurants(orders, restaurants);
				if (list.isEmpty()) System.out.println("None match (>4.5 and ≥20 completed).");
				else list.forEach(System.out::println);
			}
			case "5" -> r.orderCountByStatus(orders).forEach((s, c) -> System.out.println(s + ": " + c));
			case "6" -> r.riderDeliveryStats(riders).forEach(s ->
					System.out.println(s.riderName() + " — " + s.completedDeliveries()
							+ " deliveries, avg " + String.format("%.1f", s.avgMinutes()) + " min"));
			case "7" -> r.mostFrequentlyOrderedItem(orders).ifPresentOrElse(
					e -> System.out.println(e.getKey() + " ordered " + e.getValue() + " times"),
					() -> System.out.println("No most-frequent item: there are no orders."));
			case "8" -> {
				Optional<Customer> c = pickCustomer();
				c.ifPresent(cust -> {
					var h = r.customerHistory(orders, cust);
					h.ordersNewestFirst().forEach(o -> System.out.println(o.getId() + " " + o.getStatus()
							+ " " + Money.format(o.getTotal())));
					System.out.println("Lifetime: " + Money.format(h.lifetimeSpend()));
				});
			}
			case "9" -> r.peakOrderingHour(orders).ifPresentOrElse(
					h -> System.out.println("Peak hour: " + h + ":00"),
					() -> System.out.println("No orders."));
			case "10" -> {
				var inactive = r.inactiveCustomers(customers, orders, LocalDateTime.now());
				if (inactive.isEmpty()) System.out.println("None.");
				else inactive.forEach(c -> System.out.println(c.getName()));
			}
			default -> System.out.println("Invalid report.");
		}
	}

	// ===================== helpers =====================

	private Optional<Customer> pickCustomer() {
		List<Customer> list = new ArrayList<>(platform.allCustomers());
		if (list.isEmpty()) {
			System.out.println("No customers.");
			return Optional.empty();
		}
		for (int i = 0; i < list.size(); i++) {
			System.out.println((i + 1) + ". " + list.get(i));
		}
		int idx = readInt("Select customer: ", 1, list.size()) - 1;
		return Optional.of(list.get(idx));
	}

	private Optional<Restaurant> pickRestaurantFromAll() {
		List<Restaurant> list = platform.restaurantsByRating();
		if (list.isEmpty()) {
			System.out.println("No restaurants.");
			return Optional.empty();
		}
		printRestaurantList(list);
		int idx = readInt("Select restaurant #: ", 1, list.size()) - 1;
		return Optional.of(list.get(idx));
	}

	private Optional<Rider> pickRider() {
		List<Rider> list = new ArrayList<>(platform.allRiders());
		if (list.isEmpty()) {
			System.out.println("No riders.");
			return Optional.empty();
		}
		for (int i = 0; i < list.size(); i++) {
			System.out.println((i + 1) + ". " + list.get(i));
		}
		int idx = readInt("Select rider: ", 1, list.size()) - 1;
		return Optional.of(list.get(idx));
	}

	private String readLine(String prompt) {
		System.out.print(prompt);
		return in.nextLine().trim();
	}

	private int readInt(String prompt, int min, int max) {
		while (true) {
			String s = readLine(prompt);
			try {
				int v = Integer.parseInt(s);
				if (v >= min && v <= max) return v;
			} catch (NumberFormatException ignored) {}
			System.out.println("Enter a number between " + min + " and " + max + ".");
		}
	}

	private double readDouble(String prompt) {
		while (true) {
			String s = readLine(prompt);
			try {
				double v = Double.parseDouble(s);
				if (v > 0) return v;
			} catch (NumberFormatException ignored) {}
			System.out.println("Enter a number greater than zero.");
		}
	}
}
