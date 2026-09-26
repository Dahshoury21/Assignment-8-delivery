import exception.InvalidOrderTransitionException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
	private final String id;
	private final Customer customer;
	private final Restaurant restaurant;
	private final Address deliveryAddress;
	private final List<LineItem> lineItems;
	private final LocalDateTime placedAt;
	private final String deliveryNotes;
	private OrderStatus status;
	private PromotionStrategy promotion;
	private boolean paid;
	private LocalDateTime deliveredAt;
	private Rider assignedRider;

	private BigDecimal subtotal = Money.ZERO;
	private BigDecimal deliveryFee = Money.ZERO;
	private BigDecimal serviceFee = Money.ZERO;
	private BigDecimal discountAmount = Money.ZERO;
	private BigDecimal total = Money.ZERO;

	private final List<OrderObserver> observers = new ArrayList<>();

	Order(String id, Customer customer, Restaurant restaurant, Address deliveryAddress,
	      List<LineItem> lineItems, String deliveryNotes, PromotionStrategy promotion) {
		this.id = id;
		this.customer = customer;
		this.restaurant = restaurant;
		this.deliveryAddress = deliveryAddress;
		this.lineItems = List.copyOf(lineItems);
		this.deliveryNotes = deliveryNotes;
		this.promotion = promotion;
		this.placedAt = LocalDateTime.now();
		this.status = OrderStatus.PLACED;
		this.paid = false;
	}

	public void addObserver(OrderObserver observer) {
		if (observer != null && !observers.contains(observer)) {
			observers.add(observer);
		}
	}

	private void notifyObservers(OrderStatus previous) {
		for (OrderObserver observer : List.copyOf(observers)) {
			observer.onOrderStatusChanged(this, previous, status);
		}
	}

	public void transitionTo(OrderStatus next) {
		status.assertCanTransitionTo(next);
		OrderStatus previous = this.status;
		this.status = next;
		if (next == OrderStatus.DELIVERED) {
			this.deliveredAt = LocalDateTime.now();
		}
		notifyObservers(previous);
	}

	public void cancel() {
		if (!status.isCancellable()) {
			throw new InvalidOrderTransitionException(
					"Order " + id + " cannot be cancelled from status " + status);
		}
		transitionTo(OrderStatus.CANCELLED);
	}

	public void setPricing(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal serviceFee,
	                       BigDecimal discountAmount, BigDecimal total) {
		this.subtotal = Money.scale(subtotal);
		this.deliveryFee = Money.scale(deliveryFee);
		this.serviceFee = Money.scale(serviceFee);
		this.discountAmount = Money.scale(discountAmount);
		this.total = Money.scale(total);
	}

	public void markPaid() { this.paid = true; }
	public boolean isPaid() { return paid; }

	public void setAssignedRider(Rider rider) { this.assignedRider = rider; }
	public Rider getAssignedRider() { return assignedRider; }

	public Duration elapsedSincePlacement() {
		LocalDateTime end = deliveredAt != null ? deliveredAt : LocalDateTime.now();
		return Duration.between(placedAt, end);
	}

	public String getId() { return id; }
	public Customer getCustomer() { return customer; }
	public Restaurant getRestaurant() { return restaurant; }
	public Address getDeliveryAddress() { return deliveryAddress; }
	public List<LineItem> getLineItems() { return lineItems; }
	public LocalDateTime getPlacedAt() { return placedAt; }
	public LocalDateTime getDeliveredAt() { return deliveredAt; }
	public OrderStatus getStatus() { return status; }
	public String getDeliveryNotes() { return deliveryNotes; }
	public PromotionStrategy getPromotion() { return promotion; }
	public BigDecimal getSubtotal() { return subtotal; }
	public BigDecimal getDeliveryFee() { return deliveryFee; }
	public BigDecimal getServiceFee() { return serviceFee; }
	public BigDecimal getDiscountAmount() { return discountAmount; }
	public BigDecimal getTotal() { return total; }

	public String priceBreakdown() {
		StringBuilder sb = new StringBuilder();
		sb.append("----- Order ").append(id).append(" -----\n");
		for (LineItem li : lineItems) {
			sb.append("  ").append(li).append('\n');
		}
		sb.append("Subtotal:      ").append(Money.format(subtotal)).append('\n');
		sb.append("Delivery fee:  ").append(Money.format(deliveryFee)).append('\n');
		sb.append("Service fee:   ").append(Money.format(serviceFee)).append('\n');
		if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
			sb.append("Promotion:    -").append(Money.format(discountAmount));
			if (promotion != null) sb.append(" (").append(promotion.getCode()).append(')');
			sb.append('\n');
		}
		sb.append("TOTAL:         ").append(Money.format(total)).append('\n');
		sb.append("Status:        ").append(status);
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Order other)) return false;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
