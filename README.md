# zNova

A client-side Fabric QoL and visual utility mod for **Minecraft 1.21.11** with a neon
startup screen, a glassmorphism ClickGUI with per-module settings and keybinds, a HUD array
list and a small set of clean utility modules.

> **Disclaimer:** zNova is a quality-of-life and visual utility mod, strictly intended for
> singleplayer and quality-of-life enhancement. It is **not** a cheat client: it contains
> no movement, reach, combat or anticheat-bypass automation.

## Features

| Feature | Description |
| --- | --- |
| Custom splash | `#0A0A0F` backdrop, animated cyan/violet loading bar driven by the real resource-reload progress |
| ClickGUI (`Right Shift`) | Blurred dark glass, three compact draggable panels (Misc / Render / QoL) that resize to the window and remember their positions |
| Inline settings | Right-click any module for a dropdown card with its keybind and module-specific settings |
| Universal keybinds | Every module can be bound to a key from its settings card; `ESC` / `DELETE` clears the bind |
| Watermark (Render, on by default) | Top-left badge with the client name, optional live FPS and optional coordinates |
| ArrayList (Render) | Top-right (or bottom-right) list of enabled modules, sorted by width, with slide-in/fade and cyan accent bars |
| Fullbright (Render) | Client-side Night Vision plus a draggable brightness (gamma) slider |
| AutoMiner (QoL) | Holds the attack key while a pickaxe is in the main hand, optionally only when the crosshair is on a block |
| AutoWalk (QoL) | Holds the forward key, optionally sprinting as well |
| AutoJump (QoL) | Holds the jump key while no screen is open |

## Module settings

- **Fullbright** — `Brightness` slider (`10%`–`100%`, drives the vanilla gamma option in
  real time and restores the previous value on disable).
- **Watermark** — `Show FPS`, `Show Coordinates`.
- **ArrayList** — `Show Keybinds`, `Position` (`Top-Right` / `Bottom-Right`).
- **AutoMiner** — `Only When Looking At Block`.
- **AutoWalk** — `Sprint` (also holds the sprint key while walking).
- **AutoJump** — no extra settings.
- **Every module** — `Keybind` (click the chip, press a key; `ESC` or `DELETE` unbinds;
  pressing the bound key in-game toggles the module).

## Rendering notes

All shapes are drawn through `RenderUtils`, which rasterises rounded corners and 1px
borders at half-pixel precision by scaling the JOML matrix by `0.5`, so outlines stay razor
sharp at every GUI scale instead of stair-stepping.

## 1.21.11 port notes

Minecraft 1.21.9+ reworked GUI and HUD rendering, so this is not a pure version bump:

- `DrawContext.getMatrices()` is a JOML `Matrix3x2fStack`
  (`pushMatrix` / `popMatrix` / `translate(x, y)` / `scale(x, y)`).
- Screens receive input through `Click` and `KeyInput` records.
- `HudRenderCallback` is deprecated and limited; HUD modules are attached with
  `HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ...)`.
- `KeyBinding` requires a `KeyBinding.Category`.
- The lightmap is GPU-side, so Fullbright uses Night Vision instead of blending lightmap
  pixels, and the brightness slider drives the vanilla gamma option (vanilla clamps gamma
  to `0.0 .. 1.0`, hence the percentage range).
- `PickaxeItem` no longer exists; AutoMiner tests `ItemStack#isIn(ItemTags.PICKAXES)`.
- The splash screen opens a new GUI root layer so it draws above the vanilla overlay.

## Build

Requires **JDK 21+** (compiles with `options.release = 21`, so JDK 25 works), Gradle and the
1.21.11 Yarn mappings.

```bash
# If you do not have a Gradle wrapper yet
gradle wrapper

# Build the mod
./gradlew build
# Windows: gradlew.bat build
```

The jar lands in `build/libs/znova-1.2.0.jar`. Drop it into `.minecraft/mods` together with
**Fabric Loader 0.20+** and **Fabric API 0.141.6+1.21.11**.

## Source layout

```
src/main/java/dev/znova/
  ZNovaClient.java                     client entrypoint, keybind, tick + HUD wiring
  mixin/SplashOverlayMixin.java        futuristic startup screen
  module/Module.java                   module base class: settings, keybind, lifecycle
  module/ModuleManager.java            registry, keybind dispatch, HUD fan-out
  module/Category.java                 Misc / Render / QoL
  module/setting/*.java                Boolean, slider and mode settings
  module/modules/*.java                Watermark, ArrayList, Fullbright, AutoMiner, AutoWalk, AutoJump
  gui/ClickGuiScreen.java              Right Shift ClickGUI + keybind capture
  gui/PanelLayout.java                 saved panel positions (config/znova-panels.json)
  gui/widget/CategoryPanel.java        draggable glass panel
  gui/widget/ModuleButton.java         module row + inline settings card
  util/RenderUtils.java                crisp rounded shapes, glow, gradients, typography
  util/ColorUtils.java                 palette + ARGB math
  util/Animation.java                  frame-rate independent easing
  util/KeyNames.java                   GLFW key code display names
```
