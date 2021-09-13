package tgw.evolution.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import tgw.evolution.client.renderer.entities.*;
import tgw.evolution.client.renderer.tile.RenderTEChopping;
import tgw.evolution.client.renderer.tile.RenderTEMolding;
import tgw.evolution.client.renderer.tile.RenderTEPitKiln;
import tgw.evolution.client.renderer.tile.RenderTESchematic;

@OnlyIn(Dist.CLIENT)
public final class EvolutionRenderer {

    private EvolutionRenderer() {
    }

    public static void registryEntityRenders() {
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.COW.get(), RenderCow::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.FALLING_PEAT.get(), RenderFallingPeat::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.FALLING_WEIGHT.get(), RenderFallingWeight::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.HOOK.get(), RenderHook::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.PLAYER_CORPSE.get(), RenderPlayerCorpse::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.SIT.get(), RenderDummy::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.SPEAR.get(), RenderSpear::new);
        RenderingRegistry.registerEntityRenderingHandler(EvolutionEntities.TORCH.get(), RenderTorch::new);
        ClientRegistry.bindTileEntityRenderer(EvolutionTEs.CHOPPING.get(), RenderTEChopping::new);
        ClientRegistry.bindTileEntityRenderer(EvolutionTEs.MOLDING.get(), RenderTEMolding::new);
        ClientRegistry.bindTileEntityRenderer(EvolutionTEs.PIT_KILN.get(), RenderTEPitKiln::new);
        ClientRegistry.bindTileEntityRenderer(EvolutionTEs.SCHEMATIC.get(), RenderTESchematic::new);
    }
}