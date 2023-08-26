package tgw.evolution.client.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

import java.util.function.Function;

public final class TooltipManager {

    private static final R2OMap<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> FACTORIES = new R2OHashMap<>();

    private TooltipManager() {
    }

    public static @Nullable ClientTooltipComponent getClientTooltipComponent(TooltipComponent component) {
        Function<TooltipComponent, ClientTooltipComponent> factory = FACTORIES.get(component.getClass());
        return factory == null ? null : factory.apply(component);
    }

    public static <T extends TooltipComponent> void registerTooltipFactory(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory) {
        FACTORIES.put(clazz, (Function<TooltipComponent, ClientTooltipComponent>) factory);
    }
}
