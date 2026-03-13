package com.furniturevision.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Represents a complete room design, including the room specifications
 * and all placed furniture items. This is the main document that
 * designers save and manage.
 */
public class Design implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String designerUsername;
    private String customerName;
    private Room room;
    private List<FurnitureItem> furnitureItems;
    private Date createdDate;
    private Date modifiedDate;
    private String notes;

    public Design() {
        this.id = UUID.randomUUID().toString();
        this.furnitureItems = new ArrayList<>();
        this.createdDate = new Date();
        this.modifiedDate = new Date();
        this.notes = "";
    }

    public Design(String designerUsername, String customerName, Room room) {
        this();
        this.designerUsername = designerUsername;
        this.customerName = customerName;
        this.room = room;
    }

    // --- Furniture Management ---
    public void addFurniture(FurnitureItem item) {
        furnitureItems.add(item);
        touch();
    }

    public void removeFurniture(FurnitureItem item) {
        furnitureItems.remove(item);
        touch();
    }

    public void removeFurnitureById(String itemId) {
        furnitureItems.removeIf(item -> item.getId().equals(itemId));
        touch();
    }

    public FurnitureItem getFurnitureAt(double x, double y) {
        // Search in reverse order (top items first)
        for (int i = furnitureItems.size() - 1; i >= 0; i--) {
            FurnitureItem item = furnitureItems.get(i);
            if (item.containsPoint(x, y)) {
                return item;
            }
        }
        return null;
    }

    public void clearSelection() {
        for (FurnitureItem item : furnitureItems) {
            item.setSelected(false);
        }
    }

    /**
     * Updates the modified date.
     */
    public void touch() {
        this.modifiedDate = new Date();
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDesignerUsername() { return designerUsername; }
    public void setDesignerUsername(String designerUsername) { this.designerUsername = designerUsername; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public List<FurnitureItem> getFurnitureItems() { return furnitureItems; }
    public void setFurnitureItems(List<FurnitureItem> furnitureItems) { this.furnitureItems = furnitureItems; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Date modifiedDate) { this.modifiedDate = modifiedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getFormattedCreatedDate() {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm").format(createdDate);
    }

    public String getFormattedModifiedDate() {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm").format(modifiedDate);
    }

    @Override
    public String toString() {
        return (customerName != null ? customerName + " - " : "") + room.getName();
    }
}
