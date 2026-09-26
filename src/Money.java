import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money is stored as BigDecimal with scale 2 (piastres).
 * Binary floating point is avoided for currency arithmetic.
 */
public final class Money {
	public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

	private Money() {}

	public static BigDecimal of(double value) {
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal of(String value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal scale(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal max(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) >= 0 ? a : b;
	}

	public static BigDecimal min(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) <= 0 ? a : b;
	}

	public static boolean isPositive(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) > 0;
	}

	public static String format(BigDecimal value) {
		return Money.scale(Objects.requireNonNullElse(value, BigDecimal.ZERO)) + " EGP";
	}
}
