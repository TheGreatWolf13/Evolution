package tgw.evolution.patches;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public interface PatchAbstractWidget {

    default void childRequestedUpdate() {
        throw new AbstractMethodError();
    }

    default void focusOnParent() {
        throw new AbstractMethodError();
    }

    default @Nullable AbstractWidget getParent() {
        throw new AbstractMethodError();
    }

    default @Nullable Screen getScreen() {
        throw new AbstractMethodError();
    }

    default void setHeight(int height) {
        throw new AbstractMethodError();
    }

    default void setParent(@Nullable AbstractWidget parent) {
        throw new AbstractMethodError();
    }

    default void setScreen(@Nullable Screen screen) {
        throw new AbstractMethodError();
    }

    default void setX(int x) {
        throw new AbstractMethodError();
    }

    default void setY(int y) {
        throw new AbstractMethodError();
    }
}
