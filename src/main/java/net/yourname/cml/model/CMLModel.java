package net.yourname.cml.model;

import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class CMLModel implements UnbakedModel {
    private final JsonUnbakedModel delegate;

    public CMLModel(JsonUnbakedModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return delegate.getModelDependencies();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        delegate.setParents(modelLoader);
    }

    @Override
    public BakedModel bake(
            ModelLoader loader,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer,
            Identifier modelId
    ) {
        return delegate.bake(loader, textureGetter, rotationContainer, modelId);
    }

    // Optional: Override getTransformation() if you want custom item/block placement
    @Override
    public ModelTransformation getTransformation() {
        return delegate.getTransformation();
    }
}
