import java.math.BigDecimal;

/** Singleton platform configuration (Part G.5). */
public class AppConfig {
	private static volatile AppConfig instance;

	private BigDecimal baseDeliveryFee = Money.of(15);
	private BigDecimal perKmFee = Money.of(3);
	private int freeKmAllowance = 3;
	private double serviceFeePercentage = 0.10;
	private String dataDirectory = "data";

	private AppConfig() {}

	public static AppConfig getInstance() {
		if (instance == null) {
			synchronized (AppConfig.class) {
				if (instance == null) {
					instance = new AppConfig();
				}
			}
		}
		return instance;
	}

	/** Package-visible for tests only. */
	static void resetForTests() {
		instance = null;
	}

	public BigDecimal getBaseDeliveryFee() { return baseDeliveryFee; }
	public BigDecimal getPerKmFee() { return perKmFee; }
	public int getFreeKmAllowance() { return freeKmAllowance; }
	public double getServiceFeePercentage() { return serviceFeePercentage; }
	public String getDataDirectory() { return dataDirectory; }

	public void setBaseDeliveryFee(BigDecimal v) { this.baseDeliveryFee = Money.scale(v); }
	public void setPerKmFee(BigDecimal v) { this.perKmFee = Money.scale(v); }
	public void setFreeKmAllowance(int v) { this.freeKmAllowance = v; }
	public void setServiceFeePercentage(double v) { this.serviceFeePercentage = v; }
	public void setDataDirectory(String dataDirectory) { this.dataDirectory = dataDirectory; }
}
