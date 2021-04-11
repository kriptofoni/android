package arsi.dev.kriptofoni;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import arsi.dev.kriptofoni.Models.LineChartEntryModel;

public class FullScreenChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private CandleStickChart candleStickChart;
    private ArrayList<LineChartEntryModel> lineChartEntryModels;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_chart);

        lineChart = findViewById(R.id.full_screen_chart_line_chart);
        candleStickChart = findViewById(R.id.full_screen_chart_candlestick_chart);

        Intent intent = getIntent();

        time = intent.getStringExtra("time");
        String type = intent.getStringExtra("type");
        lineChartEntryModels = intent.getParcelableArrayListExtra("lineChartModels");

        if (type.equals("line") && lineChartEntryModels != null)
            getLineChart();
        else
            getCandleStickChart();

    }

    private void getCandleStickChart() {
        lineChart.setVisibility(View.GONE);
        candleStickChart.setVisibility(View.VISIBLE);
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

        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxisRight = lineChart.getAxisRight();
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
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setDrawAxisLine(false);
        yAxisRight.setEnabled(false);

        LineDataSet set = new LineDataSet(yValue, "");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(0f);
        set.setLineWidth(2f);
        set.setColor(Color.RED);
        set.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
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