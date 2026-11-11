# zNova

A client-side Fabric utility mod for **Minecraft 1.21.11** with a neon startup screen, a
glassmorphism ClickGUI and a small set of clean modules.

For local singleplayer testing and educational use only.

## Features

| Feature | Description |
| --- | --- |
| Custom splash | `#0A0A0F` backdrop, animated cyan/violet loading bar driven by the real resource-reload progress |
| ClickGUI (`Right Shift`) | Blurred dark glass, three draggable panels (Misc / Render / QoL), animated hovers and glowing toggles |
| Watermark (Render, on by default) | Top-left badge with the client name and live FPS |
| Fullbright (Render) | Client-side Night Vision with no particles and no HUD icon |
| AutoMiner (QoL) | Holds the attack key while a pickaxe (item tag) is in the main hand and no screen is open |

## 1.21.11 port notes

Minecraft 1.21.9+ reworked GUI and HUD rendering, so the port is not a pure version bump:

- `DrawContext.getMatrices()` is now a JOML `Matrix3x2fStack`
  (`pushMatrix` / `popMatrix` / `translate(x, y)` / `scale(x, y)`).
- Screens receive input through `Click` and `KeyInput` records instead of raw
  `(double, double, int)` / `(int, int, int)`.
- `HudRenderCallback` is deprecated and limited; the watermark is attached with
  `HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ...)`.
- `KeyBinding` requires a `KeyBinding.Category` (created from an `Identifier`).
- The lightmap is GPU-side now (`LightmapTextureManager` exposes no pixels), so Fullbright
  uses a client-side Night Vision effect instead of blending lightmap pixels.
- `PickaxeItem` no longer exists; AutoMiner tests `ItemStack#isIn(ItemTags.PICKAXES)`.
- The splash screen opens a new GUI root layer so it draws above the vanilla overlay.

## Build

Requires **JDK 21**, Gradle (recent; Loom 1.19 is used) and the 1.21.11 Yarn mappings.

```bash
# If you do not have a Gradle wrapper yet
gradle wrapper

# Build the mod
./gradlew build
# Windows: gradlew.bat build
```

The jar lands in `build/libs/znova-1.0.0.jar`. Drop it into `.minecraft/mods` together
with **Fabric Loader 0.20+** and **Fabric API 0.141.6+1.21.11**.

## Source layout

```
src/main/java/dev/znova/
  ZNovaClient.java                     client entrypoint, keybind, tick + HUD wiring
  mixin/SplashOverlayMixin.java        futuristic startup screen
  module/Module.java                   module base class + lifecycle hooks
  module/ModuleManager.java            module registry
  module/Category.java                 Misc / Render / QoL
  module/modules/*.java                Watermark, Fullbright, AutoMiner
  gui/ClickGuiScreen.java              Right Shift ClickGUI
  gui/widget/CategoryPanel.java        draggable glass panel
  gui/widget/ModuleButton.java         animated module row
  util/RenderUtils.java                rounded rects, glow, gradients, typography
  util/ColorUtils.java                 palette + ARGB math
  util/Animation.java                  frame-rate independent easing
```
