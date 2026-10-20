package com.github.stenzek.duckstation;

import org.junit.Test;
import static org.junit.Assert.*;

public class LauncherOptionsTest {
    @Test public void acceptsLauncherBooleanExtra() {
        assertTrue(LauncherOptions.resumeState(true, false));
        assertFalse(LauncherOptions.resumeState(false, true));
    }

    @Test public void acceptsIntegerAndStringExtras() {
        for (Object value : new Object[]{1, 1L, "true", " TRUE ", "1"})
            assertTrue(LauncherOptions.resumeState(value, false));
        for (Object value : new Object[]{0, 0L, "false", " FALSE ", "0"})
            assertFalse(LauncherOptions.resumeState(value, true));
    }

    @Test public void missingOrInvalidExtraUsesRequestedDefault() {
        for (Object value : new Object[]{null, "", "invalid", 2, -1, new Object()}) {
            assertTrue(LauncherOptions.resumeState(value, true));
            assertFalse(LauncherOptions.resumeState(value, false));
        }
    }
}
