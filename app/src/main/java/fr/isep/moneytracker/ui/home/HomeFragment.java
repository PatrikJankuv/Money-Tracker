package fr.isep.moneytracker.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import fr.isep.moneytracker.R;
import fr.isep.moneytracker.databinding.FragmentHomeBinding;
import fr.isep.moneytracker.model.Record;
import fr.isep.moneytracker.model.User;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView titleTextView, datePicker;
    private EditText amountEditText, descriptionEditText;
    private Button saveButton, cancelButton, editButton, deleteButton;
    private RadioButton expenseRadioButton;
    private Spinner categorySpinner;
    private DatePickerDialog.OnDateSetListener setListener;
    private final Calendar calendar = Calendar.getInstance();
    private int day = calendar.get(Calendar.DAY_OF_MONTH);
    private int month = calendar.get(Calendar.MONTH) + 1;
    private int year = calendar.get(Calendar.YEAR);
    private Long editId;
    private Double editOldAmount;
    private int editPosition;
    private ArrayList<String> amountRecord, categoryRecord, dateRecord, descriptionRecord;
    private ArrayList<Long> idRecord;
    private CustomAdapter customAdapter;
    private User user;
    private View recordPopUp;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private ImageView emptyImage;
    private TextView emptyMessage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initAlertDialog();

        binding.addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRecordDialog();
            }
        });

        emptyMessage = binding.emptyText;
        emptyImage = binding.emptyImage;

        user = User.first(User.class);
        refreshBalance(0.0);
        loadAllRecords();
        customAdapter = new CustomAdapter(getActivity(), descriptionRecord, dateRecord, amountRecord, categoryRecord, idRecord, this);
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
        idRecord = new ArrayList<>();

       Record.listAll(Record.class).forEach(this::addRecordToList);

       if(!idRecord.isEmpty()) {
           emptyImage.setVisibility(View.INVISIBLE);
           emptyMessage.setVisibility(View.INVISIBLE);
       }
    }

    private void addRecordToList(Record record){
        idRecord.add(0, record.getId());
        amountRecord.add(0, String.valueOf(record.getAmount()));
        categoryRecord.add(0, record.getCategory());
        dateRecord.add(0, record.getDate());
        descriptionRecord.add(0, record.getDescription());
    }

    private void editRecordLists(Record record){
        idRecord.set(editPosition, record.getId());
        amountRecord.set(editPosition, String.valueOf(record.getAmount()));
        categoryRecord.set(editPosition, record.getCategory());
        dateRecord.set(editPosition, record.getDate());
        descriptionRecord.set(editPosition, record.getDescription());
    }

    private void removeRecordFromLists(){
        idRecord.remove(editPosition);
        amountRecord.remove(editPosition);
        categoryRecord.remove(editPosition);
        dateRecord.remove(editPosition);
        descriptionRecord.remove(editPosition);
    }

    private void refreshBalance(Double amount){
        Double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);
        user.update();
        binding.balanceText.setText(String.valueOf(user.getBalance()) + " " + user.getCurrency());
    }

    private void refreshBalanceAfterUpdate(Double newAmount){
        Double newBalance = user.getBalance() + newAmount - editOldAmount;
        user.setBalance(newBalance);
        user.update();
        binding.balanceText.setText(String.valueOf(user.getBalance()) + " " + user.getCurrency());
    }

    private void refreshBalanceAfterDelete(){
        Double newBalance = user.getBalance() - editOldAmount;
        user.setBalance(newBalance);
        user.update();
        binding.balanceText.setText(String.valueOf(user.getBalance()) + " " + user.getCurrency());
    }

    private void initAlertDialog(){
        dialogBuilder = new AlertDialog.Builder(getContext());
        recordPopUp = getLayoutInflater().inflate(R.layout.add_record, null, false);

        titleTextView = (TextView) recordPopUp.findViewById(R.id.title);
        amountEditText = (EditText) recordPopUp.findViewById(R.id.amount);
        descriptionEditText = (EditText) recordPopUp.findViewById(R.id.note);
        expenseRadioButton = (RadioButton) recordPopUp.findViewById(R.id.expense);

        categorySpinner = (Spinner) recordPopUp.findViewById(R.id.categories);
        datePicker = (TextView) recordPopUp.findViewById(R.id.date_picker);

        cancelButton = (Button) recordPopUp.findViewById(R.id.cancel_button);
        saveButton = (Button) recordPopUp.findViewById(R.id.save_button);
        editButton = (Button) recordPopUp.findViewById(R.id.edit_button);
        deleteButton = (Button) recordPopUp.findViewById(R.id.delete_button);
    }

    public void addRecordDialog(){
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);

        initAlertDialog();
        showAlertDialog();
        editButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
    }

    public void editRecordDialog(Long id, String description, String date, String amount, int category, int position) throws ParseException {
        initAlertDialog();
        showAlertDialog();
        editButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);

        editOldAmount = Double.parseDouble(amount);
        amount = amount.startsWith("-") ? amount.substring(1) : amount;
        amountEditText.setText(amount);
        descriptionEditText.setText(description);

        day = Integer.parseInt(date.split("\\/")[2]);
        month = Integer.parseInt(date.split("\\/")[1]);
        year = Integer.parseInt(date.split("\\/")[0]);
        datePicker.setText(getDay() + "/" + getMonth() + "/" + getYear());
        categorySpinner.setSelection(category);
        editId = id;
        editPosition = position;
    }

    public void showAlertDialog() {
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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        datePicker.setText(getDay() + "/" + getMonth() + "/" + getYear());
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        setListener, getYear(), getMonth()-1, getDay());
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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(idRecord.isEmpty()) {
                        emptyImage.setVisibility(View.GONE);
                        emptyMessage.setVisibility(View.GONE);
                    }

                    Double amount = Double.parseDouble(amountEditText.getText().toString());
                    amount = expenseRadioButton.isChecked() ? amount * -1: amount;
                    String description = descriptionEditText.getText().toString();
                    String category = categorySpinner.getSelectedItem().toString();
                    Record record = new Record(amount, description,String.format("%02d", getYear()) + "/" + String.format("%02d", getMonth()) + "/" + String.format("%02d", getDay()), category);
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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Double amount = Double.parseDouble(amountEditText.getText().toString());
                    amount = expenseRadioButton.isChecked() ? amount * -1: amount;
                    String description = descriptionEditText.getText().toString();
                    String category = categorySpinner.getSelectedItem().toString();

                    Record record = Record.findById(Record.class, editId);
                    record.setAmount(amount);
                    record.setDate(String.format("%02d", getYear()) + "/" + String.format("%02d", getMonth()) + "/" + String.format("%02d", getDay()));
                    record.setCategory(category);
                    record.setDescription(description);
                    record.save();
                    refreshBalanceAfterUpdate(amount);

                    Toast.makeText(getContext(), "Record edited", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    editRecordLists(record);
                    customAdapter.notifyItemChanged(editPosition);
                } catch (Exception ex){
                    Toast.makeText(getContext(), "Amount is required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    deleteConfirmDialog();
                } catch (Exception ex){
                    Toast.makeText(getContext(), "Amount is required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete record");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Record record = Record.findById(Record.class, editId);
                record.delete();
                removeRecordFromLists();
                customAdapter.notifyDataSetChanged();
                refreshBalanceAfterDelete();
                Toast.makeText(getContext(), "Record deleted", Toast.LENGTH_SHORT).show();

                if(idRecord.isEmpty()) {
                    emptyImage.setVisibility(View.VISIBLE);
                    emptyMessage.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        builder.create().show();
    }

    private int getDay(){
        return day == calendar.get(Calendar.DAY_OF_MONTH) ? calendar.get(Calendar.DAY_OF_MONTH) : day;
    }

    private int getMonth(){
        return month == calendar.get(Calendar.MONTH) ? calendar.get(Calendar.MONTH) : month;
    }

    private int getYear(){
        return year == calendar.get(Calendar.YEAR) ? calendar.get(Calendar.YEAR) : year;
    }
}