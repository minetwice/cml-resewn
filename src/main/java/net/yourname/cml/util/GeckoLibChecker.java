package net.yourname.cml.util;

public class GeckoLibChecker {
    private static Boolean geckoLibPresent;

    public static boolean isLoaded() {
        if (geckoLibPresent == null) {
            try {
                Class.forName("software.bernie.geckolib.GeckoLib");
                geckoLibPresent = true;
            } catch (ClassNotFoundException e) {
                geckoLibPresent = false;
            }
        }
        return geckoLibPresent;
    }

    public static void warnIfMissing() {
        if (!isLoaded()) {
            System.out.println("[CML] GeckoLib not found — custom mob geometry disabled. Texture/animation still works.");
        }
    }
}
