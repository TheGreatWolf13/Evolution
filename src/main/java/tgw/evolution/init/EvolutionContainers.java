package tgw.evolution.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.inventory.corpse.ContainerCorpse;
import tgw.evolution.inventory.extendedinventory.ContainerPlayerInventory;

public final class EvolutionContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Evolution.MODID);

    public static final RegistryObject<ContainerType<ContainerCorpse>> CORPSE;
    public static final RegistryObject<ContainerType<ContainerPlayerInventory>> EXTENDED_INVENTORY;

    static {
        CORPSE = CONTAINERS.register("corpse", () -> IForgeContainerType.create((id, inv, data) -> {
            int entityId = data.readInt();
            EntityPlayerCorpse corpse = (EntityPlayerCorpse) inv.player.level.getEntity(entityId);
            return new ContainerCorpse(id, corpse, inv);
        }));
        EXTENDED_INVENTORY = CONTAINERS.register("extended_inventory",
                                                 () -> IForgeContainerType.create((id, inv, data) -> new ContainerPlayerInventory(id, inv)));
    }

    private EvolutionContainers() {
    }

    public static void register() {
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
