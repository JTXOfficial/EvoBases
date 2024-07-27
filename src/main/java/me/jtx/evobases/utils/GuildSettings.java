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

public class GuildSettings {

    private final Set<Integer> supportedLevels = new LinkedHashSet<>();
    private final Set<String> baseTypes = new LinkedHashSet<>();

    public GuildSettings() {
        loadSettings();
    }

    private void loadSettings() {
        try {
            Path path = Paths.get("guildSettings.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                // Create default JSON content
                JsonObject defaultJson = new JsonObject();

                // Initialize supportedLevels
                JsonArray levelsArray = new JsonArray();
                int[] defaultLevels = {4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16};
                for (int level : defaultLevels) {
                    levelsArray.add(new JsonPrimitive(level));
                }
                defaultJson.add("supportedLevels", levelsArray);

                // Initialize baseTypes
                JsonArray baseTypesArray = new JsonArray();
                String[] defaultBaseTypes = {"sausage", "evoxq", "tlm lucky", "price", "benz", "solo yolo", "phoenix", "vampire", "jigsaw", "ryzen"};
                for (String type : defaultBaseTypes) {
                    baseTypesArray.add(new JsonPrimitive(type));
                }
                defaultJson.add("baseTypes", baseTypesArray);

                try (FileWriter writer = new FileWriter("guildSettings.json")) {
                    writer.write(defaultJson.toString());
                }
            }

            // Read the JSON file
            File file = new File("guildSettings.json");
            JsonObject jsonObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();

            // Load supportedLevels
            JsonArray levelsArray = jsonObject.getAsJsonArray("supportedLevels");
            for (int i = 0; i < levelsArray.size(); i++) {
                supportedLevels.add(levelsArray.get(i).getAsInt());
            }

            // Load baseTypes
            JsonArray baseTypesArray = jsonObject.getAsJsonArray("baseTypes");
            for (int i = 0; i < baseTypesArray.size(); i++) {
                baseTypes.add(baseTypesArray.get(i).getAsString());
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

    public String displayBaseTypes() {
        List<String> types = new ArrayList<>(baseTypes);
        return formatTypes(types);
    }

    public Set<String> getBaseTypes() {
        return new HashSet<>(baseTypes);
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

    private static String formatTypes(List<String> types) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            sb.append(types.get(i));
            if (i < types.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean isSupportedBaseType(String type) {
        return baseTypes.contains(type);
    }

}
