package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import arsi.dev.kriptofoni.Models.CandleStickChartEntryModel;
import arsi.dev.kriptofoni.Models.LineChartEntryModel;

public class FullScreenChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private CandleStickChart candleStickChart;
    private ArrayList<LineChartEntryModel> lineChartEntryModels;
    private ArrayList<CandleStickChartEntryModel> candleStickChartEntryModels;
    private ArrayList<String> timestamps;
    private String time, type;
    private ImageView backButton, change;
    private int chartColor, textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_chart);

        textColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        lineChart = findViewById(R.id.full_screen_chart_line_chart);
        candleStickChart = findViewById(R.id.full_screen_chart_candlestick_chart);
        backButton = findViewById(R.id.full_screen_chart_back_button);
        change = findViewById(R.id.full_screen_chart_change_chart);

        Intent intent = getIntent();

        time = intent.getStringExtra("time");
        type = intent.getStringExtra("type");
        lineChartEntryModels = intent.getParcelableArrayListExtra("lineChartModels");
        candleStickChartEntryModels = intent.getParcelableArrayListExtra("candleStickChartModels");
        timestamps = intent.getStringArrayListExtra("timestamps");
        chartColor = intent.getIntExtra("color", textColor);

        if (type.equals("line") && lineChartEntryModels != null) {
            change.setImageResource(R.drawable.ic_candlestickchart);
            getLineChart();
        } else if (type.equals("candleStick") && candleStickChartEntryModels != null) {
            change.setImageResource(R.drawable.ic_linechart);
            getCandleStickChart();
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals("line")) {
                    type = "candleStick";
                    change.setImageResource(R.drawable.ic_linechart);
                    getCandleStickChart();
                } else {
                    type = "line";
                    change.setImageResource(R.drawable.ic_candlestickchart);
                    getLineChart();
                }
            }
        });
    }

    private void getCandleStickChart() {
        lineChart.setVisibility(View.GONE);
        candleStickChart.setVisibility(View.VISIBLE);

        int red = Color.parseColor("#f6465d");
        int green = Color.parseColor("#2ebd85");
        int defaultColor = ContextCompat.getColor(getApplicationContext(), R.color.textColor);

        candleStickChart.setDragEnabled(true);
        candleStickChart.setScaleEnabled(true);
        candleStickChart.setDescription(null);

        ArrayList<CandleEntry> candleEntries = new ArrayList<>();

        for (CandleStickChartEntryModel model : candleStickChartEntryModels) {
            candleEntries.add(new CandleEntry(model.getIndex(), model.getHigh(), model.getLow(), model.getOpen(), model.getClose()));
        }

        XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setTextColor(defaultColor);
        YAxis yAxisRight = candleStickChart.getAxisRight();
        candleStickChart.getAxisLeft().setTextColor(defaultColor);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String result = "";
                int v = (int) value;
                if (v < timestamps.size()) {
                    if (time.equals("oneDay")) {
                        result = getChartXAxisHourAndMinute(Float.parseFloat(timestamps.get(v)));
                    } else if (time.equals("allTime")) {
                        result = getChartXAxisYears(Float.parseFloat(timestamps.get(v)));
                    } else {
                        result = getChartXAxisDayAndMonth(Float.parseFloat(timestamps.get(v)));
                    }
                }

                return result;
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisRight.setEnabled(false);
        candleStickChart.getLegend().setEnabled(false);

        CandleDataSet cds = new CandleDataSet(candleEntries, "Fiyatlar");
        cds.setAxisDependency(YAxis.AxisDependency.LEFT);
        cds.setShadowWidth(0.8f);
        cds.setBarSpace(0.15f);
        cds.setShadowColorSameAsCandle(true);
        cds.setDecreasingColor(red);
        cds.setDecreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        cds.setIncreasingColor(green);
        cds.setIncreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        cds.setNeutralColor(defaultColor);
        cds.setDrawValues(false);

        CandleData cd = new CandleData(cds);

        candleStickChart.setData(cd);
        candleStickChart.notifyDataSetChanged();
        candleStickChart.invalidate();
    }

    private void getLineChart() {
        candleStickChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);

        ArrayList<Entry> yValue = new ArrayList<>();

        for (LineChartEntryModel model : lineChartEntryModels) {
            yValue.add(new Entry(model.getIndex(), model.getValue()));
        }

        if (lineChart.getData() != null) {
            lineChart.clearValues();
            lineChart.notifyDataSetChanged();
        }

        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(null);
        lineChart.getLegend().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(textColor);
        YAxis yAxisRight = lineChart.getAxisRight();
        lineChart.getAxisLeft().setTextColor(textColor);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String result = "";
                if (time.equals("oneDay")) {
                    result = getChartXAxisHourAndMinute(value);
                } else if (time.equals("allTime")) {
                    result = getChartXAxisYears(value);
                } else {
                    result = getChartXAxisDayAndMonth(value);
                }
                return result;
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisRight.setEnabled(false);

        LineDataSet set = new LineDataSet(yValue, "Prices");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(0f);
        set.setLineWidth(2f);
        set.setDrawFilled(true);
        set.setColor(chartColor);
        set.setFillColor(chartColor);
        set.setFillAlpha(170);
        set.setValueTextColor(textColor);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private String getChartXAxisHourAndMinute(float timestamp) {
        Date date = new Date((long) timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String result = "";
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hour < 10) {
            if (minute < 10) {
                result = 0 + "" + hour + ":" + 0 + "" + minute;
            } else {
                result = 0 + "" + hour + ":" + minute;
            }
        } else {
            if (minute < 10)  {
                result = hour + ":" + 0 + "" + minute;
            } else {
                result = hour + ":" + minute;
            }
        }
        return result;
    }

    private String getChartXAxisDayAndMonth(float timestamp) {
        Date date = new Date((long) timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        String result = "";
        if (day < 10) {
            if (month < 10) {
                result = 0 + "" + day + "/" + 0 + "" + month;
            } else {
                result = 0 + "" + day + "/" + month;
            }
        } else {
            if (month < 10) {
                result = day + "/" + 0 + "" + month;
            } else {
                result = day + "/" + month;
            }
        }
        return result;
    }

    private String getChartXAxisYears(float timestamp) {
        Date date = new Date((long) timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.valueOf(calendar.get(Calendar.YEAR));
    }
}