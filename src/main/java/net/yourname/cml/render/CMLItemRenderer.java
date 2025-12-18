package net.yourname.cml.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.yourname.cml.loader.CMLProperties;
import net.yourname.cml.loader.CMLResourceReloader;
import net.yourname.cml.model.CMLModelProvider;

import java.util.Optional;

public class CMLItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final Identifier MISSING_MODEL_ID = ModelIdentifier.ofVanilla("builtin/missing", "inventory");

    public static void register() {
        // Register as fallback for ALL items — highest priority
        BuiltinItemRendererRegistry.INSTANCE.register((item) -> true, new CMLItemRenderer());
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        try {
            Optional<CMLProperties> match = findMatchingProperties(stack);
            if (match.isPresent()) {
                CMLProperties props = match.get();

                // Case 1: Custom MODEL (e.g., Blockbench .json)
                if (props.getModel() != null && !props.getModel().isEmpty()) {
                    renderCustomModel(stack, mode, matrices, vertexConsumers, light, overlay, props);
                    return;
                }

                // Case 2: Texture override only (no geometry change)
                if (props.getTexture() != null && !props.getTexture().isEmpty()) {
                    renderCustomTexture(stack, mode, matrices, vertexConsumers, light, overlay, props);
                    return;
                }
            }

            // Fallback to vanilla
            MinecraftClient.getInstance().getItemRenderer()
                    .renderItem(stack, mode, matrices, vertexConsumers, light, overlay);
        } catch (Exception e) {
            // Never crash — log and fallback
            System.err.println("[CML] Render error for " + stack.getItem() + ": " + e.getMessage());
            MinecraftClient.getInstance().getItemRenderer()
                    .renderItem(stack, mode, matrices, vertexConsumers, light, overlay);
        }
    }

    private void renderCustomModel(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light, int overlay, CMLProperties props) {
        MinecraftClient client = MinecraftClient.getInstance();
        String modelPath = props.getModel();

        // Resolve model ID: e.g., "optifine/cml/author/sword.json" → cml:sword
        // We strip ".json" and use namespace "cml"
        String cleanPath = modelPath.replace(".json", "");
        Identifier modelId = Identifier.tryParse("cml:" + cleanPath);
        if (modelId == null) {
            modelId = MISSING_MODEL_ID;
        }

        // Ensure model is registered (lazy)
        CMLModelProvider.ensureRegistered(modelId, modelPath);

        // Get baked model
        BakedModel model = client.getBakedModelManager().getModel(modelId);
        if (model == null || model == client.getBakedModelManager().getMissingModel()) {
            model = client.getItemRenderer().getModel(stack, null, null, 0);
        }

        // Render with correct context
        ItemRenderer itemRenderer = client.getItemRenderer();
        itemRenderer.renderItem(stack, mode, matrices, vertexConsumers, light, overlay, model);
    }

    private void renderCustomTexture(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light, int overlay, CMLProperties props) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();

        // Get vanilla model first
        BakedModel vanillaModel = itemRenderer.getModel(stack, null, null, 0);

        // Create texture override model (wrapper)
        BakedModel texturedModel = new TextureOverrideModel(vanillaModel, props.getTexture());

        // Render
        itemRenderer.renderItem(stack, mode, matrices, vertexConsumers, light, overlay, texturedModel);
    }

    private Optional<CMLProperties> findMatchingProperties(ItemStack stack) {
        for (CMLProperties props : CMLResourceReloader.PROPERTIES_MAP.values()) {
            // 1. Item match
            String itemId = Identifier.of(stack.getItem()).getPath(); // e.g., "netherite_sword"
            boolean itemMatch = false;
            for (String item : props.getItems()) {
                String cleanItem = item.trim().replace("minecraft:", "");
                if (cleanItem.equals(itemId) || cleanItem.equals("*")) {
                    itemMatch = true;
                    break;
                }
            }
            if (!itemMatch) continue;

            // 2. NBT name match (case-insensitive pattern with * wildcard)
            String namePattern = props.getNamePattern();
            if (namePattern != null && !namePattern.isEmpty()) {
                String customName = stack.hasCustomName() ? stack.getName().getString() : "";
                String pattern = namePattern.toLowerCase();
                if (pattern.startsWith("ipattern:*") && pattern.endsWith("*")) {
                    String keyword = pattern.substring(10, pattern.length() - 1);
                    if (!customName.toLowerCase().contains(keyword)) {
                        continue;
                    }
                } else if (!customName.equals(namePattern)) {
                    continue;
                }
            }

            return Optional.of(props);
        }
        return Optional.empty();
    }

    // Helper: Texture override wrapper
    private static class TextureOverrideModel implements BakedModel {
        private final BakedModel delegate;
        private final String texturePath;

        TextureOverrideModel(BakedModel delegate, String texturePath) {
            this.delegate = delegate;
            this.texturePath = texturePath;
        }

        @Override
        public boolean isVanillaAdapter() {
            return delegate.isVanillaAdapter();
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, ModelBakeSettings settings) {
            // Let delegate handle geometry; we only override sprite in render
            delegate.emitItemQuads(stack, randomSupplier, settings);
        }

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, ModelBakeSettings settings) {
            delegate.emitBlockQuads(blockView, state, pos, randomSupplier, settings);
        }

        @Override
        public Sprite getParticleSprite() {
            // Override particle texture if needed
            try {
                Identifier texId = Identifier.of("minecraft", texturePath);
                MinecraftClient client = MinecraftClient.getInstance();
                Sprite sprite = client.getTextureManager().getSprite(texId);
                return sprite;
            } catch (Exception e) {
                return delegate.getParticleSprite();
            }
        }

        @Override
        public boolean useAmbientOcclusion() {
            return delegate.useAmbientOcclusion();
        }

        @Override
        public boolean hasDepth() {
            return delegate.hasDepth();
        }

        @Override
        public boolean isSideLit() {
            return delegate.isSideLit();
        }

        @Override
        public boolean isBuiltin() {
            return delegate.isBuiltin();
        }
    }
}
