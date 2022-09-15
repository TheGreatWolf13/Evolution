package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.EffectHelper;
import tgw.evolution.items.IMelee;

public interface ILivingEntityPatch {

    void addAbsorptionSuggestion(float amount);

    EffectHelper getEffectHelper();

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
     * @return Whether the current special attacks should lock the player camera.
     */
    boolean isCameraLocked();

    /**
     * @return Whether the special attack is in hit ticks (hitbox calculations should be performed and damage should be dealt).
     */
    boolean isInHitTicks();

    /**
     * @return Whether the special attack is in progress, is canceled, or on cooldown.
     */
    boolean isInSpecialAttack();

    /**
     * @return Whether the current special attack should lock the player movement (but not the player's inertia, conservation
     * of momentum is important).
     */
    boolean isMotionLocked();

    /**
     * @return Whether the special attack is in progress, has NOT been canceled and is NOT on cooldown.
     */
    boolean isSpecialAttacking();

    void removeAbsorptionSuggestion(float amount);

    /**
     * @return Whether the special attack should be rendered by clients.
     */
    boolean shouldRenderSpecialAttack();

    void startSpecialAttack(IMelee.IAttackType type);

    void stopSpecialAttack(IMelee.StopReason reason);
}
