public class RiderDashboardObserver implements OrderObserver {
	@Override
	public void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
		if (current == OrderStatus.ASSIGNED || current == OrderStatus.OUT_FOR_DELIVERY
				|| current == OrderStatus.DELIVERED || current == OrderStatus.READY) {
			System.out.println("[RIDER DASH] Order #" + order.getId() + " is now " + current
					+ (order.getAssignedRider() != null
					? " (rider: " + order.getAssignedRider().getName() + ")"
					: ""));
		}
	}
}
