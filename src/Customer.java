import exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public class Customer {
	private final String id;
	private final String name;
	private final String mobileNumber;
	private final List<Address> addresses = new ArrayList<>();
	private BigDecimal walletBalance;
	private int completedOrderCount = 0;
	/** Newest first; at most five entries (Part C.6). */
	private final Deque<String> recentSearches = new ArrayDeque<>(5);

	public Customer(String id, String name, String mobileNumber, BigDecimal walletBalance) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Customer id required");
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer name required");
		this.id = id.trim();
		this.name = name.trim();
		this.mobileNumber = validateMobile(mobileNumber);
		this.walletBalance = Money.scale(walletBalance == null ? BigDecimal.ZERO : walletBalance);
		if (this.walletBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet balance cannot be negative");
		}
	}

	private static String validateMobile(String mobile) {
		if (mobile != null && mobile.matches("^01[0125]\\d{8}$")) {
			return mobile;
		}
		throw new IllegalArgumentException(
				"Invalid Egyptian mobile number. Must be 11 digits starting with 010, 011, 012, or 015.");
	}

	/** Tier is always derived — never set by hand (A.2). */
	public LoyaltyTier getLoyaltyTier() {
		return LoyaltyTier.fromCompletedOrders(completedOrderCount);
	}

	public void addSearchQuery(String query) {
		if (query == null || query.isBlank()) return;
		recentSearches.remove(query);
		if (recentSearches.size() == 5) {
			recentSearches.removeLast();
		}
		recentSearches.addFirst(query.trim());
	}

	public List<String> getRecentSearches() {
		return List.copyOf(recentSearches);
	}

	public void incrementCompletedOrders() {
		completedOrderCount++;
	}

	public void deductWallet(BigDecimal amount) throws InsufficientBalanceException {
		BigDecimal scaled = Money.scale(amount);
		if (walletBalance.compareTo(scaled) < 0) {
			throw new InsufficientBalanceException(
					"Insufficient wallet balance. Available: " + Money.format(walletBalance));
		}
		walletBalance = walletBalance.subtract(scaled);
	}

	public void refundWallet(BigDecimal amount) {
		walletBalance = Money.scale(walletBalance.add(amount));
	}

	public void topUp(BigDecimal amount) {
		if (!Money.isPositive(amount)) throw new IllegalArgumentException("Top-up must be > 0");
		walletBalance = Money.scale(walletBalance.add(amount));
	}

	public boolean ownsAddress(Address address) {
		return addresses.contains(address);
	}

	public void addAddress(Address address) {
		if (address == null) throw new IllegalArgumentException("Address required");
		if (!addresses.contains(address)) {
			addresses.add(address);
		}
	}

	public List<Address> getAddresses() {
		return Collections.unmodifiableList(addresses);
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public String getMobileNumber() { return mobileNumber; }
	public BigDecimal getWalletBalance() { return walletBalance; }
	public int getCompletedOrderCount() { return completedOrderCount; }

	/** Same customer = same id (Part C.7). */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Customer other)) return false;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return name + " (" + id + ") — " + getLoyaltyTier() + ", wallet " + Money.format(walletBalance);
	}
}
