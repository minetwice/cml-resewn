package net.yourname.cml.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class CMLAnimationManager {
    private final String modelId;
    private final Map<String, Keyframes> boneAnimations = new HashMap<>();

    public CMLAnimationManager(String modelId, JsonObject animationJson) {
        this.modelId = modelId;
        parseAnimations(animationJson);
    }

    private void parseAnimations(JsonObject animationJson) {
        if (animationJson.has("animations")) {
            JsonArray anims = animationJson.getAsJsonArray("animations");
            for (var elem : anims) {
                if (elem.isJsonObject()) {
                    JsonObject anim = elem.getAsJsonObject();
                    String bone = anim.get("bone").getAsString();
                    JsonArray frames = anim.getAsJsonArray("keyframes");
                    boneAnimations.put(bone, new Keyframes(frames));
                }
            }
        }
    }

    public float getRotationX(String bone) {
        return boneAnimations.getOrDefault(bone, Keyframes.EMPTY).getRotationX();
    }

    public float getRotationY(String bone) {
        return boneAnimations.getOrDefault(bone, Keyframes.EMPTY).getRotationY();
    }

    public float getRotationZ(String bone) {
        return boneAnimations.getOrDefault(bone, Keyframes.EMPTY).getRotationZ();
    }

    public float getScaleX(String bone) {
        return boneAnimations.getOrDefault(bone, Keyframes.EMPTY).getScaleX();
    }

    // --- Inner class: Keyframes ---
    public static class Keyframes {
        public static final Keyframes EMPTY = new Keyframes(null);

        private final float[] timeStamps;
        private final float[] rotX, rotY, rotZ;
        private final float[] scaleX, scaleY, scaleZ;

        public Keyframes(JsonArray keyframes) {
            if (keyframes == null || keyframes.isEmpty()) {
                timeStamps = new float[]{0};
                rotX = rotY = rotZ = scaleX = scaleY = scaleZ = new float[]{0};
                return;
            }

            int n = keyframes.size();
            timeStamps = new float[n];
            rotX = new float[n]; rotY = new float[n]; rotZ = new float[n];
            scaleX = new float[n]; scaleY = new float[n]; scaleZ = new float[n];

            for (int i = 0; i < n; i++) {
                JsonObject kf = keyframes.get(i).getAsJsonObject();
                timeStamps[i] = kf.get("time").getAsFloat();
                JsonObject rot = kf.has("rotation") ? kf.getAsJsonObject("rotation") : new JsonObject();
                JsonObject scale = kf.has("scale") ? kf.getAsJsonObject("scale") : new JsonObject();

                rotX[i] = rot.has("x") ? rot.get("x").getAsFloat() : 0f;
                rotY[i] = rot.has("y") ? rot.get("y").getAsFloat() : 0f;
                rotZ[i] = rot.has("z") ? rot.get("z").getAsFloat() : 0f;

                scaleX[i] = scale.has("x") ? scale.get("x").getAsFloat() : 1f;
                scaleY[i] = scale.has("y") ? scale.get("y").getAsFloat() : 1f;
                scaleZ[i] = scale.has("z") ? scale.get("z").getAsFloat() : 1f;
            }
        }

        private float interpolate(float[] values, float time) {
            if (timeStamps.length == 1) return values[0];
            time = time % timeStamps[timeStamps.length - 1]; // loop

            int i = 0;
            while (i < timeStamps.length - 1 && timeStamps[i + 1] <= time) i++;

            if (i == timeStamps.length - 1) return values[i];

            float t0 = timeStamps[i], t1 = timeStamps[i + 1];
            float v0 = values[i], v1 = values[i + 1];
            float alpha = MathHelper.clamp((time - t0) / (t1 - t0), 0, 1);
            return MathHelper.lerp(alpha, v0, v1);
        }

        public float getRotationX() { return interpolate(rotX, (float) MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true)); }
        public float getRotationY() { return interpolate(rotY, (float) MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true)); }
        public float getRotationZ() { return interpolate(rotZ, (float) MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true)); }
        public float getScaleX() { return interpolate(scaleX, (float) MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true)); }
    }
}
