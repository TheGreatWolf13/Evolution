package tgw.evolution.patches;

public interface PatchBlockStateCache {

    default boolean isCollisionShapeFullBlock() {
        throw new AbstractMethodError();
    }

    default boolean largeCollisionShape() {
        throw new AbstractMethodError();
    }

    default boolean propagatesSkylightDown() {
        throw new AbstractMethodError();
    }

    default boolean solidRender() {
        throw new AbstractMethodError();
    }
}
