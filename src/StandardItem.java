import java.math.BigDecimal;

public class StandardItem extends MenuItem {
	private final BigDecimal price;

	public StandardItem(String id, String name, String category, int prepTimeMinutes,
	                    boolean available, BigDecimal price, int dailyStock) {
		super(id, name, category, prepTimeMinutes, available, dailyStock);
		if (!Money.isPositive(price)) throw new IllegalArgumentException("Price must be > 0");
		this.price = Money.scale(price);
	}

	public StandardItem(String id, String name, String category, int prepTimeMinutes,
	                    boolean available, BigDecimal price) {
		this(id, name, category, prepTimeMinutes, available, price, 100);
	}

	@Override
	public BigDecimal getBasePrice() { return price; }

	@Override
	public BigDecimal calculateItemTotal(double quantity) {
		if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
		return Money.scale(price.multiply(BigDecimal.valueOf(quantity)));
	}

	@Override
	public String priceLabel() {
		return Money.format(price);
	}
}
