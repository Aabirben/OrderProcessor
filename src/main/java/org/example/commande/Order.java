package org.example.commande;

import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;

public class Order {
    private int id;
    private double amount;
    @SerializedName("customerId")
    private int customerId;
    private String status;
    private long date; // Timestamp de la commande effectuée par le client
    private long creationTime; // Horodatage de la création de la commande

    public Order() {}

    public Order(int id, double amount, int customerId, String status) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
        this.status = status;
        this.creationTime = System.currentTimeMillis(); // Utilisation de System.currentTimeMillis() pour l'horodatage
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long creationTime) { this.creationTime = creationTime; }

    @Override
    public String toString() {
        return "Order{" + "id=" + id + ", amount=" + amount + ", customerId=" + customerId + ", status='" + status + '\'' + ", date=" + new Timestamp(date) + ", creationTime=" + new Timestamp(creationTime) + '}';
    }
}
