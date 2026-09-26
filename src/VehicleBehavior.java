/**
 * Strategy for vehicle-specific dispatch rules (Part G.6).
 * Adding an electric scooter means a new implementation — no edits to Rider.
 */
public interface VehicleBehavior {
	String typeName();
	double maxRangeKm();
	double speedKmPerHour();
	int maxLineItems();

	default boolean canHandle(Order order) {
		int items = order.getLineItems().size();
		return items <= maxLineItems();
	}
}
