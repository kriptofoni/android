package arsi.dev.kriptofoni.Models;

public class PortfolioMemoryModel {

    private double quantity, timestamp, price, fee;
    private String notes, shortCut, type, currency, id;

    public PortfolioMemoryModel(double quantity, double timestamp, double price, double fee, String notes, String shortCut, String type, String currency, String id) {
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.price = price;
        this.fee = fee;
        this.notes = notes;
        this.shortCut = shortCut;
        this.type = type;
        this.currency = currency;
        this.id = id;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getPrice() {
        return price;
    }

    public double getFee() {
        return fee;
    }

    public String getNotes() {
        return notes;
    }

    public String getShortCut() {
        return shortCut;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }
}
