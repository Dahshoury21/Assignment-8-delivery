import exception.InvalidPromotionException;

import java.math.BigDecimal;

/**
 * Part B pricing rules, in order.
 * Uses polymorphism on MenuItem and PromotionStrategy — no type-switch chains.
 */
public final class PricingCalculator {
	private PricingCalculator() {}

	public static void computeOrderPricing(Order order, int distanceKm)
			throws InvalidPromotionException {
		computeOrderPricing(order, distanceKm, order.getPromotion());
	}

	public static void computeOrderPricing(Order order, int distanceKm, PromotionStrategy promo)
			throws InvalidPromotionException {
		AppConfig cfg = AppConfig.getInstance();

		BigDecimal subtotal = order.getLineItems().stream()
				.map(LineItem::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		subtotal = Money.scale(subtotal);

		BigDecimal deliveryFee = Money.scale(cfg.getBaseDeliveryFee());
		int beyond = Math.max(0, distanceKm - cfg.getFreeKmAllowance());
		if (beyond > 0) {
			deliveryFee = Money.scale(deliveryFee.add(
					cfg.getPerKmFee().multiply(BigDecimal.valueOf(beyond))));
		}

		LoyaltyTier tier = order.getCustomer().getLoyaltyTier();
		if (tier == LoyaltyTier.SILVER) {
			deliveryFee = Money.scale(deliveryFee.multiply(BigDecimal.valueOf(0.90)));
		} else if (tier == LoyaltyTier.GOLD) {
			deliveryFee = Money.ZERO;
		}

		if (promo != null) {
			promo.validate(order.getCustomer(), order.getDeliveryAddress().district(), subtotal);
			deliveryFee = Money.scale(promo.adjustDeliveryFee(deliveryFee));
		}

		BigDecimal serviceFee = Money.scale(
				subtotal.multiply(BigDecimal.valueOf(cfg.getServiceFeePercentage())));

		BigDecimal discount = Money.ZERO;
		if (promo != null) {
			discount = Money.scale(promo.discountForSubtotal(subtotal));
		}

		BigDecimal total = Money.scale(subtotal.add(deliveryFee).add(serviceFee).subtract(discount));
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = Money.ZERO;
		}

		order.setPricing(subtotal, deliveryFee, serviceFee, discount, total);
	}
}
