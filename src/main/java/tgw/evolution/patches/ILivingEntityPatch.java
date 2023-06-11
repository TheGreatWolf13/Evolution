package tgw.evolution.patches;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.EffectHelper;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.HitboxType;

public interface ILivingEntityPatch<T extends LivingEntity> extends IEntityPatch<T> {

    void addAbsorptionSuggestion(float amount);

    boolean canPerformFollowUp(IMelee.IAttackType type);

    default int getAttackNumber() {
        return this.isOnGracePeriod() ? this.getFollowUp() : this.getFollowUp() + 1;
    }

    EffectHelper getEffectHelper();

    int getFollowUp();

    /**
     * @return The progress, from {@code 0.0f} to {@code 1.0f} of the special attack, given interpolation ticks.
     */
    float getSpecialAttackProgress(float partialTicks);

    /**
     * @return The type of the current special attack. {@code null} if none is being performed.
     */
    @Nullable
    IMelee.IAttackType getSpecialAttackType();

    /**
     * The intrinsic entity slowdown distance for fall damage. A higher distance means the energy is distributed throughout a longer distance,
     * decreasing fall damage.
     */
    default double intrinsicSlowdown() {
        return 0;
    }

    /**
     * @return Whether the current special attacks should lock the player camera.
     */
    boolean isCameraLocked();

    /**
     * @return Whether the special attack is in hit ticks (hitbox calculations should be performed and damage should be dealt).
     */
    boolean isInHitTicks();

    default boolean isInSpecialAttackOrGracePeriod() {
        return this.isSpecialAttacking() || this.isOnGracePeriod();
    }

    boolean isLateralMotionLocked();

    boolean isLockedInSpecialAttack();

    boolean isLongitudinalMotionLocked();

    boolean isOnGracePeriod();

    boolean isSpecialAttacking();

    boolean isVerticalMotionLocked();

    void performFollowUp();

    void removeAbsorptionSuggestion(float amount);

    /**
     * @return Whether the special attack should be rendered by clients.
     */
    default boolean shouldRenderSpecialAttack() {
        return this.isSpecialAttacking() || this.isOnGracePeriod() || this.isLockedInSpecialAttack();
    }

    void startSpecialAttack(IMelee.IAttackType type);

    void stopSpecialAttack(IMelee.StopReason reason);

    /**
     * @return The damage that should be applied to the entity.
     */
    float tryHurt(DamageSourceEv source, float amount, float strength, HitboxType hitbox);
}
