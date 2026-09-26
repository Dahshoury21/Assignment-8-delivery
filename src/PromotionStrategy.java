import exception.InvalidPromotionException;

import java.math.BigDecimal;

/**
 * Strategy for promotions (Part G.1).
 * New promo types implement this interface — PricingCalculator never switches on type.
 */
public interface PromotionStrategy {
	String getCode();

	/** Discount applied to subtotal only (never to fees). */
	BigDecimal discountForSubtotal(BigDecimal subtotal);

	/** Allows free-delivery promos to zero the fee without instanceof checks. */
	default BigDecimal adjustDeliveryFee(BigDecimal deliveryFee) {
		return deliveryFee;
	}

	boolean isApplicable(Customer customer, String district, BigDecimal subtotal);

	default void validate(Customer customer, String district, BigDecimal subtotal)
			throws InvalidPromotionException {
		if (!isApplicable(customer, district, subtotal)) {
			throw new InvalidPromotionException(
					"Promotion " + getCode() + " does not apply to this order.");
		}
	}

	String describe();
}
