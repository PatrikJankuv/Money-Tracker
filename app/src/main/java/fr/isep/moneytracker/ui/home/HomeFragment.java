package fr.isep.moneytracker.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import java.util.Date;

import fr.isep.moneytracker.R;
import fr.isep.moneytracker.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView datePicker;
    private Spinner categorySpinner;
    private Button cancelButton;
    private Button saveButton;
    private DatePickerDialog.OnDateSetListener setListener;
    private Calendar calendar = Calendar.getInstance();
    private int day = -1;
    private int month = -1;
    private int year = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewRecord();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void createNewRecord() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View recordPopUp = getLayoutInflater().inflate(R.layout.add_record, null);

        categorySpinner = (Spinner) recordPopUp.findViewById(R.id.categories);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        datePicker = (TextView) recordPopUp.findViewById(R.id.date_picker);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        setListener, getYear(), getMonth(), getDay());
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yearView, int monthView, int dayView) {
                day = dayView;
                month = monthView;
                year = yearView;
                month++;
                String date = day + "/" + month + "/" + year;
                datePicker.setText(date);
            }
        };

        dialogBuilder.setView(recordPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        cancelButton = (Button) recordPopUp.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private int getDay(){
        return day == -1 ? calendar.get(Calendar.DAY_OF_MONTH) : day;
    }

    private int getMonth(){
        return month == -1 ? calendar.get(Calendar.MONTH) : month;
    }

    private int getYear(){
        return year == -1 ? calendar.get(Calendar.YEAR) : year;
    }
}