package tgw.evolution.init;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import tgw.evolution.client.renderer.entities.*;
import tgw.evolution.client.renderer.tile.RenderTEChopping;
import tgw.evolution.client.renderer.tile.RenderTEMolding;
import tgw.evolution.client.renderer.tile.RenderTEPitKiln;
import tgw.evolution.client.renderer.tile.RenderTESchematic;

public final class EvolutionRenderer {

    private EvolutionRenderer() {
    }

    public static void registryEntityRenders() {
        //Entities
        EntityRenderers.register(EvolutionEntities.COW.get(), RenderCow::new);
        EntityRenderers.register(EvolutionEntities.FALLING_PEAT.get(), RenderFallingPeat::new);
        EntityRenderers.register(EvolutionEntities.FALLING_WEIGHT.get(), RenderFallingWeight::new);
        EntityRenderers.register(EvolutionEntities.HOOK.get(), RenderHook::new);
        EntityRenderers.register(EvolutionEntities.PLAYER_CORPSE.get(), RenderPlayerCorpse::new);
        EntityRenderers.register(EvolutionEntities.SIT.get(), RenderDummy::new);
        EntityRenderers.register(EvolutionEntities.SPEAR.get(), RenderSpear::new);
        EntityRenderers.register(EvolutionEntities.TORCH.get(), RenderTorch::new);
        //Tile Entities
        BlockEntityRenderers.register(EvolutionTEs.CHOPPING.get(), RenderTEChopping::new);
        BlockEntityRenderers.register(EvolutionTEs.MOLDING.get(), RenderTEMolding::new);
        BlockEntityRenderers.register(EvolutionTEs.PIT_KILN.get(), RenderTEPitKiln::new);
        BlockEntityRenderers.register(EvolutionTEs.SCHEMATIC.get(), RenderTESchematic::new);
    }
}