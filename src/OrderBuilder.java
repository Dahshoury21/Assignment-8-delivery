import exception.ItemUnavailableException;
import exception.RestaurantClosedException;
import exception.StockShortageException;

import java.util.ArrayList;
import java.util.List;

/** Builder — avoids a constructor with mostly-null optional params (Part G.2). */
public class OrderBuilder {
	private String id;
	private Customer customer;
	private Restaurant restaurant;
	private Address deliveryAddress;
	private final List<LineItem> lineItems = new ArrayList<>();
	private String deliveryNotes;
	private PromotionStrategy promotion;

	public OrderBuilder id(String id) {
		this.id = id;
		return this;
	}

	public OrderBuilder customer(Customer customer) {
		this.customer = customer;
		return this;
	}

	public OrderBuilder restaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
		return this;
	}

	public OrderBuilder deliveryAddress(Address deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
		return this;
	}

	public OrderBuilder deliveryNotes(String notes) {
		this.deliveryNotes = notes;
		return this;
	}

	public OrderBuilder promotion(PromotionStrategy promotion) {
		this.promotion = promotion;
		return this;
	}

	public OrderBuilder addLineItem(MenuItem item, double quantityOrWeight)
			throws ItemUnavailableException {
		if (item == null) throw new IllegalArgumentException("Menu item required");
		if (!item.isAvailable()) {
			throw new ItemUnavailableException("Item '" + item.getName() + "' is unavailable.");
		}
		lineItems.add(new LineItem(item, quantityOrWeight));
		return this;
	}

	public Order build() throws RestaurantClosedException, ItemUnavailableException, StockShortageException {
		if (id == null || id.isBlank()) {
			throw new IllegalStateException("Order id is required");
		}
		if (customer == null) throw new IllegalStateException("Customer is required");
		if (restaurant == null) throw new IllegalStateException("Restaurant is required");
		if (deliveryAddress == null) throw new IllegalStateException("Delivery address is required");
		if (lineItems.isEmpty()) throw new IllegalStateException("Order must contain at least one line item");
		if (!restaurant.isOpen()) {
			throw new RestaurantClosedException("Restaurant '" + restaurant.getName() + "' is closed.");
		}
		if (!customer.ownsAddress(deliveryAddress)) {
			throw new IllegalStateException("Delivery address must belong to the customer placing the order.");
		}
		// Atomic stock check + consume after all other validations
		for (LineItem line : lineItems) {
			if (!line.menuItem().isAvailable()) {
				throw new ItemUnavailableException(
						"Item '" + line.menuItem().getName() + "' became unavailable.");
			}
		}
		List<LineItem> consumed = new ArrayList<>();
		try {
			for (LineItem line : lineItems) {
				line.menuItem().consumeStock(line.stockUnits());
				consumed.add(line);
			}
		} catch (StockShortageException ex) {
			for (LineItem line : consumed) {
				line.menuItem().restoreStock(line.stockUnits());
			}
			throw ex;
		}
		return new Order(id, customer, restaurant, deliveryAddress, lineItems, deliveryNotes, promotion);
	}
}
