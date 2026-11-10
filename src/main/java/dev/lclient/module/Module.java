package dev.lclient.module;

public class Module {
    public enum Cat { PERFORMANCE, HUD, VISUAL }
    public final String id, name, desc;
    public final Cat cat;
    public boolean enabled;
    public int x, y;                 // HUD position
    public Module(String id, String name, String desc, Cat cat, boolean enabled, int x, int y) {
        this.id = id; this.name = name; this.desc = desc; this.cat = cat;
        this.enabled = enabled; this.x = x; this.y = y;
    }
}
