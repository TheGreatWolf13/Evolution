package tgw.evolution.patches;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import tgw.evolution.patches.obj.IStateArgumentPredicate;
import tgw.evolution.patches.obj.IStatePredicate;

public interface PatchBlockProperties {

    default IStatePredicate emissiveRendering_() {
        throw new AbstractMethodError();
    }

    default BlockBehaviour.Properties emissiveRendering_(IStatePredicate predicate) {
        throw new AbstractMethodError();
    }

    default IStatePredicate hasPostProcess_() {
        throw new AbstractMethodError();
    }

    default BlockBehaviour.Properties hasPostProcess_(IStatePredicate predicate) {
        throw new AbstractMethodError();
    }

    default BlockBehaviour.Properties isRedstoneConductor_(IStatePredicate predicate) {
        throw new AbstractMethodError();
    }

    default IStatePredicate isRedstoneConductor_() {
        throw new AbstractMethodError();
    }

    default BlockBehaviour.Properties isSuffocating_(IStatePredicate predicate) {
        throw new AbstractMethodError();
    }

    default IStatePredicate isSuffocating_() {
        throw new AbstractMethodError();
    }

    default BlockBehaviour.Properties isValidSpawn_(IStateArgumentPredicate<EntityType<?>> predicate) {
        throw new AbstractMethodError();
    }

    default IStateArgumentPredicate<EntityType<?>> isValidSpawn_() {
        throw new AbstractMethodError();
    }

    default BlockBehaviour.Properties isViewBlocking_(IStatePredicate predicate) {
        throw new AbstractMethodError();
    }

    default IStatePredicate isViewBlocking_() {
        throw new AbstractMethodError();
    }
}
