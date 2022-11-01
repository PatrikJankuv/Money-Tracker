package fr.isep.moneytracker.model;

import com.orm.SugarRecord;

public class User extends SugarRecord {
    String username;
    Double balance;
    String currency;

    public User(){}

    public User(String username, Double balance, String currency) {
        this.username = username;
        this.balance = balance;
        this.currency = currency;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
