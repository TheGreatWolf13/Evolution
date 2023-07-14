package tgw.evolution.util.hitbox.hrs;

public interface HR {

    default void rotateXHR(float xRot) {
        throw new AbstractMethodError();
    }

    default void rotateYHR(float yRot) {
        throw new AbstractMethodError();
    }

    default void rotateZHR(float zRot) {
        throw new AbstractMethodError();
    }

    default void scaleHR(float scaleX, float scaleY, float scaleZ) {
        throw new AbstractMethodError();
    }

    default void translateHR(float x, float y, float z) {
        throw new AbstractMethodError();
    }
}
