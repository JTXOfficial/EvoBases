package me.jtx.evobases.utils;

import com.google.gson.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OrderEmbedDetails {

    // Use JsonArray to store multiple message objects
    private JsonArray embedDetails;

    public OrderEmbedDetails() {
        embedDetails = new JsonArray();
        loadEmbedData();
    }

    public void addEmbedData(String messageId, JsonObject data) {
        boolean messageExists = false;
        for (int i = 0; i < embedDetails.size(); i++) {
            JsonObject obj = embedDetails.get(i).getAsJsonObject();
            if (obj.get("messageId").getAsString().equals(messageId)) {
                embedDetails.set(i, data);
                messageExists = true;
                break;
            }
        }

        if (!messageExists) {
            embedDetails.add(data);
        }
    }

    public String getBaseLinkByMessageId(String messageId) {
        for (JsonElement element : embedDetails) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("messageId") && obj.get("messageId").getAsString().equals(messageId)) {
                if (obj.has("baseLink")) {
                    return obj.get("baseLink").getAsString();
                }
            }
        }
        return null;
    }

    public JsonArray getUniqueUsersByMessageId(String messageId) {
        for (JsonElement element : embedDetails) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("messageId") && obj.get("messageId").getAsString().equals(messageId)) {
                if (obj.has("uniqueUsers")) {
                    return obj.get("uniqueUsers").getAsJsonArray();
                }
            }
        }
        return null;
    }

    public void loadEmbedData() {
        try {
            Path path = Paths.get("OrderEmbedDetails.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, "[]".getBytes());
            }

            FileReader fileReader = new FileReader(path.toFile());
            embedDetails = JsonParser.parseReader(fileReader).getAsJsonArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveEmbedData() {
        try {
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(embedDetails);
            Path path = Paths.get("OrderEmbedDetails.json");
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getJsonObjectByMessageId(String messageId) {
        for (JsonElement element : embedDetails) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("messageId") && obj.get("messageId").getAsString().equals(messageId)) {
                return obj;
            }
        }
        return null;
    }
}
