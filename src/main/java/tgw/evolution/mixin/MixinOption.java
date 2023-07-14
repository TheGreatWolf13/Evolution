package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchMinecraft;
import tgw.evolution.patches.PatchNarratorChatListener;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(Option.class)
public abstract class MixinOption {

    @Mutable @Shadow @Final public static ProgressOption BIOME_BLEND_RADIUS;
    @Mutable @Shadow @Final public static ProgressOption FOV;
    @Mutable @Shadow @Final public static CycleOption<NarratorStatus> NARRATOR;
    @Mutable @Shadow @Final public static ProgressOption RENDER_DISTANCE;
    @Mutable @Shadow @Final public static CycleOption<AmbientOcclusionStatus> AMBIENT_OCCLUSION;
    @Mutable @Shadow @Final public static CycleOption<GraphicsStatus> GRAPHICS;
    @Mutable @Shadow @Final public static CycleOption<CloudStatus> RENDER_CLOUDS;

    @Shadow @Final private static Component GRAPHICS_TOOLTIP_FAST;
    @Shadow @Final private static Component GRAPHICS_TOOLTIP_FANCY;
    @Shadow @Final private static Component GRAPHICS_TOOLTIP_FABULOUS;

    static {
        BIOME_BLEND_RADIUS = new ProgressOption("options.biomeBlendRadius",
                                                0,
                                                7,
                                                1,
                                                op -> (double) op.biomeBlendRadius,
                                                (op, d) -> {
                                                    op.biomeBlendRadius = Mth.clamp(d.intValue(), 0, 7);
                                                    ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().allChanged();
                                                },
                                                (ops, op) -> {
                                                    double value = op.get(ops);
                                                    int i = (int) value * 2 + 1;
                                                    return op.genericValueLabel(new TranslatableComponent("options.biomeBlendRadius." + i));
                                                });
        FOV = new ProgressOption("options.fov",
                                 30,
                                 110,
                                 1,
                                 op -> op.fov,
                                 (ops, d) -> {
                                     ops.fov = d;
                                     ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().needsUpdate();
                                 },
                                 (ops, op) -> {
                                     double value = op.get(ops);
                                     if (value == 70) {
                                         return op.genericValueLabel(new TranslatableComponent("options.fov.min"));
                                     }
                                     return value == op.getMaxValue() ?
                                            op.genericValueLabel(new TranslatableComponent("options.fov.max")) :
                                            op.genericValueLabel((int) value);
                                 });
        NARRATOR = CycleOption.create("options.narrator",
                                      NarratorStatus.values(),
                                      status -> ((PatchNarratorChatListener) NarratorChatListener.INSTANCE).isAvailable() ?
                                                status.getName() :
                                                new TranslatableComponent("options.narrator.notavailable"),
                                      op -> op.narratorStatus,
                                      (ops, op, status) -> {
                                          ops.narratorStatus = status;
                                          NarratorChatListener.INSTANCE.updateNarratorStatus(status);
                                      });
        RENDER_DISTANCE = new ProgressOption("options.renderDistance",
                                             2,
                                             16,
                                             1,
                                             op -> (double) op.renderDistance,
                                             (op, d) -> {
                                                 op.renderDistance = d.intValue();
                                                 ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().needsUpdate();
                                             },
                                             (ops, op) -> {
                                                 double value = op.get(ops);
                                                 return op.genericValueLabel(new TranslatableComponent("options.chunks", (int) value));
                                             });
        AMBIENT_OCCLUSION = CycleOption.create("options.ao",
                                               AmbientOcclusionStatus.values(),
                                               status -> new TranslatableComponent(status.getKey()),
                                               op -> op.ambientOcclusion,
                                               (ops, op, st) -> {
                                                   ops.ambientOcclusion = st;
                                                   ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().allChanged();
                                               });
        GRAPHICS = CycleOption.create("options.graphics",
                                      Arrays.asList(GraphicsStatus.values()),
                                      Stream.of(GraphicsStatus.values())
                                            .filter(status -> status != GraphicsStatus.FABULOUS)
                                            .collect(Collectors.toList()),
                                      () -> Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(),
                                      status -> {
                                          MutableComponent component = new TranslatableComponent(status.getKey());
                                          return status == GraphicsStatus.FABULOUS ?
                                                 component.withStyle(ChatFormatting.ITALIC) :
                                                 component;
                                      },
                                      op -> op.graphicsMode,
                                      (ops, op, status) -> {
                                          Minecraft mc = Minecraft.getInstance();
                                          GpuWarnlistManager warnlistManager = mc.getGpuWarnlistManager();
                                          if (status == GraphicsStatus.FABULOUS && warnlistManager.willShowWarning()) {
                                              warnlistManager.showWarning();
                                          }
                                          else {
                                              ops.graphicsMode = status;
                                              ((PatchMinecraft) mc).lvlRenderer().allChanged();
                                          }
                                      }).setTooltip(mc -> status -> switch (status) {
            case FANCY -> Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FANCY, 200);
            case FAST -> Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FAST, 200);
            case FABULOUS -> Minecraft.getInstance().font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
        });
        RENDER_CLOUDS = CycleOption.create("options.renderClouds",
                                           CloudStatus.values(),
                                           status -> new TranslatableComponent(status.getKey()),
                                           op -> op.renderClouds,
                                           (ops, op, status) -> {
                                               ops.renderClouds = status;
                                               if (Minecraft.useShaderTransparency()) {
                                                   RenderTarget target = ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().getCloudsTarget();
                                                   target.clear(Minecraft.ON_OSX);
                                               }
                                           });
    }
}
