package net.yourname.cml.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.yourname.cml.loader.CMLResourceReloader;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

// This class ONLY loads if GeckoLib is present (via reflection in CMLEntityRenderer)
public class GeckoLibEntityRenderer {
    public static void register() {
        for (EntityType<?> type : EntityType.REGISTRY) {
            EntityRendererRegistry.register((EntityType<?>) type, context -> {
                return new GeoEntityRenderer<>((EntityRendererFactory.Context) context) {
                    private String currentModelPath = null;
                    private String currentTexturePath = null;

                    @Override
                    public GeoModel getGeoModel() {
                        // Return dummy — we override in setCMLModel
                        return new GeoModel(new Identifier("cml", "dummy")) {};
                    }

                    public void setCMLModel(String modelPath, String texturePath) {
                        this.currentModelPath = modelPath;
                        this.currentTexturePath = texturePath;
                    }

                    @Override
                    public Identifier getTextureLocation(Object entity) {
                        return currentTexturePath != null 
                                ? Identifier.tryParse("minecraft:" + currentTexturePath)
                                : super.getTextureLocation(entity);
                    }
                };
            });
        }
    }
}
