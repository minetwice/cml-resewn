package net.yourname.cml.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.yourname.cml.entity.CMLEntityModelProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderers.class)
public class EntityRenderDispatcherMixin {
    @ModifyArg(
            method = "register",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRendererFactory;create(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;)Lnet/minecraft/client/render/entity/EntityRenderer;"),
            index = 0
    )
    private static <T extends Entity> EntityRendererFactory.Context wrapContext(EntityRendererFactory.Context context, EntityType<T> type) {
        // Inject animated model loader into context (advanced — optional)
        return context;
    }
}
