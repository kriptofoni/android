package arsi.dev.kriptofoni.Pickers;

public class CountryCodePicker {

    public String[] getCountryCode(String currencyCode) {
        String[] resultArr = new String[2];
        String countryCode = "";
        String currencySymbol = "";
        switch (currencyCode) {
            case "usd":
                countryCode = "us";
                currencySymbol = "$";
                break;
            case "aed":
                countryCode = "ae";
                currencySymbol = "د.إ";
                break;
            case "ars":
                countryCode = "ar";
                currencySymbol = "$";
                break;
            case "aud":
                countryCode = "au";
                currencySymbol = "$";
                break;
            case "bdt":
                countryCode = "bd";
                currencySymbol = "$";
                break;
            case "bhd":
                countryCode = "bh";
                currencySymbol = "ó";
                break;
            case "bmd":
                countryCode = "bm";
                currencySymbol = "$";
                break;
            case "brl":
                countryCode = "br";
                currencySymbol = "R";
                break;
            case "cad":
                countryCode = "ca";
                currencySymbol = "$";
                break;
            case "chf":
                countryCode = "li";
                currencySymbol = "Fr";
                break;
            case "clp":
                countryCode = "cl";
                currencySymbol = "$";
                break;
            case "cny":
                countryCode = "cn";
                currencySymbol = "¥";
                break;
            case "czk":
                countryCode = "cz";
                currencySymbol = "Kč";
                break;
            case "dkk":
                countryCode = "dk";
                currencySymbol = "kr";
                break;
            case "eur":
                countryCode = "eu";
                currencySymbol = "€";
                break;
            case "gbp":
                countryCode = "gb";
                currencySymbol = "£";
                break;
            case "hkd":
                countryCode = "hk";
                currencySymbol = "$";
                break;
            case "huf":
                countryCode = "hu";
                currencySymbol = "Ft";
                break;
            case "idr":
                countryCode = "id";
                currencySymbol = "Rp";
                break;
            case "ils":
                countryCode = "il";
                currencySymbol = "₪";
                break;
            case "inr":
                countryCode = "in";
                currencySymbol = "₹";
                break;
            case "jpy":
                countryCode = "jp";
                currencySymbol = "¥";
                break;
            case "krw":
                countryCode = "kr";
                currencySymbol = "₩";
                break;
            case "kwd":
                countryCode = "kw";
                currencySymbol = "د";
                break;
            case "lkr":
                countryCode = "lk";
                currencySymbol = "Rs";
                break;
            case "mmk":
                countryCode = "mm";
                currencySymbol = "K";
                break;
            case "mxn":
                countryCode = "mx";
                currencySymbol = "$";
                break;
            case "myr":
                countryCode = "my";
                currencySymbol = "RM";
                break;
            case "ngn":
                countryCode = "ng";
                currencySymbol = "₦";
                break;
            case "nok":
                countryCode = "no";
                currencySymbol = "kr";
                break;
            case "nzd":
                countryCode = "nz";
                currencySymbol = "$";
                break;
            case "php":
                countryCode = "ph";
                currencySymbol = "₱";
                break;
            case "pkr":
                countryCode = "pk";
                currencySymbol = "₨";
                break;
            case "pln":
                countryCode = "pl";
                currencySymbol = "zł";
                break;
            case "rub":
                countryCode = "ru";
                currencySymbol = "₽";
                break;
            case "sar":
                countryCode = "sa";
                currencySymbol = "﷼";
                break;
            case "sek":
                countryCode = "se";
                currencySymbol = "kr";
                break;
            case "sgd":
                countryCode = "sg";
                currencySymbol = "$";
                break;
            case "thb":
                countryCode = "th";
                currencySymbol = "฿";
                break;
            case "try":
                countryCode = "tr";
                currencySymbol = "₺";
                break;
            case "twd":
                countryCode = "tw";
                currencySymbol = "$";
                break;
            case "uah":
                countryCode = "ua";
                currencySymbol = "₴";
                break;
            case "vef":
                countryCode = "ve";
                currencySymbol = "Bs";
                break;
            case "vnd":
                countryCode = "vn";
                currencySymbol = "₫";
                break;
            case "zar":
                countryCode = "za";
                currencySymbol = "R";
                break;
            default:
                countryCode = "";
                currencySymbol = "";
                break;
        }
        resultArr[0] = countryCode;
        resultArr[1] = currencySymbol;
        return resultArr;
    }


}
