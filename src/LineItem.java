import java.math.BigDecimal;

public record LineItem(MenuItem menuItem, double quantityOrWeight) {
	public LineItem {
		if (menuItem == null) throw new IllegalArgumentException("Menu item required");
		if (quantityOrWeight <= 0) throw new IllegalArgumentException("Quantity/weight must be > 0");
	}

	public BigDecimal getSubtotal() {
		return menuItem.calculateItemTotal(quantityOrWeight);
	}

	/** Units to deduct from stock (ceil weight for weighted items). */
	public int stockUnits() {
		if (menuItem instanceof WeightedItem) {
			return Math.max(1, (int) Math.ceil(quantityOrWeight));
		}
		return (int) Math.ceil(quantityOrWeight);
	}

	@Override
	public String toString() {
		String qty = menuItem instanceof WeightedItem
				? quantityOrWeight + " kg"
				: String.valueOf((int) quantityOrWeight);
		return qty + " × " + menuItem.getName() + " = " + Money.format(getSubtotal());
	}
}
