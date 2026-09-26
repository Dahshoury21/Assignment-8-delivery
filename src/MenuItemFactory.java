import java.math.BigDecimal;

/** Factory — type decision stays in one place (Part G.3). */
public final class MenuItemFactory {
	private MenuItemFactory() {}

	public static MenuItem create(String type, String id, String name, String category,
	                              int prepTime, boolean available, BigDecimal price) {
		return create(type, id, name, category, prepTime, available, price, 100);
	}

	public static MenuItem create(String type, String id, String name, String category,
	                              int prepTime, boolean available, BigDecimal price, int stock) {
		if (type == null) throw new IllegalArgumentException("Menu item type required");
		return switch (type.trim().toUpperCase()) {
			case "STANDARD", "STD" ->
					new StandardItem(id, name, category, prepTime, available, price, stock);
			case "COMBO" ->
					new ComboItem(id, name, category, prepTime, available, price, stock);
			case "WEIGHTED", "WEIGHT", "KG" ->
					new WeightedItem(id, name, category, prepTime, available, price, stock);
			default -> throw new IllegalArgumentException("Unknown menu item type: " + type);
		};
	}
}
