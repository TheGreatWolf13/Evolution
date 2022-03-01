package tgw.evolution.capabilities.thirst;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

public interface IThirst extends INBTSerializable<CompoundTag> {

    /**
     * @param amount The amount of exhaustion to be added to the player hydration.
     */
    void addHydrationExhaustion(float amount);

    /**
     * @param exhaustion The amount of exhaustion to be added to the player thirst.
     */
    void addThirstExhaustion(float exhaustion);

    /**
     * Decreases the hydration level by 1 point.
     */
    void decreaseHydrationLevel();

    /**
     * Decreases the thirst level by 1 point.
     */
    void decreaseThirstLevel();

    /**
     * @return The amount of hydration to be consumed. When it reaches 1.0f, 1 point of hydration will be consumed.
     */
    float getHydrationExhaustion();

    /**
     * @return Hydration acts like food saturation. However, it's a bad effect.
     * Your body can only process so much water at a time (1L/h). When you go over that limit,
     * injesting more water can start to harm you through Water Intoxication.
     * Having more than 1L or 1000 points of Hydration up to 1999 points will grant you the
     * Water Intoxication status effect. Having more than 2000 points will grant you Water Intoxication II,
     * being capable of killing you.
     * Accepted values between 0 and 3000.
     */
    int getHydrationLevel();

    /**
     * @return The amount of thirst to be consumed. When it reaches 1.0f, 1 point of thirst will be consumed.
     */
    float getThirstExhaustion();

    /**
     * @return The thirst level of the player, in mL. The full bar has a capacity for 2.5L or 2500 points.
     * Each drop of water has a capacity of 250 points. Accepted values between 0 and 2500.
     */
    int getThirstLevel();

    /**
     * @param amount Increases the hydration level by the desired amount.
     */
    void increaseHydrationLevel(int amount);

    /**
     * @param amount Increases the thirst level by the desired amount.
     */
    void increaseThirstLevel(int amount);

    /**
     * @param amount Sets the current hydration exhaustion.
     */
    void setHydrationExhaustion(float amount);

    /**
     * @param hydration The desired level of hydration to be set, from 0 to 3000.
     */
    void setHydrationLevel(int hydration);

    /**
     * @param exhaustion Sets the current thirst exhaustion.
     */
    void setThirstExhaustion(float exhaustion);

    /**
     * @param thirstLevel The desired level of thirst to be set, from 0 to 2500.
     */
    void setThirstLevel(int thirstLevel);

    /**
     * Called every tick to tick the player's ThirstStats
     *
     * @param player The Player being ticked
     */
    void tick(ServerPlayer player);
}
