import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLogObserver implements OrderObserver {
	private final List<String> entries = new ArrayList<>();

	@Override
	public void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
		String entry = java.time.LocalDateTime.now()
				+ " | Order #" + order.getId() + " | " + previous + " → " + current;
		entries.add(entry);
		System.out.println("[AUDIT] " + entry);
	}

	public List<String> getEntries() {
		return Collections.unmodifiableList(entries);
	}
}
