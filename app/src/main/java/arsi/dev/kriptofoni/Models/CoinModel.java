package arsi.dev.kriptofoni.Models;

public class CoinModel {

    private String name, imageUri, shortCut;
    private int number;
    private double changeIn24Hours, changeIn7Days, currentPrice;

    public CoinModel(int number, String imageUri, String name, String shortCut, double changeIn24Hours, double changeIn7Days, double currentPrice) {
        this.number = number;
        this.imageUri = imageUri;
        this.name = name;
        this.shortCut = shortCut;
        this.changeIn24Hours = changeIn24Hours;
        this.changeIn7Days = changeIn7Days;
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

    public double getChangeIn7Days() {
        return changeIn7Days;
    }

    public void setChangeIn7Days(double changeIn7Days) {
        this.changeIn7Days = changeIn7Days;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
}
