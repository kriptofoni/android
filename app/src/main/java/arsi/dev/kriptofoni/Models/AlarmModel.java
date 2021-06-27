package arsi.dev.kriptofoni.Models;

public class AlarmModel {

    private String name, shortCut, image, id, currency;
    private double price;
    private boolean isSmaller;

    public AlarmModel(String id, String name, String shortCut, String image, double price, boolean isSmaller, String currency) {
        this.id = id;
        this.name = name;
        this.shortCut = shortCut;
        this.image = image;
        this.price = price;
        this.isSmaller = isSmaller;
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortCut() {
        return shortCut;
    }

    public void setShortCut(String shortCut) {
        this.shortCut = shortCut;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isSmaller() {
        return isSmaller;
    }

    public void setSmaller(boolean smaller) {
        isSmaller = smaller;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
