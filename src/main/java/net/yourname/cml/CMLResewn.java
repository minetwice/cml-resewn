package net.yourname.cml;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.resource.ResourceType;
import net.yourname.cml.loader.CMLResourceReloader;
import net.yourname.cml.render.CMLItemRenderer;

public class CMLResewn implements ModInitializer {
    public static final String MOD_ID = "cml-resewn";

    @Override
    public void onInitialize() {
        // Register resource reloader for /optifine/cml/
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new CMLResourceReloader());

        // Override item renderer globally
        CMLItemRenderer.register();
    }
}
