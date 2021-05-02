package arsi.dev.kriptofoni.Models;

public class PortfolioModel {

    private String shortCut, image;
    private double totalPrice, priceChange24Hours, priceChangePercentage24Hours, currentPrice, quantity;

    public PortfolioModel(String shortCut, String image, double totalPrice, double priceChange24Hours, double priceChangePercentage24Hours, double currentPrice, double quantity) {
        this.shortCut = shortCut;
        this.image = image;
        this.priceChange24Hours = priceChange24Hours;
        this.priceChangePercentage24Hours = priceChangePercentage24Hours;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public String getShortCut() {
        return shortCut;
    }

    public String getImage() {
        return image;
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

    public double getTotalPrice() {
        return totalPrice;
    }
}
