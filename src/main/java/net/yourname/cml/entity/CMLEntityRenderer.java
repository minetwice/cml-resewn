package net.yourname.cml.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.yourname.cml.animation.CMLAnimationManager;
import net.yourname.cml.loader.CMLResourceReloader;

import java.util.Optional;

public class CMLEntityRenderer {
    @SuppressWarnings("unchecked")
    public static void register() {
        // Register override for ALL entity types
        for (EntityType<?> type : EntityType.REGISTRY) {
            EntityRendererRegistry.register((EntityType<? extends Entity>) type, CMLEntityRenderer::createRenderer);
        }
    }

    private static <T extends Entity> EntityRendererFactory<T> createRenderer(EntityRendererFactory.Context ctx) {
        return (EntityRendererFactory<T>) (context) -> {
            // Wrap original renderer with CML logic
            var original = context.getRegistryManager().get(context.getEntity().getType()).create(context);
            
            return new WrappedEntityRenderer<>((MobEntityRenderer<?, ?>) original, context);
        };
    }

    public static class WrappedEntityRenderer<T extends MobEntity, M extends EntityModel<T>> 
            extends MobEntityRenderer<T, M> {
        
        private final MobEntityRenderer<T, M> delegate;

        public WrappedEntityRenderer(MobEntityRenderer<T, M> delegate, EntityRendererFactory.Context ctx) {
            super(ctx, delegate.getModel(), delegate.getShadowRadius());
            this.delegate = delegate;
        }

        @Override
        public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
            try {
                Optional<CMLProperties> match = findMatchingProperties(entity);
                if (match.isPresent()) {
                    CMLProperties props = match.get();

                    // Texture override
                    String texturePath = props.getTexture();
                    if (texturePath != null && !texturePath.isEmpty()) {
                        Identifier texId = Identifier.tryParse("minecraft:" + texturePath);
                        if (texId != null) {
                            this.entityTexture = texId;
                        }
                    }

                    // Animation (if AnimatedCMLModel available)
                    // Note: Full geo anim requires GeckoLib — this applies simple matrix anim
                    CMLAnimationManager anim = getAnimationFor(props);
                    if (anim != null) {
                        // Apply root-level animation (e.g., bob, scale)
                        matrices.push();
                        matrices.translate(0, 0.2 * Math.sin(tickDelta * 0.5), 0);
                        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
                    }

                    delegate.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

                    if (anim != null) {
                        matrices.pop();
                    }
                    return;
                }
            } catch (Exception e) {
                System.err.println("[CML] Entity render error: " + e.getMessage());
            }

            // Fallback
            delegate.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }

        private Optional<CMLProperties> findMatchingProperties(T entity) {
            String entityId = Identifier.of(entity.getType()).toString(); // e.g., "minecraft:zombie"

            for (var entry : CMLResourceReloader.ENTITY_PROPERTIES_MAP.entrySet()) {
                String targetId = entry.getKey();
                CMLProperties props = entry.getValue();

                // Match by entity ID
                if (!targetId.equals(entityId) && !targetId.equals("*")) continue;

                // Match by name (nbt.display.Name or custom name)
                String namePattern = props.getNamePattern();
                if (namePattern != null) {
                    String name = entity.hasCustomName() ? entity.getCustomName().getString() : "";
                    if (namePattern.startsWith("ipattern:*") && namePattern.endsWith("*")) {
                        String keyword = namePattern.substring(10, namePattern.length() - 1);
                        if (!name.toLowerCase().contains(keyword)) continue;
                    } else if (!name.equals(namePattern)) {
                        continue;
                    }
                }

                // Match by NBT (e.g., nbt.PersistedData.Tag=1)
                // Extend CMLProperties to parse `nbt.*=` if needed — basic version:
                // We skip for now; can add on request.

                return Optional.of(props);
            }
            return Optional.empty();
        }

        private CMLAnimationManager getAnimationFor(CMLProperties props) {
            // For now, placeholder — full support via GeckoLib
            // You can add:
            //   if (props.getModel() != null) {
            //       JsonObject animJson = ...;
            //       return new CMLAnimationManager(...);
            //   }
            return null;
        }

        // Delegate all other methods
        @Override public M getModel() { return delegate.getModel(); }
        @Override public Identifier getTexture(T entity) { return delegate.getTexture(entity); }
        @Override public float getShadowRadius() { return delegate.getShadowRadius(); }
    }
}
