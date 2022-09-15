package tgw.evolution.patches;

public interface IPlayerPatch {

    double getMotionX();

    double getMotionY();

    double getMotionZ();

    boolean isCrawling();

    boolean isMoving();

    void setCrawling(boolean crawling);
}
