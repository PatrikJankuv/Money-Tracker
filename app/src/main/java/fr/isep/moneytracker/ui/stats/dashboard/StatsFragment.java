package fr.isep.moneytracker.ui.stats.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import fr.isep.moneytracker.R;
import fr.isep.moneytracker.databinding.FragmentStatsBinding;
import fr.isep.moneytracker.model.Record;
import fr.isep.moneytracker.model.User;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private LineChart chart;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatsViewModel dashboardViewModel =
                new ViewModelProvider(this).get(StatsViewModel.class);

        binding = FragmentStatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        chart = (LineChart) binding.balanceChart;
        user = User.first(User.class);

        getBalanceStats();
        return root;
    }

    private void getBalanceStats(){
        List<Record> recordsList = Record.findWithQuery(Record.class, "Select * from Record order by date desc");
        List<String> dates = new ArrayList<>();
        List<Entry> entries = new ArrayList<Entry>();
        int x = 0;
        Double balance = user.getBalance();
        String tempDate = getToday();
        dates.add(tempDate);

        for(Record r: recordsList){
            if(r.getDate().equals(tempDate)){
                balance -= r.getAmount();
            }else{
                entries.add(new Entry(x++, balance.floatValue()));
                balance -= r.getAmount();
                dates.add(r.getDate());
            }
        }

        dates.add(recordsList.get(recordsList.size()-1).getDate());
        entries.add(new Entry(x, balance.floatValue()));

        LineDataSet dataSet = new LineDataSet(entries, "Balance");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setFillAlpha(100);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(R.color.food_category);
        LineData lineData = new LineData(dataSet);

        // the labels that should be drawn on the XAxis
        final String[] quarters = dates.toArray(new String[0]);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return quarters[(int) value];
            }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        chart.setAutoScaleMinMaxEnabled(true);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    private String getToday(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format("%02d", year) + "/" + String.format("%02d", month) + "/" + String.format("%02d", day);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}