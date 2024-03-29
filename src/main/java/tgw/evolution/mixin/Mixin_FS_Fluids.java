package tgw.evolution.mixin;

import net.minecraft.core.Registry;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OList;

@Mixin(Fluids.class)
public abstract class Mixin_FS_Fluids {

    @Mutable @Shadow @Final @RestoreFinal public static Fluid EMPTY;
    @Mutable @Shadow @Final @RestoreFinal public static FlowingFluid FLOWING_WATER;
    @Mutable @Shadow @Final @RestoreFinal public static FlowingFluid WATER;
    @Mutable @Shadow @Final @RestoreFinal public static FlowingFluid FLOWING_LAVA;
    @Mutable @Shadow @Final @RestoreFinal public static FlowingFluid LAVA;

    @Unique
    @ModifyStatic
    private static void clinit() {
        EMPTY = register("empty", new EmptyFluid());
        FLOWING_WATER = register("flowing_water", new WaterFluid.Flowing());
        WATER = register("water", new WaterFluid.Source());
        FLOWING_LAVA = register("flowing_lava", new LavaFluid.Flowing());
        LAVA = register("lava", new LavaFluid.Source());
        for (long it = Registry.FLUID.beginIteration(); Registry.FLUID.hasNextIteration(it); it = Registry.FLUID.nextEntry(it)) {
            Fluid fluid = (Fluid) Registry.FLUID.getIteration(it);
            OList<FluidState> possibleStates = fluid.getStateDefinition().getPossibleStates_();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                Fluid.FLUID_STATE_REGISTRY.add(possibleStates.get(i));
            }
        }
    }

    @Shadow
    private static <T extends Fluid> T register(String string, T fluid) {
        throw new AbstractMethodError();
    }
}
