import java.util.EnumMap;
import java.util.Map;

/** Recalculates live counters when orders change status (Part G.4). */
public class StatisticsObserver implements OrderObserver {
	private final Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);

	public StatisticsObserver() {
		for (OrderStatus s : OrderStatus.values()) {
			counts.put(s, 0L);
		}
	}

	public void registerPlaced() {
		counts.merge(OrderStatus.PLACED, 1L, Long::sum);
	}

	@Override
	public void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
		counts.merge(previous, -1L, Long::sum);
		counts.merge(current, 1L, Long::sum);
		System.out.println("[STATS] Live status counts updated.");
	}

	public Map<OrderStatus, Long> snapshot() {
		return Map.copyOf(counts);
	}
}
