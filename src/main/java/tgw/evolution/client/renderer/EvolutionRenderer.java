package tgw.evolution.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import tgw.evolution.blocks.tileentities.*;
import tgw.evolution.client.renderer.entities.*;
import tgw.evolution.client.renderer.tile.*;
import tgw.evolution.entities.*;

@OnlyIn(Dist.CLIENT)
public class EvolutionRenderer {

    public static void registryEntityRenders() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCow.class, RenderCow::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingWeight.class, RenderFallingWeight::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingPeat.class, RenderFallingPeat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingTimber.class, RenderFallingTimber::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBull.class, RenderBull::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityShadowHound.class, RenderShadowHound::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpear.class, RenderSpear::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHook.class, RenderHook::new);
        ClientRegistry.bindTileEntitySpecialRenderer(TEKnapping.class, new RenderTileKnapping());
        ClientRegistry.bindTileEntitySpecialRenderer(TEShadowHound.class, new RenderTileShadowHound());
        ClientRegistry.bindTileEntitySpecialRenderer(TEMolding.class, new RenderTileMolding());
        ClientRegistry.bindTileEntitySpecialRenderer(TEChopping.class, new RenderTileChopping());
        ClientRegistry.bindTileEntitySpecialRenderer(TEPitKiln.class, new RenderTilePitKiln());
        ClientRegistry.bindTileEntitySpecialRenderer(TESchematic.class, new RenderTileSchematic());
    }
}