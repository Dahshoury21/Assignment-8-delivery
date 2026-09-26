import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Open search criteria — compose filters without editing search code (Part D).
 */
@FunctionalInterface
public interface RestaurantCriterion extends Predicate<Restaurant> {

	default RestaurantCriterion and(RestaurantCriterion other) {
		return r -> this.test(r) && other.test(r);
	}

	default RestaurantCriterion or(RestaurantCriterion other) {
		return r -> this.test(r) || other.test(r);
	}

	static RestaurantCriterion always() {
		return r -> true;
	}

	static RestaurantCriterion openOnly() {
		return Restaurant::isOpen;
	}

	static RestaurantCriterion district(String district) {
		return r -> r.getDistrict().equalsIgnoreCase(district);
	}

	static RestaurantCriterion cuisine(String cuisine) {
		return r -> r.getCuisines().stream().anyMatch(c -> c.equalsIgnoreCase(cuisine));
	}

	static RestaurantCriterion minRating(double min) {
		return r -> r.getRating() >= min;
	}

	static RestaurantCriterion maxMenuPrice(java.math.BigDecimal ceiling) {
		return r -> r.getMenu().stream()
				.anyMatch(m -> m.getBasePrice().compareTo(ceiling) <= 0);
	}

	static RestaurantCriterion nameOrCuisineContains(String text) {
		String q = text.toLowerCase();
		return r -> r.getName().toLowerCase().contains(q)
				|| r.getCuisines().stream().anyMatch(c -> c.toLowerCase().contains(q));
	}

	static List<Restaurant> apply(List<Restaurant> source, RestaurantCriterion criterion) {
		List<Restaurant> result = new ArrayList<>();
		for (Restaurant r : source) {
			if (criterion.test(r)) result.add(r);
		}
		return result;
	}
}
