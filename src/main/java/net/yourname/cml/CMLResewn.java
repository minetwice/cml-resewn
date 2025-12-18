package net.yourname.cml;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.yourname.cml.block.CMLBlockRenderer;
import net.yourname.cml.entity.CMLEntityRenderer;
import net.yourname.cml.loader.CMLResourceReloader;
import net.yourname.cml.render.CMLItemRenderer;

public class CMLResewn implements ClientModInitializer {
    public static final String MOD_ID = "cml-resewn";

    @Override
    public void onInitializeClient() {
        // Register resource reloader (scans /optifine/cml/, cbl/, cem/)
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new CMLResourceReloader());

        // Register renderers
        CMLItemRenderer.register();   // Items (with animation)
        CMLBlockRenderer.register();  // Blocks (with .mcmeta animation)
        CMLEntityRenderer.register(); // Entities (texture + matrix anim)
    }
}
