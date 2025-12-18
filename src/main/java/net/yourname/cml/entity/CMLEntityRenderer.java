package net.yourname.cml.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.yourname.cml.loader.CMLResourceReloader;
import net.yourname.cml.util.GeckoLibChecker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

public class CMLEntityRenderer {
    // Dynamic GeckoLib support
    private static boolean geckoRegistered = false;
    private static Constructor<?> geckoRendererConstructor;
    private static Method geckoSetModelMethod;

    @SuppressWarnings("unchecked")
    public static void register() {
        // Register texture-only fallback for all entities
        for (EntityType<?> type : EntityType.REGISTRY) {
            EntityRendererRegistry.register((EntityType<? extends Entity>) type, context -> {
                var original = context.getRegistryManager().get(context.getEntity().getType()).create(context);
                return new TextureOnlyWrapper<>((MobEntityRenderer<?, ?>) original);
            });
        }

        // If GeckoLib is present, enhance with geo support
        if (GeckoLibChecker.isLoaded()) {
            initGeckoReflection();
            GeckoLibEntityRenderer.register();
            System.out.println("[CML] GeckoLib detected — full 3D mob models enabled.");
        } else {
            GeckoLibChecker.warnIfMissing();
        }
    }

    // --- Texture-only wrapper (always active) ---
    public static class TextureOnlyWrapper<T extends MobEntity, M> extends MobEntityRenderer<T, M> {
        private final MobEntityRenderer<T, M> delegate;
        private Identifier overrideTexture = null;

        public TextureOnlyWrapper(MobEntityRenderer<T, M> delegate) {
            super(null, null, 0); // dummy — we delegate all calls
            this.delegate = delegate;
        }

        @Override
        public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
            // Apply CML texture override
            Optional<CMLProperties> match = findMatchingProperties(entity);
            if (match.isPresent()) {
                String tex = match.get().getTexture();
                if (tex != null && !tex.isEmpty()) {
                    this.overrideTexture = Identifier.tryParse("minecraft:" + tex);
                }
            }

            // Let GeckoLib or vanilla handle geometry; we only override texture
            delegate.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }

        @Override
        public Identifier getTexture(T entity) {
            return overrideTexture != null ? overrideTexture : delegate.getTexture(entity);
        }

        // Delegate everything else
        @Override public M getModel() { return (M) delegate.getModel(); }
        @Override public float getShadowRadius() { return delegate.getShadowRadius(); }

        private Optional<CMLProperties> findMatchingProperties(T entity) {
            String entityId = net.minecraft.util.Identifier.of(entity.getType()).toString();
            return CMLResourceReloader.ENTITY_PROPERTIES_MAP.entrySet().stream()
                    .filter(e -> e.getKey().equals(entityId) || e.getKey().equals("*"))
                    .map(Map.Entry::getValue)
                    .filter(props -> {
                        String namePattern = props.getNamePattern();
                        if (namePattern == null) return true;
                        String name = entity.hasCustomName() ? entity.getCustomName().getString() : "";
                        return namePattern.startsWith("ipattern:*") && namePattern.endsWith("*")
                                ? name.toLowerCase().contains(namePattern.substring(10, namePattern.length() - 1))
                                : name.equals(namePattern);
                    })
                    .findFirst();
        }
    }

    // --- GeckoLib dynamic setup ---
    private static void initGeckoReflection() {
        try {
            Class<?> baseClass = Class.forName("software.bernie.geckolib.renderer.layer.GeoRenderLayer");
            Class<?> rendererClass = Class.forName("net.yourname.cml.entity.GeckoLibEntityRenderer$GeoEntityRenderer");
            geckoRendererConstructor = rendererClass.getConstructor(EntityRendererFactory.Context.class);
            geckoSetModelMethod = rendererClass.getMethod("setCMLModel", String.class, String.class);
        } catch (Exception e) {
            System.err.println("[CML] GeckoLib reflection failed: " + e.getMessage());
        }
    }
}
