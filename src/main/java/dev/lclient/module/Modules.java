package dev.lclient.module;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import org.lwjgl.glfw.GLFW;

import java.nio.file.*;
import java.util.*;

public class Modules {
    public static final List<Module> ALL = new ArrayList<>();
    public static float hudScale = 1.0f;
    public static float blurStrength = 0.5f;

    public static Module FPS, CPS, KEYS, COORDS, CLOCK, BOOST, NOCLOUDS, NOSHADOW, NOPARTICLES, MOTIONBLUR, NAMETAG;

    static Module add(Module m) { ALL.add(m); return m; }

    public static void init() {
        FPS    = add(new Module("fps", "FPS", "Show frames per second", Module.Cat.HUD, true, 6, 6));
        CPS    = add(new Module("cps", "CPS", "Clicks per second", Module.Cat.HUD, true, 6, 20));
        COORDS = add(new Module("coords", "Coordinates", "XYZ position", Module.Cat.HUD, true, 6, 34));
        CLOCK  = add(new Module("clock", "Clock", "Real time", Module.Cat.HUD, false, 6, 48));
        KEYS   = add(new Module("keys", "Keystrokes", "WASD + mouse buttons", Module.Cat.HUD, true, 6, 70));
        BOOST       = add(new Module("boost", "FPS Boost", "Lower render/entity distance", Module.Cat.PERFORMANCE, false, 0, 0));
        NOCLOUDS    = add(new Module("noclouds", "No Clouds", "Disable clouds", Module.Cat.PERFORMANCE, false, 0, 0));
        NOSHADOW    = add(new Module("noshadow", "No Entity Shadows", "Disable entity shadows", Module.Cat.PERFORMANCE, false, 0, 0));
        NOPARTICLES = add(new Module("noparticles", "Minimal Particles", "Reduce particles", Module.Cat.PERFORMANCE, false, 0, 0));
        MOTIONBLUR  = add(new Module("motionblur", "Motion Blur", "Smooth motion effect", Module.Cat.VISUAL, false, 0, 0));
        NAMETAG     = add(new Module("nametag", "Custom Nametag Icon", "Icon next to your name", Module.Cat.VISUAL, false, 0, 0));
        load();
    }

    public static void applyPerformance() {
        var o = Minecraft.getInstance().options;
        if (o == null) return;
        if (NOCLOUDS.enabled) o.cloudStatus().set(CloudStatus.OFF);
        if (NOSHADOW.enabled) o.entityShadows().set(false);
        if (NOPARTICLES.enabled) o.particles().set(ParticleStatus.MINIMAL);
        if (BOOST.enabled) {
            o.renderDistance().set(Math.min(o.renderDistance().get(), 8));
            o.entityDistanceScaling().set(0.75);
        }
    }

    private static final ArrayDeque<Long> lc = new ArrayDeque<>(), rc = new ArrayDeque<>();
    private static boolean lw, rw;
    public static boolean[] keyDown = new boolean[6];

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;
        long h = mc.getWindow().handle();
        boolean l = GLFW.glfwGetMouseButton(h, 0) == 1, r = GLFW.glfwGetMouseButton(h, 1) == 1;
        long now = System.currentTimeMillis();
        if (l && !lw) lc.add(now);
        if (r && !rw) rc.add(now);
        lw = l; rw = r;
        while (!lc.isEmpty() && now - lc.peek() > 1000) lc.poll();
        while (!rc.isEmpty() && now - rc.peek() > 1000) rc.poll();
        var o = mc.options;
        keyDown[0] = o.keyUp.isDown(); keyDown[1] = o.keyLeft.isDown();
        keyDown[2] = o.keyDown.isDown(); keyDown[3] = o.keyRight.isDown();
        keyDown[4] = l; keyDown[5] = r;
    }
    public static int leftCps()  { return lc.size(); }
    public static int rightCps() { return rc.size(); }

    static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("lclient.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("hudScale", hudScale);
            root.addProperty("blur", blurStrength);
            JsonObject mods = new JsonObject();
            for (Module m : ALL) {
                JsonObject o = new JsonObject();
                o.addProperty("on", m.enabled); o.addProperty("x", m.x); o.addProperty("y", m.y);
                mods.add(m.id, o);
            }
            root.add("modules", mods);
            Files.writeString(file(), new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception ignored) {}
    }

    static void load() {
        try {
            if (!Files.exists(file())) return;
            JsonObject root = JsonParser.parseString(Files.readString(file())).getAsJsonObject();
            if (root.has("hudScale")) hudScale = root.get("hudScale").getAsFloat();
            if (root.has("blur")) blurStrength = root.get("blur").getAsFloat();
            JsonObject mods = root.getAsJsonObject("modules");
            for (Module m : ALL) if (mods != null && mods.has(m.id)) {
                JsonObject o = mods.getAsJsonObject(m.id);
                m.enabled = o.get("on").getAsBoolean(); m.x = o.get("x").getAsInt(); m.y = o.get("y").getAsInt();
            }
        } catch (Exception ignored) {}
    }
}
