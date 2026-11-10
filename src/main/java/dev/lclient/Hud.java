package dev.lclient;

import dev.lclient.module.Modules;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Hud {
    static final int BG = 0x90101014, ACCENT = 0xFF7C5CFF, TEXT = 0xFFFFFFFF, DIM = 0xFFB0B0C0;

    public static void render(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        g.pose().pushMatrix();
        g.pose().scale(Modules.hudScale, Modules.hudScale);

        if (Modules.FPS.enabled)    pill(g, mc, Modules.FPS.x, Modules.FPS.y, mc.getFps() + " FPS");
        if (Modules.CPS.enabled)    pill(g, mc, Modules.CPS.x, Modules.CPS.y, Modules.leftCps() + " | " + Modules.rightCps() + " CPS");
        if (Modules.COORDS.enabled) pill(g, mc, Modules.COORDS.x, Modules.COORDS.y,
                (int) mc.player.getX() + ", " + (int) mc.player.getY() + ", " + (int) mc.player.getZ());
        if (Modules.CLOCK.enabled)  pill(g, mc, Modules.CLOCK.x, Modules.CLOCK.y,
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (Modules.KEYS.enabled)   keystrokes(g, mc, Modules.KEYS.x, Modules.KEYS.y);

        g.pose().popMatrix();
    }

    static void pill(GuiGraphicsExtractor g, Minecraft mc, int x, int y, String s) {
        int w = mc.font.width(s) + 12;
        g.fill(x, y, x + w, y + 13, BG);
        g.fill(x, y, x + 2, y + 13, ACCENT);
        g.text(mc.font, s, x + 7, y + 3, TEXT, false);
    }

    static void key(GuiGraphicsExtractor g, Minecraft mc, int x, int y, int w, int h, String label, boolean down) {
        g.fill(x, y, x + w, y + h, down ? 0xE0FFFFFF : BG);
        int tw = mc.font.width(label);
        g.text(mc.font, label, x + (w - tw) / 2, y + (h - 8) / 2, down ? 0xFF101014 : TEXT, false);
    }

    static void keystrokes(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        int s = 22, gap = 2;
        boolean[] k = Modules.keyDown;
        key(g, mc, x + s + gap, y, s, s, "W", k[0]);
        key(g, mc, x, y + s + gap, s, s, "A", k[1]);
        key(g, mc, x + s + gap, y + s + gap, s, s, "S", k[2]);
        key(g, mc, x + 2 * (s + gap), y + s + gap, s, s, "D", k[3]);
        int w = (3 * s + 2 * gap - gap) / 2;
        key(g, mc, x, y + 2 * (s + gap), w, 18, "LMB", k[4]);
        key(g, mc, x + w + gap, y + 2 * (s + gap), w, 18, "RMB", k[5]);
    }
}
