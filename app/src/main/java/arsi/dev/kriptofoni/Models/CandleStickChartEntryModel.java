package arsi.dev.kriptofoni.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class CandleStickChartEntryModel implements Parcelable {

    private float index, high, low, open, close;

    public CandleStickChartEntryModel(float index, float high, float low, float open, float close) {
        this.index = index;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
    }

    protected CandleStickChartEntryModel(Parcel in) {
        index = in.readFloat();
        high = in.readFloat();
        low = in.readFloat();
        open = in.readFloat();
        close = in.readFloat();
    }

    public static final Creator<CandleStickChartEntryModel> CREATOR = new Creator<CandleStickChartEntryModel>() {
        @Override
        public CandleStickChartEntryModel createFromParcel(Parcel in) {
            return new CandleStickChartEntryModel(in);
        }

        @Override
        public CandleStickChartEntryModel[] newArray(int size) {
            return new CandleStickChartEntryModel[size];
        }
    };

    public float getIndex() {
        return index;
    }

    public float getHigh() {
        return high;
    }

    public float getLow() {
        return low;
    }

    public float getOpen() {
        return open;
    }

    public float getClose() {
        return close;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(index);
        parcel.writeFloat(high);
        parcel.writeFloat(low);
        parcel.writeFloat(open);
        parcel.writeFloat(close);
    }
}
