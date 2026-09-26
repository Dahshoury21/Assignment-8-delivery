import java.math.BigDecimal;
import java.util.Objects;

public abstract class MenuItem {
	private final String id;
	private final String name;
	private final String category;
	private final int prepTimeMinutes;
	private boolean available;
	private int dailyStock;

	protected MenuItem(String id, String name, String category, int prepTimeMinutes,
	                   boolean available, int dailyStock) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Menu item id required");
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Menu item name required");
		if (prepTimeMinutes < 0) throw new IllegalArgumentException("Prep time cannot be negative");
		this.id = id.trim();
		this.name = name.trim();
		this.category = category == null ? "General" : category.trim();
		this.prepTimeMinutes = prepTimeMinutes;
		this.available = available;
		this.dailyStock = Math.max(0, dailyStock);
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public String getCategory() { return category; }
	public int getPrepTimeMinutes() { return prepTimeMinutes; }
	public boolean isAvailable() { return available && dailyStock > 0; }
	public void setAvailable(boolean available) { this.available = available; }
	public int getDailyStock() { return dailyStock; }

	public void setDailyStock(int dailyStock) {
		if (dailyStock < 0) throw new IllegalArgumentException("Stock cannot be negative");
		this.dailyStock = dailyStock;
	}

	public void consumeStock(int units) throws exception.StockShortageException {
		if (units > dailyStock) {
			throw new exception.StockShortageException(
					"Not enough stock for '" + name + "'. Available: " + dailyStock);
		}
		dailyStock -= units;
	}

	public void restoreStock(int units) {
		dailyStock += units;
	}

	/** Polymorphic pricing — new item kinds override this without touching order code. */
	public abstract BigDecimal calculateItemTotal(double quantityOrWeight);

	public abstract BigDecimal getBasePrice();

	public abstract String priceLabel();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MenuItem other)) return false;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		String flag = isAvailable() ? "" : " [UNAVAILABLE]";
		return name + " — " + priceLabel() + flag;
	}
}
