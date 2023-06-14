package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMinecraftPatch;

@Mixin(RenderStateShard.class)
public abstract class RenderStateShardMixin {

    @Mutable @Shadow @Final public static RenderStateShard.OutputStateShard PARTICLES_TARGET;
    @Mutable @Shadow @Final public static RenderStateShard.OutputStateShard WEATHER_TARGET;
    @Mutable @Shadow @Final public static RenderStateShard.OutputStateShard CLOUDS_TARGET;
    @Mutable @Shadow @Final protected static RenderStateShard.OutputStateShard OUTLINE_TARGET;
    @Mutable @Shadow @Final protected static RenderStateShard.OutputStateShard TRANSLUCENT_TARGET;

    @Mutable @Shadow @Final protected static RenderStateShard.OutputStateShard ITEM_ENTITY_TARGET;

    static {
        OUTLINE_TARGET = new RenderStateShard.OutputStateShard("outline_target",
                                                               () -> ((IMinecraftPatch) Minecraft.getInstance()).lvlRenderer()
                                                                                                                .entityTarget()
                                                                                                                .bindWrite(false),
                                                               () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));
        TRANSLUCENT_TARGET = new RenderStateShard.OutputStateShard("translucent_target",
                                                                   () -> {
                                                                       if (Minecraft.useShaderTransparency()) {
                                                                           ((IMinecraftPatch) Minecraft.getInstance()).lvlRenderer()
                                                                                                                      .getTranslucentTarget()
                                                                                                                      .bindWrite(false);
                                                                       }
                                                                   },
                                                                   () -> {
                                                                       if (Minecraft.useShaderTransparency()) {
                                                                           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                                                                       }
                                                                   });
        PARTICLES_TARGET = new RenderStateShard.OutputStateShard("particles_target",
                                                                 () -> {
                                                                     if (Minecraft.useShaderTransparency()) {
                                                                         ((IMinecraftPatch) Minecraft.getInstance()).lvlRenderer()
                                                                                                                    .getParticlesTarget()
                                                                                                                    .bindWrite(false);
                                                                     }
                                                                 },
                                                                 () -> {
                                                                     if (Minecraft.useShaderTransparency()) {
                                                                         Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                                                                     }
                                                                 });
        WEATHER_TARGET = new RenderStateShard.OutputStateShard("weather_target",
                                                               () -> {
                                                                   if (Minecraft.useShaderTransparency()) {
                                                                       ((IMinecraftPatch) Minecraft.getInstance()).lvlRenderer()
                                                                                                                  .getWeatherTarget()
                                                                                                                  .bindWrite(false);
                                                                   }
                                                               },
                                                               () -> {
                                                                   if (Minecraft.useShaderTransparency()) {
                                                                       Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                                                                   }
                                                               });
        CLOUDS_TARGET = new RenderStateShard.OutputStateShard("clouds_target",
                                                              () -> {
                                                                  if (Minecraft.useShaderTransparency()) {
                                                                      ((IMinecraftPatch) Minecraft.getInstance()).lvlRenderer()
                                                                                                                 .getCloudsTarget()
                                                                                                                 .bindWrite(false);
                                                                  }
                                                              },
                                                              () -> {
                                                                  if (Minecraft.useShaderTransparency()) {
                                                                      Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                                                                  }
                                                              });
        ITEM_ENTITY_TARGET = new RenderStateShard.OutputStateShard("item_entity_target",
                                                                   () -> {
                                                                       if (Minecraft.useShaderTransparency()) {
                                                                           ((IMinecraftPatch) Minecraft.getInstance()).lvlRenderer()
                                                                                                                      .getItemEntityTarget()
                                                                                                                      .bindWrite(false);
                                                                       }
                                                                   },
                                                                   () -> {
                                                                       if (Minecraft.useShaderTransparency()) {
                                                                           Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                                                                       }
                                                                   });
    }
}
