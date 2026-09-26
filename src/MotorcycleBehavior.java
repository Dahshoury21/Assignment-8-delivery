public final class MotorcycleBehavior implements VehicleBehavior {
	@Override public String typeName() { return "motorcycle"; }
	@Override public double maxRangeKm() { return 25; }
	@Override public double speedKmPerHour() { return 40; }
	@Override public int maxLineItems() { return 12; }
}
