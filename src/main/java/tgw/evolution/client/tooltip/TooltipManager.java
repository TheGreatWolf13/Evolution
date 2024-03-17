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

    public static void registerTooltipFactories() {
        registerTooltipFactory(TooltipCold.class, EvolutionTooltipRenderer.COLD::setTooltip);
        registerTooltipFactory(TooltipCooldown.class, EvolutionTooltipRenderer.COOLDOWN::setTooltip);
        registerTooltipFactory(TooltipDamage.class, TooltipDamage::setup);
        registerTooltipFactory(TooltipDmgMultiplier.class, TooltipDmgMultiplier::setup);
        registerTooltipFactory(TooltipDrink.class, EvolutionTooltipRenderer.DRINK::setTooltip);
        registerTooltipFactory(TooltipDurability.class, TooltipDurability::setup);
        registerTooltipFactory(TooltipFollowUp.class, EvolutionTooltipRenderer.FOLLOW_UP::setTooltip);
        registerTooltipFactory(TooltipFood.class, EvolutionTooltipRenderer.FOOD::setTooltip);
        registerTooltipFactory(TooltipHeat.class, EvolutionTooltipRenderer.HEAT::setTooltip);
        registerTooltipFactory(TooltipInfo.class, EvolutionTooltipRenderer.INFO::setTooltip);
        registerTooltipFactory(TooltipMass.class, TooltipMass::setup);
        registerTooltipFactory(TooltipMining.class, EvolutionTooltipRenderer.MINING::setTooltip);
        registerTooltipFactory(TooltipPrecision.class, EvolutionTooltipRenderer.PRECISION::setTooltip);
        registerTooltipFactory(TooltipStructuralIntegrity.class, EvolutionTooltipRenderer.STRUCTURAL_INTEGRITY::setTooltip);
        registerTooltipFactory(TooltipStructureType.class, EvolutionTooltipRenderer.STRUCTURAL_TYPE::setTooltip);
        registerTooltipFactory(TooltipThrowSpeed.class, EvolutionTooltipRenderer.THROW_SPEED::setTooltip);
    }

    public static <T extends TooltipComponent> void registerTooltipFactory(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory) {
        FACTORIES.put(clazz, (Function<TooltipComponent, ClientTooltipComponent>) factory);
    }
}
