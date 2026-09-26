public final class VehicleFactory {
	private VehicleFactory() {}

	public static VehicleBehavior create(String type) {
		if (type == null) throw new IllegalArgumentException("Vehicle type required");
		return switch (type.trim().toLowerCase()) {
			case "motorcycle", "motor", "bike-motor" -> new MotorcycleBehavior();
			case "bicycle", "bike", "cycle" -> new BicycleBehavior();
			case "car", "auto" -> new CarBehavior();
			default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
		};
	}
}
