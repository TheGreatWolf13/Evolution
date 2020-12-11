package tgw.evolution.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import tgw.evolution.blocks.tileentities.*;
import tgw.evolution.client.renderer.entities.*;
import tgw.evolution.client.renderer.tile.*;
import tgw.evolution.entities.EntityCow;
import tgw.evolution.entities.misc.EntityFallingPeat;
import tgw.evolution.entities.misc.EntityFallingTimber;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.entities.projectiles.EntityHook;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.entities.projectiles.EntityTorch;

@OnlyIn(Dist.CLIENT)
public final class EvolutionRenderer {

    private EvolutionRenderer() {
    }

    public static void registryEntityRenders() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCow.class, RenderCow::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingWeight.class, RenderFallingWeight::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingPeat.class, RenderFallingPeat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingTimber.class, RenderFallingTimber::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityBull.class, RenderBull::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityShadowHound.class, RenderShadowHound::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpear.class, RenderSpear::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHook.class, RenderHook::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTorch.class, RenderTorch::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerCorpse.class, RenderPlayerCorpse::new);
        ClientRegistry.bindTileEntitySpecialRenderer(TEKnapping.class, new RenderTileKnapping());
//        ClientRegistry.bindTileEntitySpecialRenderer(TEShadowHound.class, new RenderTileShadowHound());
        ClientRegistry.bindTileEntitySpecialRenderer(TEMolding.class, new RenderTileMolding());
        ClientRegistry.bindTileEntitySpecialRenderer(TEChopping.class, new RenderTileChopping());
        ClientRegistry.bindTileEntitySpecialRenderer(TEPitKiln.class, new RenderTilePitKiln());
        ClientRegistry.bindTileEntitySpecialRenderer(TESchematic.class, new RenderTileSchematic());
    }
}