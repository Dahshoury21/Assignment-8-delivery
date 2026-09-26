public interface OrderObserver {
	void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus current);
}
