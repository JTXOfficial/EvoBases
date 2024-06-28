package me.jtx.evobases.utils;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OrderDetail {

    private JsonArray orders;

    public OrderDetail() {
        orders = new JsonArray();
        loadOrders();
    }

    private int orderId;
    private int queueNum;
    private String userId;
    private boolean completed;
    private String messageId;

    public OrderDetail(int orderId, int queueNum, String userId, boolean completed, String messageId) {
        this.orderId = orderId;
        this.queueNum = queueNum;
        this.userId = userId;
        this.completed = completed;
        this.messageId = messageId;
    }

    public void addOrder(String userId, String messageId) {
        this.orderId = getHighestOrderNum() + 1;
        this.queueNum = getHighestPosition() + 1;
        this.userId = userId;
        this.completed = false;
        this.messageId = messageId;

        JsonObject order = new JsonObject();
        order.addProperty("orderId", this.orderId);
        order.addProperty("queueNum", this.queueNum);
        order.addProperty("userId", this.userId);
        order.addProperty("completed", this.completed);
        order.addProperty("messageId", this.messageId);
        orders.add(new Gson().toJsonTree(order));

    }

    private int getHighestPosition() {
        return orders.isEmpty() ? 0 : Collections.max(StreamSupport.stream(orders.spliterator(), false)
                .map(element -> element.getAsJsonObject().get("queueNum").getAsInt())
                .toList());
    }

    public int getHighestOrderNum() {
        return orders.isEmpty() ? 0 : Collections.max(StreamSupport.stream(orders.spliterator(), false)
                .map(element -> element.getAsJsonObject().get("orderId").getAsInt())
                .toList());
    }

    public void loadOrders() {
        try {
            Path path = Paths.get("Orders.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, "[]".getBytes());
            }

            File file = new File("Orders.json");
            JsonArray newOrder = new Gson().fromJson(new FileReader(file), JsonArray.class);
            if (newOrder != null) {
                orders = newOrder;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOrder() {
        try {
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(orders);
            Path path = Paths.get("Orders.json");
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAllOrderIds() {
        return StreamSupport.stream(orders.spliterator(), false)
                .map(element -> element.getAsJsonObject().get("orderId").getAsInt())
                .collect(Collectors.toList());
    }

    public boolean orderIdExists(int orderId) {
        return getAllOrderIds().contains(orderId);
    }


    public String getUserIdByOrderId(int orderId) {
        for (JsonElement orderElement : orders) {
            JsonObject order = orderElement.getAsJsonObject();
            if (order.get("orderId").getAsInt() == orderId) {
                return order.get("userId").getAsString();
            }
        }
        return null;
    }

    public String getMessageIdByOrderId(int orderId) {
        for (JsonElement orderElement : orders) {
            JsonObject order = orderElement.getAsJsonObject();
            if (order.get("orderId").getAsInt() == orderId) {
                return order.get("messageId").getAsString();
            }
        }
        return null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(int orderId) {
        orders.forEach(order -> {
            JsonObject orderObj = order.getAsJsonObject();
            if (orderObj.get("orderId").getAsInt() == orderId) {
                orderObj.addProperty("completed", true);
            }
        });
        recalculateQueuePositions();
        saveOrder();
    }

    private void recalculateQueuePositions() {
        int position = 1;
        for (JsonElement order : orders) {
            JsonObject orderObj = order.getAsJsonObject();
            if (!orderObj.get("completed").getAsBoolean()) {
                orderObj.addProperty("queueNum", position++);
            }
        }
    }

    public List<JsonObject> getIncompleteOrders() {
        return StreamSupport.stream(orders.spliterator(), false)
                .map(element -> element.getAsJsonObject())
                .filter(order -> !order.get("completed").getAsBoolean())
                .collect(Collectors.toList());
    }

    public String getUserId() {
        return userId;
    }

}

