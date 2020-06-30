package tgw.evolution.capabilities.inventory;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import tgw.evolution.inventory.IExtendedItemHandler;

public class PlayerInventoryCapability {
	
	@CapabilityInject(IExtendedItemHandler.class)
	public static final Capability<IExtendedItemHandler> CAPABILITY_EXTENDED_INVENTORY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IExtendedItemHandler.class, new Capability.IStorage<IExtendedItemHandler>() {

			@Override
			public void readNBT(Capability<IExtendedItemHandler> capability, IExtendedItemHandler instance, Direction facing, INBT nbt) {
			}

			@Override
			public INBT writeNBT(Capability<IExtendedItemHandler> capability, IExtendedItemHandler instance, Direction facing) {
				return null;
			}
			
		}, () -> null);
	}
}
