import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Seeds demo data including the Part B worked example (NILE20 → 258.00 EGP). */
public final class SampleData {
	private SampleData() {}

	public static Platform bootstrap() {
		Platform platform = new Platform();

		Customer ahmed = new Customer("C1", "Ahmed Hassan", "01012345678", Money.of(500));
		ahmed.addAddress(new Address("Faisal", "12 Street 9, near metro"));
		ahmed.addAddress(new Address("Dokki", "45 Tahrir St"));
		platform.addCustomer(ahmed);

		Customer sara = new Customer("C2", "Sara Ali", "01123456789", Money.of(200));
		sara.addAddress(new Address("Nasr City", "Abbas El Akkad 100"));
		platform.addCustomer(sara);

		Customer goldCust = new Customer("C3", "Omar Gold", "01234567890", Money.of(1000));
		// bump completed orders into Gold tier (30+)
		for (int i = 0; i < 30; i++) goldCust.incrementCompletedOrders();
		goldCust.addAddress(new Address("Heliopolis", "Korba square 5"));
		platform.addCustomer(goldCust);

		Restaurant maadiGrill = new Restaurant(
				"R1", "Maadi Grill House", "Maadi",
				List.of("Grill", "Egyptian"), 4.7);
		maadiGrill.addMenuItem(MenuItemFactory.create(
				"STANDARD", "R1-M1", "Koshari Plate", "Mains", 15, true, Money.of(60), 50));
		maadiGrill.addMenuItem(MenuItemFactory.create(
				"STANDARD", "R1-M2", "Grilled Chicken", "Mains", 25, true, Money.of(120), 40));
		maadiGrill.addMenuItem(MenuItemFactory.create(
				"COMBO", "R1-M3", "Family Grill Combo", "Combos", 40, true, Money.of(240), 20));
		maadiGrill.addMenuItem(MenuItemFactory.create(
				"WEIGHTED", "R1-M4", "Kofta", "Grill", 20, true, Money.of(280), 25));
		platform.addRestaurant(maadiGrill);

		Restaurant dokkiPizza = new Restaurant(
				"R2", "Dokki Pizza Lab", "Dokki",
				List.of("Italian", "Pizza"), 4.3);
		dokkiPizza.addMenuItem(MenuItemFactory.create(
				"STANDARD", "R2-M1", "Margherita", "Pizza", 20, true, Money.of(95), 40));
		dokkiPizza.addMenuItem(MenuItemFactory.create(
				"COMBO", "R2-M2", "Pizza + Drink Combo", "Combos", 25, true, Money.of(130), 30));
		platform.addRestaurant(dokkiPizza);

		Restaurant closedCafe = new Restaurant(
				"R3", "Faisal Nights", "Faisal",
				List.of("Cafe", "Desserts"), 3.9);
		closedCafe.setOpen(false);
		closedCafe.addMenuItem(MenuItemFactory.create(
				"STANDARD", "R3-M1", "Kunafa", "Dessert", 10, true, Money.of(55), 20));
		platform.addRestaurant(closedCafe);

		platform.addRider(new Rider("D1", "Youssef", VehicleFactory.create("motorcycle"), "Maadi"));
		platform.addRider(new Rider("D2", "Nour", VehicleFactory.create("bicycle"), "Dokki"));
		platform.addRider(new Rider("D3", "Karim", VehicleFactory.create("car"), "Nasr City"));

		platform.addPromotion(new PercentageDiscountStrategy(
				"NILE20",
				BigDecimal.valueOf(0.20),
				Money.of(50),
				Money.ZERO,
				LocalDate.now().plusMonths(6)));
		platform.addPromotion(new FixedAmountStrategy(
				"SAVE30", Money.of(30), Money.of(100), LocalDate.now().plusMonths(3)));
		platform.addPromotion(new FreeDeliveryStrategy(
				"FREEDEL", LocalDate.now().plusMonths(2), Money.of(80)));

		return platform;
	}

	/**
	 * Reproduces the PDF worked example exactly:
	 * Faisal customer, Maadi restaurant 12 km, subtotal 240, Bronze, NILE20 → total 258.00
	 */
	public static String runWorkedExample(Platform platform) throws Exception {
		Customer customer = platform.findCustomer("C1").orElseThrow();
		Restaurant restaurant = platform.findRestaurant("R1").orElseThrow();
		Address address = customer.getAddresses().get(0); // Faisal
		PromotionStrategy promo = platform.findPromotion("NILE20").orElseThrow();

		MenuItem combo = restaurant.findMenuItem("R1-M3").orElseThrow(); // 240 combo

		OrderBuilder builder = new OrderBuilder()
				.id(platform.nextOrderId())
				.customer(customer)
				.restaurant(restaurant)
				.deliveryAddress(address)
				.promotion(promo)
				.addLineItem(combo, 1);

		Order order = platform.placeOrder(builder);
		int km = DistanceService.distanceKm("Maadi", "Faisal");

		StringBuilder sb = new StringBuilder();
		sb.append("=== Part B Worked Example ===\n");
		sb.append("Distance Maadi → Faisal: ").append(km).append(" km\n");
		sb.append("Loyalty: ").append(customer.getLoyaltyTier()).append('\n');
		sb.append(order.priceBreakdown()).append('\n');
		sb.append("Expected total: 258.00 EGP\n");
		sb.append("Match: ").append(order.getTotal().compareTo(Money.of(258)) == 0 ? "YES" : "NO");
		return sb.toString();
	}
}
