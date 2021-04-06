package arsi.dev.kriptofoni.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class LineChartEntryModel implements Parcelable {

    private float index, value;

    public LineChartEntryModel(float index, float value) {
        this.index = index;
        this.value = value;
    }

    protected LineChartEntryModel(Parcel in) {
        index = in.readFloat();
        value = in.readFloat();
    }

    public static final Creator<LineChartEntryModel> CREATOR = new Creator<LineChartEntryModel>() {
        @Override
        public LineChartEntryModel createFromParcel(Parcel in) {
            return new LineChartEntryModel(in);
        }

        @Override
        public LineChartEntryModel[] newArray(int size) {
            return new LineChartEntryModel[size];
        }
    };

    public float getIndex() {
        return index;
    }

    public float getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(index);
        parcel.writeFloat(value);
    }
}
