public final class CarBehavior implements VehicleBehavior {
	@Override public String typeName() { return "car"; }
	@Override public double maxRangeKm() { return 50; }
	@Override public double speedKmPerHour() { return 35; }
	@Override public int maxLineItems() { return 30; }
}
