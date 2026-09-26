public final class BicycleBehavior implements VehicleBehavior {
	@Override public String typeName() { return "bicycle"; }
	@Override public double maxRangeKm() { return 8; }
	@Override public double speedKmPerHour() { return 15; }
	@Override public int maxLineItems() { return 4; }
}
