package net.yourname.cml.block;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public class CMLBlockRenderer {
    public static void register() {
        WorldRenderEvents.BEFORE_BLOCK_RENDER.register((context, blockState, model, vertexConsumer, matrix, seed) -> {
            BakedModel custom = CMLBlockModelProvider.getAnimatedModelFor(blockState, context.getModelLoader());
            if (custom != null) {
                context.setCustomModel(custom);
                return true; // override
            }
            return false;
        });
    }
}
