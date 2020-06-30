package tgw.evolution.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.inventory.ContainerPlayerInventory;

public class EvolutionContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Evolution.MODID);

    public static final RegistryObject<ContainerType<ContainerPlayerInventory>> EXTENDED_INVENTORY = CONTAINERS.register("extended_inventory", () -> IForgeContainerType.create((id, inv, data) -> new ContainerPlayerInventory(id, inv)));

    public static void register() {
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
