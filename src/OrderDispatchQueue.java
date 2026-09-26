import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * READY orders awaiting assignment (Part C.5).
 * Gold customers jump ahead of Bronze/Silver; otherwise longest-waiting first.
 */
public class OrderDispatchQueue {
	private final PriorityQueue<Order> readyOrders = new PriorityQueue<>(
			Comparator
					.<Order>comparingInt(o -> o.getCustomer().getLoyaltyTier() == LoyaltyTier.GOLD ? 0 : 1)
					.thenComparing(Order::getPlacedAt)
	);

	public void addReadyOrder(Order order) {
		if (order.getStatus() != OrderStatus.READY) {
			throw new IllegalArgumentException("Only READY orders may enter the dispatch queue");
		}
		readyOrders.add(order);
	}

	public Order dispatchNextOrder() {
		return readyOrders.poll();
	}

	public boolean isEmpty() {
		return readyOrders.isEmpty();
	}

	public int size() {
		return readyOrders.size();
	}
}
