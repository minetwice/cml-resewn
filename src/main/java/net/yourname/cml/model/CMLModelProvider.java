package net.yourname.cml.model;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.yourname.cml.loader.CMLResourceReloader;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class CMLModelProvider {
    static {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (modelId, context) -> {
            if (!modelId.getNamespace().equals("cml")) return null;

            String path = modelId.getPath();
            JsonObject json = CMLResourceReloader.MODELS_CACHE.get(path);
            if (json != null) {
                return JsonUnbakedModel.deserialize(json);
            }
            return null;
        });

        ModelLoadingRegistry.INSTANCE.registerModelProvider((modelProviderContext, out) -> {
            // Optional: Pre-register known IDs
        });
    }

    public static void ensureRegistered(Identifier modelId, String modelPath) {
        // Fabric auto-loads if model.json exists + provider returns non-null
        // No-op — registration is lazy & handled above
    }
}
