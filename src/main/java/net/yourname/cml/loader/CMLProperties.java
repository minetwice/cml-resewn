package net.yourname.cml.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class CMLProperties {
    private final Map<String, String> values = new HashMap<>();

    public static CMLProperties parse(BufferedReader reader) throws IOException {
        CMLProperties props = new CMLProperties();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int eq = line.indexOf('=');
            if (eq > 0) {
                String key = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String val = line.substring(eq + 1).trim();
                props.values.put(key, val);
            }
        }
        return props;
    }

    public boolean isValid() {
        return values.containsKey("items") || values.containsKey("matchitems");
    }

    public String[] getItems() {
        String items = values.getOrDefault("items", values.get("matchitems"));
        return items != null ? items.split("\\s*,\\s*") : new String[0];
    }

    public String getNamePattern() {
        return values.get("nbt.display.name");
    }

    public String getModel() {
        return values.get("model");
    }

    public String getTexture() {
        return values.get("texture");
    }

    // Add more as needed: weight, damage, enchantments, etc.
}
