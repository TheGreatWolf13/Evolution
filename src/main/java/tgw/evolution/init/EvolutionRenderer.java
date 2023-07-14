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
//        EntityRenderers.register(EvolutionEntities.COW, RenderCow::new);
        EntityRenderers.register(EvolutionEntities.FALLING_PEAT, RenderFallingPeat::new);
        EntityRenderers.register(EvolutionEntities.FALLING_WEIGHT, RenderFallingWeight::new);
        EntityRenderers.register(EvolutionEntities.HOOK, RenderHook::new);
        EntityRenderers.register(EvolutionEntities.PLAYER_CORPSE, RenderPlayerCorpse::new);
        EntityRenderers.register(EvolutionEntities.SIT, RenderDummy::new);
        EntityRenderers.register(EvolutionEntities.SPEAR, RenderSpear::new);
        EntityRenderers.register(EvolutionEntities.TORCH, RenderTorch::new);
        //Tile Entities
        BlockEntityRenderers.register(EvolutionTEs.CHOPPING, RenderTEChopping::new);
        BlockEntityRenderers.register(EvolutionTEs.MOLDING, RenderTEMolding::new);
        BlockEntityRenderers.register(EvolutionTEs.PIT_KILN, RenderTEPitKiln::new);
        BlockEntityRenderers.register(EvolutionTEs.SCHEMATIC, RenderTESchematic::new);
    }
}