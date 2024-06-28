package me.jtx.evobases.utils;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Townhall {

    private final Set<Integer> supportedLevels = new LinkedHashSet<>();

    public Townhall() {
        loadSupportedLevels();
    }

    private void loadSupportedLevels() {
        try {
            Path path = Paths.get("TownHallLevels.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                // Create default JSON content
                JsonObject defaultJson = new JsonObject();
                JsonArray levelsArray = new JsonArray();

                levelsArray.add(new JsonPrimitive(""));


                defaultJson.add("supportedLevels", levelsArray);
                try (FileWriter writer = new FileWriter("TownHallLevels.json")) {
                    writer.write(defaultJson.toString());
                }
            }

            // Read the JSON file
            File file = new File("TownHallLevels.json");
            JsonObject jsonObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
            JsonArray levelsArray = jsonObject.getAsJsonArray("supportedLevels");
            for (int i = 0; i < levelsArray.size(); i++) {
                supportedLevels.add(levelsArray.get(i).getAsInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSupportedTownHall(int level) {
        return supportedLevels.contains(level);
    }

    public String displaySupportedLevels() {
        List<Integer> levels = new ArrayList<>(supportedLevels);

        return formatLevels(levels);
    }

    private static String formatLevels(List<Integer> levels) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < levels.size(); i++) {
            sb.append(levels.get(i));
            if (i < levels.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}
