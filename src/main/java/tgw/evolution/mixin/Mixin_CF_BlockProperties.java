package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchBlockProperties;
import tgw.evolution.patches.obj.IStateArgumentPredicate;
import tgw.evolution.patches.obj.IStatePredicate;

import java.util.function.Function;
import java.util.function.ToIntFunction;

@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@Mixin(BlockBehaviour.Properties.class)
public abstract class Mixin_CF_BlockProperties implements PatchBlockProperties {

    @Shadow public boolean canOcclude;
    @Shadow public ToIntFunction<BlockState> lightEmission;
    @Shadow public Material material;
    @Shadow public Function<BlockState, MaterialColor> materialColor;
    @Shadow @DeleteField BlockBehaviour.StatePredicate emissiveRendering;
    @Unique IStatePredicate emissiveRendering_;
    @Shadow float friction;
    @Shadow boolean hasCollision;
    @Shadow @DeleteField BlockBehaviour.StatePredicate hasPostProcess;
    @Unique IStatePredicate hasPostProcess_;
    @Shadow @DeleteField BlockBehaviour.StatePredicate isRedstoneConductor;
    @Unique IStatePredicate isRedstoneConductor_;
    @Shadow @DeleteField BlockBehaviour.StatePredicate isSuffocating;
    @Unique IStatePredicate isSuffocating_;
    @Shadow @DeleteField BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn;
    @Unique IStateArgumentPredicate<EntityType<?>> isValidSpawn_;
    @Shadow @DeleteField BlockBehaviour.StatePredicate isViewBlocking;
    @Unique IStatePredicate isViewBlocking_;
    @Shadow float jumpFactor;
    @Shadow SoundType soundType;
    @Shadow float speedFactor;

    @ModifyConstructor
    private Mixin_CF_BlockProperties(Material material, Function<BlockState, MaterialColor> function) {
        this.hasCollision = true;
        this.soundType = SoundType.STONE;
        this.lightEmission = Mixin_CF_BlockProperties::method_26237;
        this.friction = 0.6F;
        this.speedFactor = 1.0F;
        this.jumpFactor = 1.0F;
        this.canOcclude = true;
        this.isValidSpawn_ = Mixin_CF_BlockProperties::_isValidSpawn;
        this.isRedstoneConductor_ = Mixin_CF_BlockProperties::_isRedstoneConductor;
        this.isSuffocating_ = this::_isSuffocating;
        this.isViewBlocking_ = this.isSuffocating_;
        this.hasPostProcess_ = Mixin_CF_BlockProperties::_alwaysFalse;
        this.emissiveRendering_ = Mixin_CF_BlockProperties::_alwaysFalse;
        this.material = material;
        this.materialColor = function;
    }

    @Unique
    private static boolean _alwaysFalse(BlockState state, BlockGetter level, int x, int y, int z) {
        return false;
    }

    @Unique
    private static boolean _isRedstoneConductor(BlockState state, BlockGetter level, int x, int y, int z) {
        return state.getMaterial().isSolidBlocking() && state.isCollisionShapeFullBlock_(level, x, y, z);
    }

    @Unique
    private static boolean _isValidSpawn(BlockState state, BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return state.isFaceSturdy_(level, x, y, z, Direction.UP) && state.getLightEmission() < 14;
    }

    @Shadow
    private static int method_26237(BlockState par1) {
        throw new AbstractMethodError();
    }

    @Unique
    private boolean _isSuffocating(BlockState state, BlockGetter level, int x, int y, int z) {
        return this.material.blocksMotion() && state.isCollisionShapeFullBlock_(level, x, y, z);
    }

    @Overwrite
    public BlockBehaviour.Properties emissiveRendering(BlockBehaviour.StatePredicate statePredicate) {
        Evolution.deprecatedMethod();
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public IStatePredicate emissiveRendering_() {
        return this.emissiveRendering_;
    }

    @Override
    public BlockBehaviour.Properties emissiveRendering_(IStatePredicate predicate) {
        this.emissiveRendering_ = predicate;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Overwrite
    public BlockBehaviour.Properties hasPostProcess(BlockBehaviour.StatePredicate statePredicate) {
        Evolution.deprecatedMethod();
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public BlockBehaviour.Properties hasPostProcess_(IStatePredicate predicate) {
        this.hasPostProcess_ = predicate;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public IStatePredicate hasPostProcess_() {
        return this.hasPostProcess_;
    }

    @Overwrite
    public BlockBehaviour.Properties isRedstoneConductor(BlockBehaviour.StatePredicate statePredicate) {
        Evolution.deprecatedMethod();
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public IStatePredicate isRedstoneConductor_() {
        return this.isRedstoneConductor_;
    }

    @Override
    public BlockBehaviour.Properties isRedstoneConductor_(IStatePredicate predicate) {
        this.isRedstoneConductor_ = predicate;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Overwrite
    public BlockBehaviour.Properties isSuffocating(BlockBehaviour.StatePredicate statePredicate) {
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public BlockBehaviour.Properties isSuffocating_(IStatePredicate predicate) {
        this.isSuffocating_ = predicate;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public IStatePredicate isSuffocating_() {
        return this.isSuffocating_;
    }

    @Overwrite
    public BlockBehaviour.Properties isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> stateArgumentPredicate) {
        Evolution.deprecatedMethod();
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public BlockBehaviour.Properties isValidSpawn_(IStateArgumentPredicate<EntityType<?>> predicate) {
        this.isValidSpawn_ = predicate;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public IStateArgumentPredicate<EntityType<?>> isValidSpawn_() {
        return this.isValidSpawn_;
    }

    @Overwrite
    public BlockBehaviour.Properties isViewBlocking(BlockBehaviour.StatePredicate statePredicate) {
        Evolution.deprecatedMethod();
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public BlockBehaviour.Properties isViewBlocking_(IStatePredicate predicate) {
        this.isViewBlocking_ = predicate;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public IStatePredicate isViewBlocking_() {
        return this.isViewBlocking_;
    }
}
