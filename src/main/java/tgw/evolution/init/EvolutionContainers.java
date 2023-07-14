package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import tgw.evolution.Evolution;
import tgw.evolution.inventory.corpse.ContainerCorpse;
import tgw.evolution.inventory.extendedinventory.ContainerInventory;

public final class EvolutionContainers {

    public static final MenuType<ContainerCorpse> CORPSE;
    public static final MenuType<ContainerInventory> EXTENDED_INVENTORY;

    static {
        CORPSE = register("corpse", ContainerCorpse::new);
        EXTENDED_INVENTORY = register("extended_inventory", ContainerInventory::new);
    }

    private EvolutionContainers() {
    }

    public static void register() {
        //Containers are registered via class-loading.
    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> supplier) {
        return Registry.register(Registry.MENU, Evolution.getResource(name), new MenuType<>(supplier));
    }
}
