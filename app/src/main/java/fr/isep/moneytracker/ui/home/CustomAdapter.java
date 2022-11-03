package fr.isep.moneytracker.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.isep.moneytracker.R;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private Context context;
    private HomeFragment fragment;
    private ArrayList recordDescription, recordDate, recordAmount, recordCategory, recordId;
    public Map<String, CategoryIcon> categoryIconMap;


    CustomAdapter(Context context, ArrayList record_description, ArrayList record_date, ArrayList record_amount, ArrayList record_category, ArrayList recordId, HomeFragment fragment) {
        this.context = context;
        this.recordDescription = record_description;
        this.recordDate = record_date;
        this.recordAmount = record_amount;
        this.recordCategory = record_category;
        this.recordId = recordId;
        this.fragment = fragment;
        initIconMap();
    }

    private void initIconMap(){
        categoryIconMap = new HashMap<>();
        categoryIconMap.put("F", new CategoryIcon(R.drawable.ic_baseline_food_24, R.color.food_category, 0));
        categoryIconMap.put("S", new CategoryIcon(R.drawable.ic_baseline_shopping_24, R.color.shopping_category, 1));
        categoryIconMap.put("H", new CategoryIcon(R.drawable.ic_baseline_house_24, R.color.housing_category, 2));
        categoryIconMap.put("T", new CategoryIcon(R.drawable.ic_baseline_transport_24, R.color.transportation_category, 3));
        categoryIconMap.put("E", new CategoryIcon(R.drawable.ic_baseline_nightlife_24, R.color.entertainment_category, 4));
        categoryIconMap.put("I", new CategoryIcon(R.drawable.ic_baseline_income_24, R.color.income_category, 5));
        categoryIconMap.put("O", new CategoryIcon(R.drawable.ic_baseline_other_24, R.color.other_category, 6));
    }

    @NonNull
    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.record_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {
        String description = String.valueOf(recordDescription.get(position));
        String date = String.valueOf(recordDate.get(position));
        String amount = String.valueOf(recordAmount.get(position));
        Long id = (Long) recordId.get(position);
        int spinnerIndex = Objects.requireNonNull(categoryIconMap.get(String.valueOf(recordCategory.get(position).toString().charAt(0)))).spinnerIndex;

        holder.recordDescriptionTxt.setText(description);
        holder.recordDateTxt.setText(date);
        holder.recordAmountTxt.setText(amount);
        CategoryIcon icon = categoryIconMap.get(String.valueOf(recordCategory.get(position).toString().charAt(0)));
        assert icon != null;
        holder.categoryImg.setImageResource(icon.getIcon());
        holder.categoryImg.setBackgroundResource(icon.getColor());

        holder.recordCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    fragment.editRecordDialog(id, description, date, amount, spinnerIndex, position);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.recordAmount.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView recordDescriptionTxt, recordDateTxt, recordAmountTxt;
        private ImageView categoryImg;
        private CardView  recordCardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recordDescriptionTxt = itemView.findViewById(R.id.description_row);
            recordDateTxt = itemView.findViewById(R.id.date_row);
            recordAmountTxt = itemView.findViewById(R.id.amount_row);
            categoryImg = itemView.findViewById(R.id.category);
            recordCardView = itemView.findViewById(R.id.row_card_view);
        }
    }

    private static class CategoryIcon{
        int icon;
        int color;
        int spinnerIndex;

        public CategoryIcon(int icon, int color, int spinnerIndex) {
            this.icon = icon;
            this.color = color;
            this.spinnerIndex = spinnerIndex;
        }

        public int getIcon() {
            return icon;
        }

        public int getColor() {
            return color;
        }
    }
}
