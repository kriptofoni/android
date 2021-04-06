package arsi.dev.kriptofoni.Models;

public class CandleStickChartEntryModel {

    private long index, value;

    public CandleStickChartEntryModel(long index, long value) {
        this.index = index;
        this.value = value;
    }

    public long getIndex() {
        return index;
    }

    public long getValue() {
        return value;
    }
}
