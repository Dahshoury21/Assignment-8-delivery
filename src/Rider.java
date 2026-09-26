import exception.RiderBusyException;

import java.util.Objects;

public class Rider {
	private final String id;
	private final String name;
	private final VehicleBehavior vehicle;
	private String currentDistrict;
	private boolean onDuty = false;
	private Order activeOrder = null;
	private int completedDeliveries = 0;
	private long totalDeliveryMinutes = 0;

	public Rider(String id, String name, VehicleBehavior vehicle, String currentDistrict) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Rider id required");
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Rider name required");
		if (vehicle == null) throw new IllegalArgumentException("Vehicle behavior required");
		if (currentDistrict == null || currentDistrict.isBlank()) {
			throw new IllegalArgumentException("District required");
		}
		this.id = id.trim();
		this.name = name.trim();
		this.vehicle = vehicle;
		this.currentDistrict = currentDistrict.trim();
	}

	public void goOnDuty() { onDuty = true; }
	public void goOffDuty() {
		if (activeOrder != null) {
			throw new IllegalStateException("Cannot go off duty while holding an active order");
		}
		onDuty = false;
	}

	public boolean isAvailable() {
		return onDuty && activeOrder == null;
	}

	public void assignOrder(Order order) throws RiderBusyException {
		if (!onDuty) {
			throw new RiderBusyException("Rider " + name + " is off duty.");
		}
		if (activeOrder != null) {
			throw new RiderBusyException("Rider " + name + " already has an active order — absolute rule.");
		}
		if (!vehicle.canHandle(order)) {
			throw new RiderBusyException(
					"Rider " + name + "'s " + vehicle.typeName() + " cannot handle this order size/distance.");
		}
		this.activeOrder = order;
		order.transitionTo(OrderStatus.ASSIGNED);
	}

	public void markPickedUp() {
		if (activeOrder == null) throw new IllegalStateException("No active order");
		activeOrder.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
	}

	public void markDelivered() {
		if (activeOrder == null) throw new IllegalStateException("No active order");
		Order order = activeOrder;
		order.transitionTo(OrderStatus.DELIVERED);
		order.getCustomer().incrementCompletedOrders();
		completedDeliveries++;
		if (order.getDeliveredAt() != null && order.getPlacedAt() != null) {
			totalDeliveryMinutes += java.time.Duration.between(order.getPlacedAt(), order.getDeliveredAt()).toMinutes();
		}
		currentDistrict = order.getDeliveryAddress().district();
		activeOrder = null;
	}

	public double averageDeliveryMinutes() {
		return completedDeliveries == 0 ? 0.0 : (double) totalDeliveryMinutes / completedDeliveries;
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public VehicleBehavior getVehicle() { return vehicle; }
	public String getVehicleType() { return vehicle.typeName(); }
	public String getCurrentDistrict() { return currentDistrict; }
	public void setCurrentDistrict(String currentDistrict) { this.currentDistrict = currentDistrict; }
	public boolean isOnDuty() { return onDuty; }
	public Order getActiveOrder() { return activeOrder; }
	public int getCompletedDeliveries() { return completedDeliveries; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Rider other)) return false;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		String duty = onDuty ? "ON DUTY" : "OFF DUTY";
		String busy = activeOrder != null ? " | holding " + activeOrder.getId() : "";
		return name + " (" + vehicle.typeName() + ", " + currentDistrict + ") — " + duty + busy;
	}
}
