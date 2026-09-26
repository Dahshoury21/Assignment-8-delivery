import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedAmountStrategy implements PromotionStrategy {
	private final String code;
	private final BigDecimal discountAmount;
	private final BigDecimal minSubtotal;
	private final LocalDate expiryDate;

	public FixedAmountStrategy(String code, BigDecimal discountAmount) {
		this(code, discountAmount, BigDecimal.ZERO, LocalDate.now().plusYears(1));
	}

	public FixedAmountStrategy(String code, BigDecimal discountAmount,
	                           BigDecimal minSubtotal, LocalDate expiryDate) {
		this.code = code;
		this.discountAmount = Money.scale(discountAmount);
		this.minSubtotal = minSubtotal == null ? BigDecimal.ZERO : minSubtotal;
		this.expiryDate = expiryDate;
	}

	@Override
	public String getCode() { return code; }

	@Override
	public boolean isApplicable(Customer customer, String district, BigDecimal subtotal) {
		if (expiryDate != null && LocalDate.now().isAfter(expiryDate)) return false;
		return subtotal.compareTo(minSubtotal) >= 0;
	}

	@Override
	public BigDecimal discountForSubtotal(BigDecimal subtotal) {
		return Money.min(discountAmount, Money.scale(subtotal));
	}

	@Override
	public String describe() {
		return code + ": " + Money.format(discountAmount) + " off subtotal";
	}
}
