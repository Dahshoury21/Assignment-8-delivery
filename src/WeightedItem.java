import java.math.BigDecimal;

public class WeightedItem extends MenuItem {
	private final BigDecimal pricePerKg;

	public WeightedItem(String id, String name, String category, int prepTimeMinutes,
	                    boolean available, BigDecimal pricePerKg, int dailyStock) {
		super(id, name, category, prepTimeMinutes, available, dailyStock);
		if (!Money.isPositive(pricePerKg)) throw new IllegalArgumentException("Price/kg must be > 0");
		this.pricePerKg = Money.scale(pricePerKg);
	}

	public WeightedItem(String id, String name, String category, int prepTimeMinutes,
	                    boolean available, BigDecimal pricePerKg) {
		this(id, name, category, prepTimeMinutes, available, pricePerKg, 30);
	}

	@Override
	public BigDecimal getBasePrice() { return pricePerKg; }

	@Override
	public BigDecimal calculateItemTotal(double weightInKg) {
		if (weightInKg <= 0) throw new IllegalArgumentException("Weight must be > 0");
		return Money.scale(pricePerKg.multiply(BigDecimal.valueOf(weightInKg)));
	}

	@Override
	public String priceLabel() {
		return Money.format(pricePerKg) + " / kg";
	}
}
