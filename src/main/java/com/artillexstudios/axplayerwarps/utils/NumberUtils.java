package com.artillexstudios.axplayerwarps.utils;

public final class NumberUtils {

    private NumberUtils() {
    }

    public static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
