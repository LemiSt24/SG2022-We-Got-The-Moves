package com.sg2022.we_got_the_moves.ui.statistics;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsBinding;

import java.util.ArrayList;

public class StatisticsFragment extends Fragment {

  private FragmentStatisticsBinding binding;

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    StatisticsViewModel StatisticsViewModel =
        new ViewModelProvider(this).get(StatisticsViewModel.class);

    binding = FragmentStatisticsBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    buildBarChart(binding.barChart);

    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @SuppressLint("ResourceAsColor")
  public void buildBarChart(BarChart barChart){
    ArrayList<Integer> trainings = new ArrayList<Integer>();
    trainings.add(0);
    trainings.add(10);
    trainings.add(10);
    trainings.add(5);
    trainings.add(0);
    trainings.add(1);
    trainings.add(7);

    final ArrayList<String> xAxisLabel = new ArrayList<>();
    xAxisLabel.add("MON"); //this label will be mapped to the 1st index of the valuesList
    xAxisLabel.add("TUE");
    xAxisLabel.add("WED");
    xAxisLabel.add("THU");
    xAxisLabel.add("FRI");
    xAxisLabel.add("SAT");
    xAxisLabel.add("SUN");
    xAxisLabel.add("bla");

    ArrayList<BarEntry> entries = new ArrayList<>();
    for (int i = 0; i < trainings.size(); i++){
      entries.add(new BarEntry(i+1, trainings.get(i)));
    }

    XAxis xAxis = barChart.getXAxis();
    xAxis.enableGridDashedLine(10f, 10f, 0f);
    xAxis.setTextColor(R.color.black);
    //xAxis.setTextSize(14);
    xAxis.setDrawAxisLine(true);
    xAxis.setAxisLineColor(R.color.black);
    xAxis.setDrawGridLines(true);
    xAxis.setAxisMinimum(0 + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
    xAxis.setAxisMaximum(entries.size() + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
    xAxis.setLabelCount(xAxisLabel.size(), true); //draw x labels for 13 vertical grid lines
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setXOffset(0f); //labels x offset in dps
    xAxis.setYOffset(0f); //labels y offset in dps
    xAxis.setCenterAxisLabels(true);
    xAxis.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return xAxisLabel.get((int) value);
      }
    });

    //initialize Y-Right-Axis
    YAxis rightAxis = barChart.getAxisRight();
    rightAxis.setTextColor(R.color.black);
    rightAxis.setTextSize(14);
    rightAxis.setDrawAxisLine(true);
    rightAxis.setAxisLineColor(R.color.black);
    rightAxis.setDrawGridLines(true);
    rightAxis.setGranularity(1f);
    rightAxis.setGranularityEnabled(true);
    rightAxis.setAxisMinimum(0);
    //rightAxis.setLabelCount(4, true); //draw y labels (Y-Values) for 4 horizontal grid lines starting from 0 to 6000f (step=2000)
    rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

    //initialize Y-Left-Axis
    YAxis leftAxis = barChart.getAxisLeft();
    leftAxis.setAxisMinimum(0);
    leftAxis.setDrawAxisLine(true);
    leftAxis.setLabelCount(0, true);
    leftAxis.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return "";
      }
    });


    BarDataSet barDataSet = new BarDataSet(entries, "Trainings");
    barDataSet.setColor(R.color.sg_design_green);

    BarData data = new BarData(barDataSet);
    barChart.setData(data);
    barChart.setScaleEnabled(false);
    barChart.getLegend().setEnabled(false);
    barChart.setDrawBarShadow(false);
    barChart.getDescription().setEnabled(false);
    barChart.setPinchZoom(false);
    barChart.setDrawGridBackground(true);
    barChart.invalidate();
  }
}
