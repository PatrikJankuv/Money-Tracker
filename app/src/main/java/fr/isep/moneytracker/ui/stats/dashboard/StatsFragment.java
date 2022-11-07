package fr.isep.moneytracker.ui.stats.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.isep.moneytracker.R;
import fr.isep.moneytracker.databinding.FragmentStatsBinding;
import fr.isep.moneytracker.model.Record;
import fr.isep.moneytracker.model.User;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private LineChart balanceChart;
    private BarChart ieChart;
    private PieChart categoryChart;
    private User user;
    private Spinner graphSpinner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatsViewModel dashboardViewModel =
                new ViewModelProvider(this).get(StatsViewModel.class);

        binding = FragmentStatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        graphSpinner = binding.graphSpinner;
        ieChart = binding.ieChart;
        categoryChart = binding.categoryChart;
        balanceChart = (LineChart) binding.balanceChart;

        user = User.first(User.class);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.graphs_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpinner.setAdapter(adapter);

        graphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                balanceChart.setVisibility(View.GONE);
                ieChart.setVisibility(View.GONE);
                categoryChart.setVisibility(View.GONE);
                String selectedGraph = graphSpinner.getSelectedItem().toString();
                switch (selectedGraph){
                    case "Balance":
                        showBalanceGraph();
                        balanceChart.setVisibility(View.VISIBLE);
                        break;
                    case "Income and Expense":
                        showIncomeExpenseGraph();
                        ieChart.setVisibility(View.VISIBLE);
                        break;
                    case "Category expense":
                        showCategoryGraph();
                        categoryChart.setVisibility(View.VISIBLE);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid graph selection");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        return root;
    }

    private void showBalanceGraph(){
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
        XAxis xAxis = balanceChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        balanceChart.setAutoScaleMinMaxEnabled(true);
        balanceChart.setData(lineData);
        balanceChart.invalidate(); // refresh
    }

    private void showIncomeExpenseGraph(){
        List<Record> recordsList = Record.findWithQuery(Record.class, "Select * from Record order by date desc");
        List<String> months = new ArrayList<String>();
        Map<String, Double> incomeMonth = new HashMap<>();
        Map<String, Double> expenseMonth = new HashMap<>();

        for(Record record: recordsList){
            String recordMonth = extractMonth(record.getDate());

            if(!months.contains(recordMonth)){
                if(months.size() == 5){
                    break;
                }
                months.add(recordMonth);
                incomeMonth.put(recordMonth, 0.0);
                expenseMonth.put(recordMonth, 0.0);
            }

            double recordAmount = record.getAmount();
            if(recordAmount < 0){
                double temp = expenseMonth.get(recordMonth);
                temp -= recordAmount;
                expenseMonth.put(recordMonth, temp);
            }else{
                double temp = incomeMonth.get(recordMonth);
                temp += recordAmount;
                incomeMonth.put(recordMonth, temp);
            }
        }

        ArrayList incomeEntries = new ArrayList<>();
        ArrayList expenseEntries = new ArrayList<>();

        float i = 0;
        for(String m: months){
            ++i;
            incomeEntries.add(new BarEntry(i, incomeMonth.get(m).floatValue()));
            expenseEntries.add(new BarEntry(i, expenseMonth.get(m).floatValue()));
        }

        // variable for our bar data set.
        BarDataSet incomeDataSet, expenseDataSet;

        incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.GREEN);
        expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.RED);

        // below line is to add bar data set to our bar data.
        BarData data = new BarData(incomeDataSet, expenseDataSet);
        ieChart.setData(data);
        ieChart.getDescription().setEnabled(false);

        XAxis xAxis = ieChart.getXAxis();

        final String[] monthsArray = months.toArray(new String[0]);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthsArray));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        ieChart.setDragEnabled(true);
        ieChart.setVisibleXRangeMaximum(3);
        float barSpace = 0.1f;
        float groupSpace = 0.5f;
        data.setBarWidth(0.15f);

        ieChart.getXAxis().setAxisMinimum(0);
        ieChart.animate();
        ieChart.groupBars(0, groupSpace, barSpace);
        ieChart.invalidate();
    }

    private void showCategoryGraph(){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "type";
        String[] categories = new String[] {"Food", "Shopping", "Housing", "Transportation", "Entertainment", "Income", "Other"};

        Map<String, Integer> typeAmountMap = new HashMap<>();
        for(int i = 0; i < categories.length; i++){
            List<Record> recordsList = Record.findWithQuery(Record.class, "Select * from Record where category = ? and amount < 0", categories[i]);
            double temp = 0.0;
            for(Record r: recordsList){
                temp -= r.getAmount();
            }
            if(temp != 0.0) typeAmountMap.put(categories[i], (int)temp);
        }

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#304567"));
        colors.add(Color.parseColor("#309967"));
        colors.add(Color.parseColor("#476567"));
        colors.add(Color.parseColor("#890567"));
        colors.add(Color.parseColor("#a35567"));
        colors.add(Color.parseColor("#ff5f67"));
        colors.add(Color.parseColor("#3ca567"));

        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);

        categoryChart.setData(pieData);
        categoryChart.invalidate();
    }

    private String extractMonth(String date){
        return date.split("\\/")[1];
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