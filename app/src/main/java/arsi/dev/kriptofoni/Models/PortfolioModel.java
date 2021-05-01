package arsi.dev.kriptofoni.Models;

public class PortfolioModel {

    private String shortCut, image;
    private double ownedPrice, priceChange24Hours, priceChangePercentage24Hours, currentPrice, quantity;

    public PortfolioModel(String shortCut, String image, double ownedPrice, double priceChange24Hours, double priceChangePercentage24Hours, double currentPrice, double quantity) {
        this.shortCut = shortCut;
        this.image = image;
        this.ownedPrice = ownedPrice;
        this.priceChange24Hours = priceChange24Hours;
        this.priceChangePercentage24Hours = priceChangePercentage24Hours;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
    }

    public String getShortCut() {
        return shortCut;
    }

    public String getImage() {
        return image;
    }

    public double getOwnedPrice() {
        return ownedPrice;
    }

    public double getPriceChange24Hours() {
        return priceChange24Hours;
    }

    public double getPriceChangePercentage24Hours() {
        return priceChangePercentage24Hours;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setOwnedPrice(double ownedPrice) {
        this.ownedPrice = ownedPrice;
    }
}
