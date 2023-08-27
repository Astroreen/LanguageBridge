package me.astroreen.languagebridge.compatibility.protocollib;

public class ProtocolLibManager {

    private static boolean isActive;

    public static void setup() {
        isActive = true;
    }

    public static void disable() {
        isActive = false;
    }

    public static boolean isActive() {
        return isActive;
    }
}
