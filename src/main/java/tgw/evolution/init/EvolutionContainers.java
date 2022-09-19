package tgw.evolution.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.inventory.corpse.ContainerCorpse;
import tgw.evolution.inventory.extendedinventory.ContainerInventory;

public final class EvolutionContainers {

    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Evolution.MODID);

    public static final RegistryObject<MenuType<ContainerCorpse>> CORPSE;
    public static final RegistryObject<MenuType<ContainerInventory>> EXTENDED_INVENTORY;

    static {
        CORPSE = CONTAINERS.register("corpse", () -> IForgeMenuType.create((id, inv, data) -> {
            int entityId = data.readInt();
            EntityPlayerCorpse corpse = (EntityPlayerCorpse) inv.player.level.getEntity(entityId);
            return new ContainerCorpse(id, corpse, inv);
        }));
        EXTENDED_INVENTORY = CONTAINERS.register("extended_inventory",
                                                 () -> IForgeMenuType.create((id, inv, data) -> new ContainerInventory(id, inv)));
    }

    private EvolutionContainers() {
    }

    public static void register() {
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
