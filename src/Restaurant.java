import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Restaurant {
	private final String id;
	private final String name;
	private final String district;
	private final Set<String> cuisines = new LinkedHashSet<>();
	private double rating;
	private boolean open = true;
	/** Insertion-order menu (Part C.2). */
	private final List<MenuItem> menu = new ArrayList<>();

	public Restaurant(String id, String name, String district, List<String> cuisines, double rating) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Restaurant id required");
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Restaurant name required");
		if (district == null || district.isBlank()) throw new IllegalArgumentException("District required");
		if (rating < 0.0 || rating > 5.0) throw new IllegalArgumentException("Rating must be 0.0–5.0");
		this.id = id.trim();
		this.name = name.trim();
		this.district = district.trim();
		if (cuisines != null) {
			for (String c : cuisines) {
				if (c != null && !c.isBlank()) this.cuisines.add(c.trim());
			}
		}
		this.rating = rating;
	}

	public void addMenuItem(MenuItem item) {
		if (item == null) throw new IllegalArgumentException("Menu item required");
		if (menu.contains(item)) {
			throw new IllegalArgumentException("Duplicate menu item id within restaurant: " + item.getId());
		}
		menu.add(item);
	}

	public boolean removeMenuItem(String itemId) {
		return menu.removeIf(m -> m.getId().equals(itemId));
	}

	public Optional<MenuItem> findMenuItem(String itemId) {
		return menu.stream().filter(m -> m.getId().equals(itemId)).findFirst();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public String getDistrict() { return district; }
	public double getRating() { return rating; }
	public void setRating(double rating) {
		if (rating < 0.0 || rating > 5.0) throw new IllegalArgumentException("Rating must be 0.0–5.0");
		this.rating = rating;
	}
	public boolean isOpen() { return open; }
	public void setOpen(boolean open) { this.open = open; }

	public Set<String> getCuisines() {
		return Collections.unmodifiableSet(cuisines);
	}

	public void addCuisine(String cuisine) {
		if (cuisine != null && !cuisine.isBlank()) cuisines.add(cuisine.trim());
	}

	/** Defensive copy — caller cannot mutate the restaurant menu (Part C.8). */
	public List<MenuItem> getMenu() {
		return Collections.unmodifiableList(menu);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Restaurant other)) return false;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		String status = open ? "OPEN" : "CLOSED";
		return name + " [" + district + "] ★" + String.format("%.1f", rating)
				+ " — " + String.join(", ", cuisines) + " (" + status + ")";
	}
}
