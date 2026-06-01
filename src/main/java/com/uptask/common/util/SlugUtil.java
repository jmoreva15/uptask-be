package com.uptask.common.util;

import java.util.Locale;

public final class SlugUtil {

    private SlugUtil() {}

    public static String from(String input) {
        return input.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }
}
