import exception.InvalidOrderTransitionException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {
	PLACED,
	ACCEPTED,
	PREPARING,
	READY,
	ASSIGNED,
	OUT_FOR_DELIVERY,
	DELIVERED,
	CANCELLED;

	private static final Map<OrderStatus, Set<OrderStatus>> LEGAL = new EnumMap<>(OrderStatus.class);

	static {
		LEGAL.put(PLACED, EnumSet.of(ACCEPTED, CANCELLED));
		LEGAL.put(ACCEPTED, EnumSet.of(PREPARING, CANCELLED));
		LEGAL.put(PREPARING, EnumSet.of(READY, CANCELLED));
		LEGAL.put(READY, EnumSet.of(ASSIGNED, CANCELLED));
		LEGAL.put(ASSIGNED, EnumSet.of(OUT_FOR_DELIVERY, CANCELLED));
		LEGAL.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));
		LEGAL.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
		LEGAL.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
	}

	public boolean canTransitionTo(OrderStatus next) {
		return LEGAL.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(next);
	}

	public void assertCanTransitionTo(OrderStatus next) {
		if (!canTransitionTo(next)) {
			throw new InvalidOrderTransitionException(
					"Illegal transition from " + this + " to " + next);
		}
	}

	public boolean isCancellable() {
		return this != OUT_FOR_DELIVERY && this != DELIVERED && this != CANCELLED;
	}

	public boolean isTerminal() {
		return this == DELIVERED || this == CANCELLED;
	}
}
