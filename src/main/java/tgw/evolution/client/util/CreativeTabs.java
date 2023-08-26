package tgw.evolution.client.util;

import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Contract;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public final class CreativeTabs {

    private static final OList<CreativeModeTab> TABS = new OArrayList<>();

    private CreativeTabs() {
    }

    public static void add(CreativeModeTab tab) {
        TABS.add(tab);
    }

    @Contract(pure = true)
    public static CreativeModeTab get(int id) {
        return TABS.get(id);
    }

    @Contract(pure = true)
    public static int idForTab() {
        return TABS.size();
    }

    @Contract(pure = true)
    public static int size() {
        return TABS.size();
    }

    public static OList<CreativeModeTab> tabs() {
        return TABS.view();
    }
}
