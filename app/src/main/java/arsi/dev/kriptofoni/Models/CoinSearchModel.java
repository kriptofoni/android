package arsi.dev.kriptofoni.Models;

public class CoinSearchModel {

    private String id, name, symbol, image;
    private int number;
    private double priceChangeIn24, priceChangeIn7, marketCap, marketCapRank, currentPrice, priceIn24;

    public CoinSearchModel(double marketCapRank, String id, String name, String symbol, double marketCap, String image, double priceChangeIn24, double priceChangeIn7, int number, double currentPrice, double priceIn24) {
        this.marketCapRank = marketCapRank == 0 ? 10000 : marketCapRank;
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.image = image;
        this.marketCap = marketCap;
        this.priceChangeIn24 = priceChangeIn24;
        this.number = number;
        this.priceChangeIn7 = priceChangeIn7;
        this.currentPrice = currentPrice;
        this.priceIn24 = priceIn24;
    }

    public CoinSearchModel(int number, String id, String name, String symbol, String image, double priceChangeIn24) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.image = image;
        this.number = number;
        this.priceChangeIn24 = priceChangeIn24;
    }

    public CoinSearchModel(String id, String name, String symbol, String image, double priceChangeIn7, int number) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.image = image;
        this.number = number;
        this.priceChangeIn7 = priceChangeIn7;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getImage() {
        return image;
    }

    public double getPriceChangeIn24() {
        return priceChangeIn24;
    }

    public void setPriceChangeIn24(double priceChangeIn24) {
        this.priceChangeIn24 = priceChangeIn24;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public double getMarketCapRank() {
        return marketCapRank;
    }

    public void setMarketCapRank(double marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }

    public double getPriceChangeIn7() {
        return priceChangeIn7;
    }

    public void setPriceChangeIn7(double priceChangeIn7) {
        this.priceChangeIn7 = priceChangeIn7;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPriceIn24() {
        return priceIn24;
    }

    public void setPriceIn24(double priceIn24) {
        this.priceIn24 = priceIn24;
    }
}
