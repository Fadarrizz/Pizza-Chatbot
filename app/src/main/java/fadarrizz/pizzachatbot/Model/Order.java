package fadarrizz.pizzachatbot.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class Order {

    private String ID;
    private String category;
    private String pizza;
    private ArrayList<String> toppings;
    private Date orderDate;
    private int orderTime;

    public Order() {}

    public Order(String ID, String category, String pizza, ArrayList<String> toppings,
                 Date orderDate, int orderTime) {
        this.ID = ID;
        this.category = category;
        this.pizza = pizza;
        this.toppings = toppings;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPizza() {
        return pizza;
    }

    public void setPizza(String pizza) {
        this.pizza = pizza;
    }

    public ArrayList<String> getToppings() {
        return toppings;
    }

    public void setToppings(ArrayList<String> toppings) {
        this.toppings = toppings;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public int getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(int orderTime) {
        this.orderTime = orderTime;
    }
}
