package tgw.evolution.patches;

import tgw.evolution.entities.EffectHelper;
import tgw.evolution.items.ISpecialAttack;

import javax.annotation.Nullable;

public interface ILivingEntityPatch {

    void addAbsorptionSuggestion(float amount);

    EffectHelper getEffectHelper();

    /**
     * @return The progress, from {@code 0.0f} to {@code 1.0f} of the mainhand special attack, given interpolation ticks.
     */
    float getMainhandSpecialAttackProgress(float partialTicks);

    /**
     * @return The type of the current mainhand special attack. {@code null} if none is being performed.
     */
    @Nullable
    ISpecialAttack.IAttackType getMainhandSpecialAttackType();

    /**
     * @return The progress, from {@code 0.0f} to {@code 1.0f} of the offhand special attack, given interpolation ticks.
     */
    float getOffhandSpecialAttackProgress(float partialTicks);

    /**
     * @return The type of the current offhand special attack. {@code null} if none is being performed.
     */
    @Nullable
    ISpecialAttack.IAttackType getOffhandSpecialAttackType();

    /**
     * @return Whether the current mainhand or offhand special attacks should lock the player camera.
     */
    boolean isCameraLocked();

    /**
     * @return Whether the mainhand special attack is in hit ticks (hitbox calculations should be performed and damage should be dealt).
     */
    boolean isMainhandInHitTicks();

    /**
     * @return Whether the mainhand special attack is in progress, is canceled, or on cooldown.
     */
    boolean isMainhandInSpecialAttack();

    /**
     * @return Whether the mainhand special attack is in progress, has NOT been canceled and is NOT on cooldown.
     */
    boolean isMainhandSpecialAttacking();

    /**
     * @return Whether the offhand special attack is in hit ticks (hitbox calculations should be performed and damage should be dealt).
     */
    boolean isOffhandInHitTicks();

    /**
     * @return Whether the offhand special attack is in progress, has been canceled, or is on cooldown.
     */
    boolean isOffhandInSpecialAttack();

    /**
     * @return Whether the offhand special attack is in progress, has NOT been canceled and is NOT on cooldown.
     */
    boolean isOffhandSpecialAttacking();

    void removeAbsorptionSuggestion(float amount);

    /**
     * @return Whether the mainhand special attack should be rendered by clients.
     */
    boolean renderMainhandSpecialAttack();

    /**
     * @return Whether the offhand special attack should be rendered by clients.
     */
    boolean renderOffhandSpecialAttack();

    void startMainhandSpecialAttack(ISpecialAttack.IAttackType type);

    void startOffhandSpecialAttack(ISpecialAttack.IAttackType type);

    void stopMainhandSpecialAttack(ISpecialAttack.StopReason reason);

    void stopOffhandSpecialAttack(ISpecialAttack.StopReason reason);
}
