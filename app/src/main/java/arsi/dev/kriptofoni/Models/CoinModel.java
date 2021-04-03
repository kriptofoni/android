package arsi.dev.kriptofoni.Models;

public class CoinModel {

    private String name, imageUri, shortCut, id;
    private int number;
    private double changeIn24Hours, priceChangeIn24Hours, changeIn7Days, currentPrice, marketCap, priceChangeIn7Days;

    public CoinModel(int number, String imageUri, String name, String shortCut, double changeIn24Hours, double priceChangeIn24Hours, double currentPrice, double marketCap, double changeIn7Days, String id, double priceChangeIn7Days) {
        this.number = number;
        this.imageUri = imageUri;
        this.name = name;
        this.shortCut = shortCut;
        this.changeIn24Hours = changeIn24Hours;
        this.priceChangeIn24Hours = priceChangeIn24Hours;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.changeIn7Days = changeIn7Days;
        this.id = id;
        this.priceChangeIn7Days = priceChangeIn7Days;
    }

    public CoinModel(int number, String imageUri, String name, String shortCut, double changeIn24Hours, double priceChangeIn24Hours, double currentPrice) {
        this.name = name;
        this.imageUri = imageUri;
        this.shortCut = shortCut;
        this.number = number;
        this.changeIn24Hours = changeIn24Hours;
        this.priceChangeIn24Hours = priceChangeIn24Hours;
        this.currentPrice = currentPrice;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
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

    public double getChangeIn24Hours() {
        return changeIn24Hours;
    }

    public void setChangeIn24Hours(double changeIn24Hours) {
        this.changeIn24Hours = changeIn24Hours;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPriceChangeIn24Hours() {
        return priceChangeIn24Hours;
    }

    public void setPriceChangeIn24Hours(double priceChangeIn24Hours) {
        this.priceChangeIn24Hours = priceChangeIn24Hours;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }

    public double getPriceChangeIn7Days() {
        return priceChangeIn7Days;
    }

    public void setPriceChangeIn7Days(double priceChangeIn7Days) {
        this.priceChangeIn7Days = priceChangeIn7Days;
    }

    public double getChangeIn7Days() {
        return changeIn7Days;
    }

    public void setChangeIn7Days(double changeIn7Days) {
        this.changeIn7Days = changeIn7Days;
    }
}
