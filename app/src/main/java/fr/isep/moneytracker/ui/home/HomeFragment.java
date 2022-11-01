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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import fr.isep.moneytracker.R;
import fr.isep.moneytracker.databinding.FragmentHomeBinding;
import fr.isep.moneytracker.model.Record;
import fr.isep.moneytracker.model.User;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AlertDialog dialog;
    private TextView titleTextView;
    private EditText amountEditText;
    private EditText noteEditText;
    private RadioButton expenseRadioButton;
    private TextView datePicker;
    private Spinner categorySpinner;
    private DatePickerDialog.OnDateSetListener setListener;
    private final Calendar calendar = Calendar.getInstance();
    private int day = calendar.get(Calendar.DAY_OF_MONTH);
    private int month = calendar.get(Calendar.MONTH);
    private int year = calendar.get(Calendar.YEAR);
    private ArrayList<String> amountRecord, categoryRecord, dateRecord, descriptionRecord;
    private CustomAdapter customAdapter;
    private User user;

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

        user = User.first(User.class);
        refreshBalance(0.0);
        loadAllRecords();
        customAdapter = new CustomAdapter(getActivity(), descriptionRecord, dateRecord, amountRecord, categoryRecord);
        binding.recordsRecycledView.setAdapter(customAdapter);
        binding.recordsRecycledView.setLayoutManager(new LinearLayoutManager(getContext()));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadAllRecords(){
        amountRecord = new ArrayList<>();
        categoryRecord = new ArrayList<>();
        dateRecord = new ArrayList<>();
        descriptionRecord = new ArrayList<>();

       Record.listAll(Record.class).forEach(this::addRecordToList);
    }

    private void addRecordToList(Record record){
        amountRecord.add(0, String.valueOf(record.getAmount()));
        categoryRecord.add(0, record.getCategory());
        dateRecord.add(0, record.getDate());
        descriptionRecord.add(0, record.getDescription());
    }

    private void refreshBalance(Double amount){
        Double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);
        user.update();
        binding.balanceText.setText(String.valueOf(user.getBalance()) + " " + user.getCurrency());
    }

    public void createNewRecord() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        final View recordPopUp = getLayoutInflater().inflate(R.layout.add_record, null);

        titleTextView = (TextView) recordPopUp.findViewById(R.id.title);
        amountEditText = (EditText) recordPopUp.findViewById(R.id.amount);
        noteEditText = (EditText) recordPopUp.findViewById(R.id.note);
        expenseRadioButton = (RadioButton) recordPopUp.findViewById(R.id.expense);

        expenseRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleTextView.setBackgroundColor(getResources().getColor(R.color.expense_red));
            }
        });

        RadioButton incomeRadioButton = (RadioButton) recordPopUp.findViewById(R.id.income);
        incomeRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleTextView.setBackgroundColor(getResources().getColor(R.color.income_green));
            }
        });


        expenseRadioButton.setChecked(true);

        categorySpinner = (Spinner) recordPopUp.findViewById(R.id.categories);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        datePicker = (TextView) recordPopUp.findViewById(R.id.date_picker);
        datePicker.setText(getDay() + "." + getMonth() + "." + getYear());
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
                String date = day + "." + month + "." + year;
                datePicker.setText(date);
            }
        };

        dialogBuilder.setView(recordPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        Button cancelButton = (Button) recordPopUp.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button saveButton = (Button) recordPopUp.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Double amount = Double.parseDouble(amountEditText.getText().toString());
                    amount = expenseRadioButton.isChecked() ? amount * -1: amount;
                    String note = noteEditText.getText().toString();
                    String category = categorySpinner.getSelectedItem().toString();
                    Record record = new Record(amount, note,day + "." + month + "." + year, category);
                    record.save();
                    refreshBalance(amount);
                    Toast.makeText(getContext(), "Record created", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    addRecordToList(record);
                    customAdapter.notifyItemInserted(0);
                } catch (Exception ex){
                    Toast.makeText(getContext(), "Amount is required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int getDay(){
        return day == calendar.get(Calendar.DAY_OF_MONTH) ? calendar.get(Calendar.DAY_OF_MONTH) : day;
    }

    private int getMonth(){
        return month == calendar.get(Calendar.MONTH) ? calendar.get(Calendar.MONTH) + 1 : month;
    }

    private int getYear(){
        return year == calendar.get(Calendar.YEAR) ? calendar.get(Calendar.YEAR) : year;
    }
}