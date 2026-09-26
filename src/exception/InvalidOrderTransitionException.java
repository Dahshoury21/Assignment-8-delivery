package exception;

/** Unchecked: illegal lifecycle transition is an invariant violation. */
public class InvalidOrderTransitionException extends RuntimeException {
	public InvalidOrderTransitionException(String message) {
		super(message);
	}
}
