import java.math.BigDecimal;
import java.time.LocalDate;

public class FreeDeliveryStrategy implements PromotionStrategy {
	private final String code;
	private final LocalDate expiryDate;
	private final BigDecimal minSubtotal;

	public FreeDeliveryStrategy(String code) {
		this(code, LocalDate.now().plusYears(1), BigDecimal.ZERO);
	}

	public FreeDeliveryStrategy(String code, LocalDate expiryDate, BigDecimal minSubtotal) {
		this.code = code;
		this.expiryDate = expiryDate;
		this.minSubtotal = minSubtotal == null ? BigDecimal.ZERO : minSubtotal;
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
		return Money.ZERO;
	}

	@Override
	public BigDecimal adjustDeliveryFee(BigDecimal deliveryFee) {
		return Money.ZERO;
	}

	@Override
	public String describe() {
		return code + ": free delivery";
	}
}
