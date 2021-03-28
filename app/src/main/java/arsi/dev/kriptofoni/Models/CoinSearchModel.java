package arsi.dev.kriptofoni.Models;

public class CoinSearchModel {

    private String id, name, symbol, image;
    private int number;

    public CoinSearchModel(int number, String id, String name, String symbol, String image) {
        this.number = number;
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.image = image;
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
}
