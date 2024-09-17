package me.jtx.evobases.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OrderEmbedDetails {

    private JsonArray embedDetails;

    public OrderEmbedDetails() {
        embedDetails = new JsonArray();
        loadEmbedData();
    }

    public JsonArray getUniqueUsers() {
        return embedDetails;
    }

    public void loadEmbedData() {
        try {
            Path path = Paths.get("OrderEmbedDetails.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, "[]".getBytes());
            }

            File file = new File("OrderEmbedDetails.json");
            JsonArray newDetails = new Gson().fromJson(new FileReader(file), JsonArray.class);
            if (newDetails != null) {
                embedDetails = newDetails;
            }
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

}
