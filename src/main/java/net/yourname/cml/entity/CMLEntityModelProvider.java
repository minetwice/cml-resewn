package net.yourname.cml.entity;

import com.google.gson.JsonObject;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.TexturedModelData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.yourname.cml.loader.CMLResourceReloader;

import java.util.HashMap;
import java.util.Map;

public class CMLEntityModelProvider {
    public static final Map<String, EntityModelLayer> CUSTOM_LAYERS = new HashMap<>();

    public static <T extends Entity> EntityModel<T> getModelFor(T entity) {
        String entityId = Identifier.of(entity).toString();
        var props = CMLResourceReloader.ENTITY_PROPERTIES_MAP.get(entityId);
        if (props == null) return null;

        String modelPath = props.getModel();
        if (modelPath == null) return null;

        // Load .json → convert to EntityModel (simplified: use existing model + override texture/anim)
        // For full geo, use GeckoLib — but for simplicity, we override texture & animate via matrix

        return null; // Placeholder — full geo requires GeckoLib or custom loader
    }
}
