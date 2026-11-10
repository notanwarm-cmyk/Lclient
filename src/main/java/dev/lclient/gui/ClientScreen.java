package dev.lclient.gui;

import dev.lclient.module.Module;
import dev.lclient.module.Modules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Clean sidebar GUI: categories on the left, toggle cards on the right. */
public class ClientScreen extends Screen {
    static final int PANEL = 0xF0121218, SIDE = 0xFF0C0C11, CARD = 0xFF1B1B24, CARD_HOVER = 0xFF23232F,
            ACCENT = 0xFF7C5CFF, TEXT = 0xFFFFFFFF, DIM = 0xFF8A8AA0, OFF = 0xFF3A3A48;

    private Module.Cat selected = Module.Cat.PERFORMANCE;
    private static final Module.Cat[] CATS = Module.Cat.values();
    private int px, py, pw = 440, ph = 270;
    private boolean dragSlider;

    public ClientScreen() { super(Component.literal("L Client")); }

    @Override protected void init() { px = (width - pw) / 2; py = (height - ph) / 2; }

    private static String catName(Module.Cat c) {
        return switch (c) { case PERFORMANCE -> "Performance"; case HUD -> "HUD"; case VISUAL -> "Visual"; };
    }

    private List<Module> list() {
        List<Module> l = new ArrayList<>();
        for (Module m : Modules.ALL) if (m.cat == selected) l.add(m);
        return l;
    }

    private int cx() { return px + 130; }
    private int cardW() { return pw - 130 - 12; }
    private int cy(int i) { return py + 44 + i * 40; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x70000000);
        g.fill(px, py, px + pw, py + ph, PANEL);
        g.fill(px, py, px + 118, py + ph, SIDE);
        g.fill(px, py, px + pw, py + 2, ACCENT);

        g.text(font, "L CLIENT", px + 14, py + 14, TEXT, false);
        g.text(font, "v1.0 - 26.2", px + 14, py + 26, DIM, false);

        for (int i = 0; i < CATS.length; i++) {
            int y = py + 52 + i * 24;
            boolean sel = CATS[i] == selected, hov = in(mx, my, px + 8, y, 102, 20);
            if (sel) { g.fill(px + 8, y, px + 110, y + 20, CARD); g.fill(px + 8, y, px + 10, y + 20, ACCENT); }
            else if (hov) g.fill(px + 8, y, px + 110, y + 20, 0xFF16161D);
            g.text(font, catName(CATS[i]), px + 18, y + 6, sel ? TEXT : DIM, false);
        }

        g.text(font, catName(selected), cx(), py + 16, TEXT, false);

        List<Module> mods = list();
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int y = cy(i);
            boolean hov = in(mx, my, cx(), y, cardW(), 34);
            g.fill(cx(), y, cx() + cardW(), y + 34, hov ? CARD_HOVER : CARD);
            g.text(font, m.name, cx() + 10, y + 7, TEXT, false);
            g.text(font, m.desc, cx() + 10, y + 19, DIM, false);
            int tx = cx() + cardW() - 36, ty = y + 10;
            g.fill(tx, ty, tx + 26, ty + 14, m.enabled ? ACCENT : OFF);
            int kx = m.enabled ? tx + 14 : tx + 2;
            g.fill(kx, ty + 2, kx + 10, ty + 12, 0xFFFFFFFF);
        }

        int sy = cy(mods.size()) + 4;
        if (selected == Module.Cat.HUD) slider(g, "HUD Scale", Modules.hudScale, 0.5f, 1.5f, sy);
        if (selected == Module.Cat.VISUAL) slider(g, "Motion Blur Strength", Modules.blurStrength, 0f, 1f, sy);

        g.text(font, "Right Shift to open - changes save automatically", px + 130, py + ph - 14, DIM, false);
    }

    private void slider(GuiGraphicsExtractor g, String label, float v, float min, float max, int y) {
        g.text(font, label + ": " + String.format("%.2f", v), cx(), y, TEXT, false);
        int bx = cx(), by = y + 14, bw = cardW();
        g.fill(bx, by, bx + bw, by + 4, OFF);
        int fill = (int) ((v - min) / (max - min) * bw);
        g.fill(bx, by, bx + fill, by + 4, ACCENT);
        g.fill(bx + fill - 3, by - 3, bx + fill + 3, by + 7, 0xFFFFFFFF);
    }

    private boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int sliderY() { return cy(list().size()) + 4 + 14; }

    private void setSlider(double mx) {
        float t = (float) Math.max(0, Math.min(1, (mx - cx()) / cardW()));
        if (selected == Module.Cat.HUD) Modules.hudScale = 0.5f + t;
        else if (selected == Module.Cat.VISUAL) Modules.blurStrength = t;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        for (int i = 0; i < CATS.length; i++)
            if (in(mx, my, px + 8, py + 52 + i * 24, 102, 20)) { selected = CATS[i]; return true; }
        List<Module> mods = list();
        for (int i = 0; i < mods.size(); i++)
            if (in(mx, my, cx(), cy(i), cardW(), 34)) {
                mods.get(i).enabled = !mods.get(i).enabled;
                Modules.applyPerformance(); Modules.save();
                return true;
            }
        if ((selected == Module.Cat.HUD || selected == Module.Cat.VISUAL)
                && in(mx, my, cx(), sliderY() - 6, cardW(), 18)) { dragSlider = true; setSlider(mx); return true; }
        return super.mouseClicked(e, dbl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragSlider) { setSlider(e.x()); return true; }
        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (dragSlider) { dragSlider = false; Modules.save(); return true; }
        return super.mouseReleased(e);
    }

    @Override public boolean isPauseScreen() { return false; }
}
