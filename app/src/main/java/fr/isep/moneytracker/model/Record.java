package fr.isep.moneytracker.model;

import com.orm.SugarRecord;

public class Record extends SugarRecord {
    Double amount;
    String description;
    // SQLite doesn't support date format, instead useing String in 'YYYY/mm/dd' format
    String date;
    String category;

    public Record(){}

    public Record(Double amount, String description, String date, String category) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
