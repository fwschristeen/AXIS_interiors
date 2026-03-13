package com.furniturevision.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages Light / Dark theme switching across the application.
 * All UI components should read colours from here.
 * Call ThemeManager.toggle() to switch modes at runtime.
 */
public class ThemeManager {

    public enum Mode { LIGHT, DARK }

    private static Mode currentMode = Mode.LIGHT;  // default = light

    // Listeners that get notified on theme change
    private static final List<Runnable> listeners = new ArrayList<>();

    // ===========================================
    //  THEME-AWARE COLOURS (read via getters)
    // ===========================================

    // -- backgrounds --
    public static Color bgPrimary()    { return currentMode == Mode.DARK ? new Color(15, 15, 26)  : new Color(255, 255, 255); }
    public static Color bgSecondary()  { return currentMode == Mode.DARK ? new Color(26, 26, 46)  : new Color(248, 248, 252); }
    public static Color bgTertiary()   { return currentMode == Mode.DARK ? new Color(37, 37, 64)  : new Color(240, 240, 246); }
    public static Color surface()      { return currentMode == Mode.DARK ? new Color(42, 42, 74)  : new Color(255, 255, 255); }
    public static Color surfaceHover() { return currentMode == Mode.DARK ? new Color(52, 52, 90)  : new Color(245, 245, 250); }

    // -- text --
    public static Color textPrimary()  { return currentMode == Mode.DARK ? new Color(240, 240, 255) : new Color(30, 30, 40); }
    public static Color textSecondary(){ return currentMode == Mode.DARK ? new Color(160, 160, 200) : new Color(100, 100, 120); }
    public static Color textMuted()    { return currentMode == Mode.DARK ? new Color(100, 100, 140) : new Color(160, 160, 175); }
    public static Color textPlaceholder(){ return currentMode == Mode.DARK ? new Color(100, 100, 140) : new Color(180, 180, 195); }

    // -- border --
    public static Color border()       { return currentMode == Mode.DARK ? new Color(58, 58, 92)   : new Color(215, 215, 225); }
    public static Color borderLight()  { return currentMode == Mode.DARK ? new Color(70, 70, 110)  : new Color(230, 230, 240); }

    // -- accent --
    public static Color accent()       { return new Color(108, 99, 255); }   // purple — same in both
    public static Color accentLight()  { return new Color(139, 131, 255); }
    public static Color accentDark()   { return new Color(80, 72, 200); }

    // -- semantic --
    public static Color success()      { return new Color(76, 175, 80); }
    public static Color warning()      { return new Color(255, 152, 0); }
    public static Color error()        { return new Color(244, 67, 54); }

    // -- special --
    public static Color fieldBg()      { return currentMode == Mode.DARK ? new Color(37, 37, 64)  : new Color(255, 255, 255); }
    public static Color fieldBorder()  { return currentMode == Mode.DARK ? new Color(58, 58, 92)  : new Color(200, 200, 215); }
    public static Color buttonPrimary(){ return currentMode == Mode.DARK ? new Color(108, 99, 255) : new Color(50, 50, 60); }
    public static Color buttonText()   { return Color.WHITE; }

    // -- sidebar --
    public static Color sidebarBg()    { return currentMode == Mode.DARK ? new Color(18, 18, 32)  : new Color(250, 250, 255); }
    public static Color sidebarHover() { return currentMode == Mode.DARK ? new Color(255,255,255,8) : new Color(0,0,0,8); }
    public static Color sidebarActive(){ return currentMode == Mode.DARK ? new Color(108,99,255,30) : new Color(108,99,255,15); }

    // -- canvas --
    public static Color canvasBg()     { return new Color(245, 242, 235); }  // always light
    public static Color gridColor()    { return new Color(220, 215, 205); }

    // -- selection --
    public static Color selection()    { return new Color(108, 99, 255, 100); }

    // -- card / form --
    public static Color cardBg()       { return currentMode == Mode.DARK ? new Color(26, 26, 46) : Color.WHITE; }
    public static Color cardBorder()   { return currentMode == Mode.DARK ? new Color(58, 58, 92) : new Color(230, 230, 235); }

    // ===========================================
    //  STATE
    // ===========================================

    public static Mode getMode() { return currentMode; }
    public static boolean isDark() { return currentMode == Mode.DARK; }

    public static void setMode(Mode mode) {
        if (currentMode != mode) {
            currentMode = mode;
            notifyListeners();
        }
    }

    public static void toggle() {
        setMode(currentMode == Mode.DARK ? Mode.LIGHT : Mode.DARK);
    }

    // ===========================================
    //  LISTENERS
    // ===========================================

    public static void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    public static void removeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
