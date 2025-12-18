package net.yourname.cml.animation;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.function.Supplier;

public class AnimatedCMLModel implements BakedModel {
    private final BakedModel delegate;
    private final CMLAnimationManager animationManager;

    public AnimatedCMLModel(BakedModel delegate, CMLAnimationManager animationManager) {
        this.delegate = delegate;
        this.animationManager = animationManager;
    }

    @Override
    public List<BakedQuad> getQuads(Object blockState, net.minecraft.util.math.Direction face, Random random) {
        // Geometry is static — animation applied in entity/block/item renderer via matrix
        return delegate.getQuads(blockState, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
    @Override
    public boolean hasDepth() { return delegate.hasDepth(); }
    @Override
    public boolean isSideLit() { return delegate.isSideLit(); }
    @Override
    public boolean isBuiltin() { return delegate.isBuiltin(); }
    @Override
    public Sprite getParticleSprite() { return delegate.getParticleSprite(); }
    @Override
    public ModelTransformation getTransformation() { return delegate.getTransformation(); }

    // --- Animation API ---
    public CMLAnimationManager getAnimationManager() {
        return animationManager;
    }

    // Helper to apply bone transform in renderers
    public void applyBoneTransform(String bone, net.minecraft.client.util.math.MatrixStack matrices) {
        float rx = animationManager.getRotationX(bone);
        float ry = animationManager.getRotationY(bone);
        float rz = animationManager.getRotationZ(bone);
        float sx = animationManager.getScaleX(bone);

        matrices.translate(0.5f, 0.5f, 0.5f); // center pivot
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(rx));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(ry));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(rz));
        if (sx != 1f) matrices.scale(sx, sx, sx);
        matrices.translate(-0.5f, -0.5f, -0.5f);
    }
}
