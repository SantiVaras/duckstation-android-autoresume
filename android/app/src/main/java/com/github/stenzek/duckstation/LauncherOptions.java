package com.github.stenzek.duckstation;

/** Type-tolerant launch options shared by ES-DE and other Android frontends. */
final class LauncherOptions {
    private LauncherOptions() { }

    static boolean resumeState(Object value, boolean fallback) {
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof Integer || value instanceof Long) {
            long number = ((Number) value).longValue();
            return number == 1 ? true : number == 0 ? false : fallback;
        }
        if (value instanceof String) {
            String text = ((String) value).trim();
            if (text.equalsIgnoreCase("true") || text.equals("1"))
                return true;
            if (text.equalsIgnoreCase("false") || text.equals("0"))
                return false;
        }
        return fallback;
    }
}
