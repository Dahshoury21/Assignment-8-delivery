import exception.InvalidPromotionException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PercentageDiscountStrategy implements PromotionStrategy {
	private final String code;
	private final BigDecimal percentage; // e.g. 0.20 for 20%
	private final BigDecimal maxCap;
	private final BigDecimal minSubtotal;
	private final LocalDate expiryDate;
	private final String restrictedDistrict; // null = any
	private final boolean firstTimeOnly;

	public PercentageDiscountStrategy(String code, BigDecimal percentage, BigDecimal maxCap,
	                                  BigDecimal minSubtotal, LocalDate expiryDate) {
		this(code, percentage, maxCap, minSubtotal, expiryDate, null, false);
	}

	public PercentageDiscountStrategy(String code, BigDecimal percentage, BigDecimal maxCap,
	                                  BigDecimal minSubtotal, LocalDate expiryDate,
	                                  String restrictedDistrict, boolean firstTimeOnly) {
		this.code = code;
		this.percentage = percentage;
		this.maxCap = maxCap;
		this.minSubtotal = minSubtotal == null ? BigDecimal.ZERO : minSubtotal;
		this.expiryDate = expiryDate;
		this.restrictedDistrict = restrictedDistrict;
		this.firstTimeOnly = firstTimeOnly;
	}

	@Override
	public String getCode() { return code; }

	@Override
	public boolean isApplicable(Customer customer, String district, BigDecimal subtotal) {
		if (expiryDate != null && LocalDate.now().isAfter(expiryDate)) return false;
		if (subtotal.compareTo(minSubtotal) < 0) return false;
		if (restrictedDistrict != null && !restrictedDistrict.equalsIgnoreCase(district)) return false;
		if (firstTimeOnly && customer.getCompletedOrderCount() > 0) return false;
		return true;
	}

	@Override
	public void validate(Customer customer, String district, BigDecimal subtotal)
			throws InvalidPromotionException {
		if (expiryDate != null && LocalDate.now().isAfter(expiryDate)) {
			throw new InvalidPromotionException("Promotion " + code + " has expired.");
		}
		if (subtotal.compareTo(minSubtotal) < 0) {
			throw new InvalidPromotionException(
					"Subtotal " + Money.format(subtotal) + " is below minimum "
							+ Money.format(minSubtotal) + " for " + code);
		}
		if (restrictedDistrict != null && !restrictedDistrict.equalsIgnoreCase(district)) {
			throw new InvalidPromotionException("Promotion " + code + " is restricted to " + restrictedDistrict);
		}
		if (firstTimeOnly && customer.getCompletedOrderCount() > 0) {
			throw new InvalidPromotionException("Promotion " + code + " is for first-time customers only.");
		}
	}

	@Override
	public BigDecimal discountForSubtotal(BigDecimal subtotal) {
		BigDecimal discount = Money.scale(subtotal.multiply(percentage));
		if (maxCap != null) {
			discount = Money.min(discount, Money.scale(maxCap));
		}
		return discount;
	}

	@Override
	public String describe() {
		return code + ": " + percentage.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()
				+ "% off subtotal"
				+ (maxCap != null ? " (cap " + Money.format(maxCap) + ")" : "");
	}
}
