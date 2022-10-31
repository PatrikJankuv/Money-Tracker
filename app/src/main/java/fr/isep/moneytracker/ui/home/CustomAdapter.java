package fr.isep.moneytracker.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.isep.moneytracker.R;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private Context context;
    private ArrayList recordDescription, recordDate, recordAmount, recordCategory;
    public Map<String, CategoryIcon> categoryIconMap;


    CustomAdapter(Context context, ArrayList record_description, ArrayList record_date, ArrayList record_amount, ArrayList record_category) {
        this.context = context;
        this.recordDescription = record_description;
        this.recordDate = record_date;
        this.recordAmount = record_amount;
        this.recordCategory = record_category;
        initIconMap();
    }

    private void initIconMap(){
        categoryIconMap = new HashMap<>();
        categoryIconMap.put("F", new CategoryIcon(R.drawable.ic_baseline_food_24, R.color.food_category));
        categoryIconMap.put("S", new CategoryIcon(R.drawable.ic_baseline_shopping_24, R.color.shopping_category));
        categoryIconMap.put("H", new CategoryIcon(R.drawable.ic_baseline_house_24, R.color.housing_category));
        categoryIconMap.put("T", new CategoryIcon(R.drawable.ic_baseline_transport_24, R.color.transportation_category));
        categoryIconMap.put("L", new CategoryIcon(R.drawable.ic_baseline_nightlife_24, R.color.entertainment_category));
        categoryIconMap.put("I", new CategoryIcon(R.drawable.ic_baseline_income_24, R.color.income_category));
        categoryIconMap.put("O", new CategoryIcon(R.drawable.ic_baseline_other_24, R.color.other_category));
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
        holder.recordDescriptionTxt.setText(String.valueOf(recordDescription.get(position)));
        holder.recordDateTxt.setText(String.valueOf(recordDate.get(position)));
        holder.recordAmountTxt.setText(String.valueOf(recordAmount.get(position)));

        System.out.println(recordCategory.get(position).toString().charAt(0));
        CategoryIcon icon = categoryIconMap.get(String.valueOf(recordCategory.get(position).toString().charAt(0)));
        assert icon != null;
        holder.categoryImg.setImageResource(icon.getIcon());
        holder.categoryImg.setBackgroundResource(icon.getColor());
    }

    @Override
    public int getItemCount() {
        return this.recordAmount.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView recordDescriptionTxt, recordDateTxt, recordAmountTxt;
        private ImageView categoryImg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recordDescriptionTxt = itemView.findViewById(R.id.description_row);
            recordDateTxt = itemView.findViewById(R.id.date_row);
            recordAmountTxt = itemView.findViewById(R.id.amount_row);
            categoryImg = itemView.findViewById(R.id.category);
        }
    }

    private static class CategoryIcon{
        int icon;
        int color;

        public CategoryIcon(int icon, int color) {
            this.icon = icon;
            this.color = color;
        }

        public int getIcon() {
            return icon;
        }

        public int getColor() {
            return color;
        }
    }
}
