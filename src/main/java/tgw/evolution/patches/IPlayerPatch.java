package tgw.evolution.patches;

import net.minecraft.world.entity.player.Player;

public interface IPlayerPatch extends ILivingEntityPatch<Player> {

    double getMotionX();

    double getMotionY();

    double getMotionZ();

    boolean isCrawling();

    boolean isMoving();

    void setCrawling(boolean crawling);
}
