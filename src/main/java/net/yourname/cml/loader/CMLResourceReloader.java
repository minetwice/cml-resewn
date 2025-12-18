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
    // === Items (CML) ===
    public static final Map<Identifier, CMLProperties> ITEM_PROPERTIES_MAP = new HashMap<>();
    public static final Map<String, JsonObject> ITEM_MODELS_CACHE = new HashMap<>(); // key: path like "author/sword.json"

    // === Blocks (CBL) ===
    public static final Map<String, CMLProperties> BLOCK_PROPERTIES_MAP = new HashMap<>(); // key: "minecraft:stone"
    public static final Map<String, JsonObject> BLOCK_MODELS_CACHE = new HashMap<>();
    public static final Map<String, JsonObject> BLOCK_ANIMATIONS_CACHE = new HashMap<>(); // key: "author/stone.json"

    // === Entities (CEM) ===
    public static final Map<String, CMLProperties> ENTITY_PROPERTIES_MAP = new HashMap<>(); // key: "minecraft:zombie"

    @Override
    public Identifier getFabricId() {
        return Identifier.of("cml-resewn", "reloader");
    }

    @Override
    public void reload(ResourceManager manager, Profiler profiler) {
        ITEM_PROPERTIES_MAP.clear();
        ITEM_MODELS_CACHE.clear();
        BLOCK_PROPERTIES_MAP.clear();
        BLOCK_MODELS_CACHE.clear();
        BLOCK_ANIMATIONS_CACHE.clear();
        ENTITY_PROPERTIES_MAP.clear();

        // === Load CML (Items) ===
        loadCategory(manager, "optifine/cml", ITEM_PROPERTIES_MAP, ITEM_MODELS_CACHE);

        // === Load CBL (Blocks) ===
        loadCategory(manager, "optifine/cbl", BLOCK_PROPERTIES_MAP, BLOCK_MODELS_CACHE, this::extractBlockId);
        // Load block animations (.mcmeta)
        manager.findResources("optifine/cbl", path -> path.getPath().endsWith(".mcmeta"))
                .forEach((id, res) -> {
                    try (var reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonObject json = JsonHelper.deserialize(reader);
                        String path = id.getPath()
                                .replace("optifine/cbl/", "")
                                .replace(".mcmeta", "") + ".json";
                        BLOCK_ANIMATIONS_CACHE.put(path, json);
                    } catch (Exception e) {
                        System.err.println("[CML] Invalid block animation: " + id);
                    }
                });

        // === Load CEM (Entities) ===
        manager.findResources("optifine/cem", path -> path.getPath().endsWith(".properties"))
                .forEach((id, resource) -> {
                    try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        CMLProperties props = CMLProperties.parse(reader);
                        if (props.isValid()) {
                            String entityId = extractEntityId(id);
                            ENTITY_PROPERTIES_MAP.put(entityId, props);
                        }
                    } catch (Exception e) {
                        System.err.println("[CML] Failed to load entity props: " + id + " | " + e.getMessage());
                    }
                });

        System.out.printf("[CML] Loaded: %d items, %d blocks, %d entities%n",
                ITEM_PROPERTIES_MAP.size(),
                BLOCK_PROPERTIES_MAP.size(),
                ENTITY_PROPERTIES_MAP.size());
    }

    // Generic loader for cml/cbl
    private void loadCategory(
            ResourceManager manager,
            String rootPath,
            Map<?, CMLProperties> propsMap,
            Map<String, JsonObject> modelsCache
    ) {
        loadCategory(manager, rootPath, propsMap, modelsCache, id -> null);
    }

    private void loadCategory(
            ResourceManager manager,
            String rootPath,
            Map<?, CMLProperties> propsMap,
            Map<String, JsonObject> modelsCache,
            java.util.function.Function<Identifier, String> idExtractor
    ) {
        manager.findResources(rootPath, path -> path.getPath().endsWith(".properties"))
                .forEach((id, resource) -> {
                    try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        CMLProperties props = CMLProperties.parse(reader);
                        if (props.isValid()) {
                            Object key = idExtractor.apply(id);
                            if (key == null) key = id;
                            propsMap.put(key, props);

                            // Preload model
                            String model = props.getModel();
                            if (model != null && !model.isEmpty()) {
                                String modelPath = model.startsWith("optifine/") ? model : "optifine/" + rootPath + "/" + model;
                                Identifier modelId = Identifier.of("minecraft", modelPath);
                                if (manager.containsResource(modelId)) {
                                    try (var modelReader = manager.openAsReader(modelId)) {
                                        JsonObject json = JsonHelper.deserialize(modelReader);
                                        String cacheKey = model.startsWith("optifine/") ? model.substring("optifine/".length()) : model;
                                        modelsCache.put(cacheKey, json);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("[CML] Failed to load " + id + ": " + e.getMessage());
                    }
                });
    }

    private String extractBlockId(Identifier id) {
        String path = id.getPath().replace("optifine/cbl/", "").replace(".properties", "");
        if (path.contains("/")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        return "minecraft:" + path;
    }

    private String extractEntityId(Identifier id) {
        String path = id.getPath().replace("optifine/cem/", "").replace(".properties", "");
        if (path.contains("/")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        return "minecraft:" + path;
    }
}
