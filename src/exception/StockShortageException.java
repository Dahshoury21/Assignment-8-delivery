package exception;

public class StockShortageException extends MasrDeliveryException {
	public StockShortageException(String message) {
		super(message);
	}
}
