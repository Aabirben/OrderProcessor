package org.example.commande;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import java.lang.reflect.Type;

public class OrderAdapter implements JsonDeserializer<Order> {
    @Override
    public Order deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        Order order = new Order();
        order.setId(jsonObject.get("id").getAsInt());
        order.setAmount(jsonObject.get("amount").getAsDouble());
        order.setCustomerId(jsonObject.get("customerId").getAsInt());
        order.setStatus(jsonObject.get("status").getAsString());
        return order;
    }
}
