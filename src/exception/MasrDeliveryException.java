package exception;

/**
 * Root of all platform-specific failures.
 * Callers may catch this to handle any Masr Delivery business error
 * without catching unrelated JVM exceptions.
 *
 * Rule applied project-wide:
 * - Checked: recoverable operational failures the caller can present to the user
 *   (closed restaurant, insufficient balance, expired promo, busy rider, stock, unavailable item).
 * - Unchecked: illegal program state / invariant violations that indicate a bug
 *   in transition logic or internal wiring (invalid status transition).
 */
public class MasrDeliveryException extends Exception {
	public MasrDeliveryException(String message) {
		super(message);
	}

	public MasrDeliveryException(String message, Throwable cause) {
		super(message, cause);
	}
}
