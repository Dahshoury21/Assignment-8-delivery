public enum LoyaltyTier {
	BRONZE,
	SILVER,
	GOLD;

	public static LoyaltyTier fromCompletedOrders(int completedOrderCount) {
		if (completedOrderCount >= 30) return GOLD;
		if (completedOrderCount >= 10) return SILVER;
		return BRONZE;
	}
}
