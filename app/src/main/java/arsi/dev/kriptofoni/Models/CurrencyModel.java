package arsi.dev.kriptofoni.Models;

public class CurrencyModel {

    private String name, icon;
    private Boolean choosen;

    public CurrencyModel(String name, String icon, Boolean choosen) {
        this.name = name;
        this.icon = icon;
        this.choosen = choosen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getChoosen() {
        return choosen;
    }

    public void setChoosen(Boolean choosen) {
        this.choosen = choosen;
    }
}
