import java.math.BigDecimal;

/**
 * Combo is sold at a precomputed bundle price (discount already applied vs sum of parts).
 */
public class ComboItem extends MenuItem {
	private final BigDecimal bundlePrice;

	public ComboItem(String id, String name, String category, int prepTimeMinutes,
	                 boolean available, BigDecimal bundlePrice, int dailyStock) {
		super(id, name, category, prepTimeMinutes, available, dailyStock);
		if (!Money.isPositive(bundlePrice)) throw new IllegalArgumentException("Bundle price must be > 0");
		this.bundlePrice = Money.scale(bundlePrice);
	}

	public ComboItem(String id, String name, String category, int prepTimeMinutes,
	                 boolean available, BigDecimal bundlePrice) {
		this(id, name, category, prepTimeMinutes, available, bundlePrice, 50);
	}

	@Override
	public BigDecimal getBasePrice() { return bundlePrice; }

	@Override
	public BigDecimal calculateItemTotal(double count) {
		if (count <= 0) throw new IllegalArgumentException("Quantity must be > 0");
		return Money.scale(bundlePrice.multiply(BigDecimal.valueOf(count)));
	}

	@Override
	public String priceLabel() {
		return Money.format(bundlePrice) + " (combo)";
	}
}
