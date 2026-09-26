import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Simple district-distance table for delivery fee calculation. */
public final class DistanceService {
	private static final Map<String, Map<String, Integer>> KM = new HashMap<>();

	static {
		put("Maadi", "Faisal", 12);
		put("Maadi", "Dokki", 14);
		put("Maadi", "Nasr City", 18);
		put("Maadi", "Heliopolis", 22);
		put("Dokki", "Faisal", 6);
		put("Dokki", "Nasr City", 12);
		put("Dokki", "Heliopolis", 15);
		put("Faisal", "Nasr City", 16);
		put("Faisal", "Heliopolis", 20);
		put("Nasr City", "Heliopolis", 8);
		// same district
		for (String d : new String[]{"Maadi", "Dokki", "Faisal", "Nasr City", "Heliopolis"}) {
			put(d, d, 1);
		}
	}

	private DistanceService() {}

	private static void put(String a, String b, int km) {
		KM.computeIfAbsent(norm(a), k -> new HashMap<>()).put(norm(b), km);
		KM.computeIfAbsent(norm(b), k -> new HashMap<>()).put(norm(a), km);
	}

	private static String norm(String d) {
		return d.trim().toLowerCase(Locale.ROOT);
	}

	public static int distanceKm(String fromDistrict, String toDistrict) {
		if (fromDistrict == null || toDistrict == null) return 5;
		Integer km = KM.getOrDefault(norm(fromDistrict), Map.of()).get(norm(toDistrict));
		return km != null ? km : 10; // default unknown pair
	}
}
