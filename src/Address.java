public record Address(String district, String detailLine) {
	public Address {
		if (district == null || district.isBlank()) {
			throw new IllegalArgumentException("District cannot be empty.");
		}
		if (detailLine == null || detailLine.isBlank()) {
			throw new IllegalArgumentException("Address detail cannot be empty.");
		}
		district = district.trim();
		detailLine = detailLine.trim();
	}

	@Override
	public String toString() {
		return detailLine + ", " + district;
	}
}
