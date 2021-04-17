package arsi.dev.kriptofoni.Models;

public class WatchingListModel {

    private String id, name, icon;
    private double priceChangeIn24Hours, price;

    public WatchingListModel(String id, String name, String icon, double priceChangeIn24Hours, double price) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.priceChangeIn24Hours = priceChangeIn24Hours;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public double getPriceChangeIn24Hours() {
        return priceChangeIn24Hours;
    }

    public double getPrice() {
        return price;
    }
}
