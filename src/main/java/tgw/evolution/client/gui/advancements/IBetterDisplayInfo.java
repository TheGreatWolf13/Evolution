package tgw.evolution.client.gui.advancements;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public interface IBetterDisplayInfo {
    /**
     * If the advancement should be able to be dragged in the gui
     *
     * @return a boolean value
     */
    default boolean allowDragging() {
        return false;
    }

    /**
     * If the advancement should use direct lines for its connections
     *
     * @return a boolean value or null to default
     */
    @Nullable
    default Boolean drawDirectLines() {
        return null;
    }

    /**
     * The background color of the icon when an advancement is completed
     *
     * @return an integer color value or -1 to default
     */
    default int getCompletedIconColor() {
        return -1;
    }

    /**
     * The inner color of the connection line when an advancement is completed
     *
     * @return an integer color value or -1 to default
     */
    default int getCompletedLineColor() {
        return -1;
    }

    /**
     * The background color of the title text when an advancement is completed
     *
     * @return an integer color value or -1 to default
     */
    default int getCompletedTitleColor() {
        return -1;
    }

    /**
     * @return The resource location this information is about
     */
    ResourceLocation getId();

    /**
     * The X position of the advancement, in pixels
     *
     * @return an integer position or null to default
     */
    @Nullable
    default Integer getPosX() {
        return null;
    }

    /**
     * The Y position of the advancement, in pixels
     *
     * @return an integer position or null to default
     */
    @Nullable
    default Integer getPosY() {
        return null;
    }

    /**
     * The background color of the icon when an advancement is uncompleted
     *
     * @return an integer color value or -1 to default
     */
    default int getUnCompletedIconColor() {
        return -1;
    }

    /**
     * The inner color of the connection line when an advancement is uncompleted
     *
     * @return an integer color value or -1 to default
     */
    default int getUnCompletedLineColor() {
        return -1;
    }

    /**
     * The background color of the title text when an advancement is uncompleted
     *
     * @return an integer color value or -1 to default
     */
    default int getUnCompletedTitleColor() {
        return -1;
    }

    /**
     * If the advancement should hide its connection lines
     *
     * @return a boolean value or null to default
     */
    @Nullable
    default Boolean hideLines() {
        return null;
    }
}
