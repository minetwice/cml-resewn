package net.yourname.cml.loader;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CMLResourceReloader implements SimpleSynchronousResourceReloadListener {
    public static final Map<Identifier, CMLProperties> PROPERTIES_MAP = new HashMap<>();
    public static final Map<String, JsonObject> MODELS_CACHE = new HashMap<>(); // key: path (e.g. "optifine/cml/author/sword.json")

    @Override
    public Identifier getFabricId() {
        return Identifier.of("cml-resewn", "reloader");
    }

    @Override
    public void reload(ResourceManager manager, Profiler profiler) {
        PROPERTIES_MAP.clear();
        MODELS_CACHE.clear();

        // Scan all /assets/minecraft/optifine/cml/.../*.properties
        manager.findResources("optifine/cml", path -> path.getPath().endsWith(".properties"))
                .forEach((id, resource) -> {
                    try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        CMLProperties props = CMLProperties.parse(reader);
                        if (props.isValid()) {
                            PROPERTIES_MAP.put(id, props);
                        }
                    } catch (IOException e) {
                        System.err.println("[CML] Failed to load " + id + ": " + e.getMessage());
                    }
                });

        // Preload models referenced in properties
        PROPERTIES_MAP.values().stream()
                .map(CMLProperties::getModel)
                .filter(m -> m != null && !m.isEmpty())
                .distinct()
                .forEach(modelPath -> {
                    Identifier modelId = Identifier.tryParse("minecraft:" + modelPath); // relative to assets/minecraft/
                    if (manager.containsResource(modelId)) {
                        try (var reader = manager.openAsReader(modelId)) {
                            JsonObject json = JsonHelper.deserialize(reader);
                            MODELS_CACHE.put(modelPath, json);
                        } catch (Exception e) {
                            System.err.println("[CML] Invalid model: " + modelPath);
                        }
                    }
                });

        System.out.println("[CML] Loaded " + PROPERTIES_MAP.size() + " custom models/textures.");
    }
}
