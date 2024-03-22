package tgw.evolution.patches;

import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.SI;

public interface PatchEntity {

    default double getAcceleration() {
        return 0;
    }

    default double getBaseAttackDamage() {
        return 1;
    }

    default double getBaseHealth() {
        return 25;
    }

    /**
     * @return The entity mass in kg.
     */
    default double getBaseMass() {
        return 1;
    }

    default double getBaseWalkForce() {
        return 1 * SI.NEWTON;
    }

    float getFrictionModifier();

    default long getFrictionPos() {
        return 0L;
    }

    default @Nullable HitboxEntity<? extends Entity> getHitboxes() {
        return null;
    }

    default @Nullable Pose getLastPose() {
        return null;
    }

    /**
     * @return Controls the deceleration because of the motion of your legs.
     */
    default double getLegSlowdown() {
        return 0;
    }

    /**
     * Colored light in the following format: <br>
     * <br>
     * Bit 0 ~ 3: Red light range; <br>
     * Bit 4: Red light strength; <br>
     * Bit 5 ~ 8: Green light range; <br>
     * Bit 9: Green light strength; <br>
     * Bit 10 ~ 13: Blue light range; <br>
     * Bit 14: Blue light strength; <br>
     * <br>
     * Each individual component intensity must be in the range 0 ~ 15. <br>
     * Each individual component value must be in the range 0 ~ 1. <br>
     */
    default short getLightEmission() {
        return 0;
    }

    default long getLightEmissionPos() {
        return 0L;
    }

    default double getLungCapacity() {
        return 0;
    }

    default int getNoJumpDelay() {
        return 0;
    }

    default long getSteppingPos() {
        return 0;
    }

    default double getVolume() {
        return 0;
    }

    default double getVolumeCorrectionFactor() {
        return 0;
    }

    default boolean hasAnyFluidInEye() {
        return false;
    }

    default boolean hasCollidedOnXAxis() {
        return false;
    }

    default boolean hasCollidedOnZAxis() {
        return false;
    }

    default InteractionResult interactAt_(Player player, double hitX, double hitY, double hitZ, InteractionHand hand) {
        throw new AbstractMethodError();
    }

    default boolean isAddedToWorld() {
        //Is implemented on MixinEntity
        throw new AbstractMethodError();
    }

    default boolean isColliding_(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default boolean isFullySubmerged(TagKey<Fluid> fluid) {
        return false;
    }

    default boolean isInAnyFluid() {
        return false;
    }

    default void onAddedToWorld() {
        //Is implemented on MixinEntity
        throw new AbstractMethodError();
    }

    default void onRemovedFromWorld() {
        //Is implemented on MixinEntity
        throw new AbstractMethodError();
    }

    default void playStepSound(int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default void setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        throw new AbstractMethodError();
    }
}
