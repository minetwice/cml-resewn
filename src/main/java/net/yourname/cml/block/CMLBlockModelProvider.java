package net.yourname.cml.block;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.yourname.cml.animation.CMLAnimationManager;
import net.yourname.cml.animation.AnimatedCMLModel;
import net.yourname.cml.loader.CMLResourceReloader;

public class CMLBlockModelProvider {
    static {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (modelId, context) -> {
            if (!modelId.getNamespace().equals("cml_block")) return null;

            String path = modelId.getPath();
            JsonObject json = CMLResourceReloader.BLOCK_MODELS_CACHE.get(path);
            if (json != null) {
                return JsonUnbakedModel.deserialize(json);
            }
            return null;
        });
    }

    public static BakedModel getAnimatedModelFor(BlockState state, ModelLoader loader) {
        // Match block → properties (e.g., minecraft:stone → cbl/author/stone.properties)
        String blockId = Identifier.of(state.getBlock()).toString();
        var props = CMLResourceReloader.BLOCK_PROPERTIES_MAP.get(blockId);
        if (props == null) return null;

        String modelPath = props.getModel();
        if (modelPath == null) return null;

        Identifier modelId = Identifier.of("cml_block", modelPath.replace(".json", ""));
        UnbakedModel unbaked = MinecraftClient.getInstance().getModelLoader().getOrLoadModel(modelId);
        if (unbaked == null) return null;

        BakedModel baked = unbaked.bake(loader, tex -> MinecraftClient.getInstance().getSpriteAtlas(tex.getAtlasId()).apply(tex.getTextureId()), ModelLoader.INSTANCE.getModelState(), modelId);
        if (baked == null) return null;

        // Add animation if .mcmeta exists
        JsonObject animJson = CMLResourceReloader.BLOCK_ANIMATIONS_CACHE.get(modelPath);
        if (animJson != null) {
            CMLAnimationManager anim = new CMLAnimationManager(modelPath, animJson);
            return new AnimatedCMLModel(baked, anim);
        }

        return baked;
    }
}
