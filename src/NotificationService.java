public class NotificationService implements OrderObserver {
	@Override
	public void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
		System.out.println("[NOTIFY] " + order.getCustomer().getName()
				+ ": order #" + order.getId() + " " + previous + " → " + current);
	}
}
