//package tgw.evolution.blocks.tileentities;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.inventory.container.Container;
//import net.minecraft.inventory.container.Slot;
//import net.minecraft.item.ItemStack;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.IWorldPosCallable;
//import net.minecraft.util.IntReferenceHolder;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import net.minecraftforge.energy.CapabilityEnergy;
//import net.minecraftforge.energy.IEnergyStorage;
//import net.minecraftforge.items.CapabilityItemHandler;
//import net.minecraftforge.items.IItemHandler;
//import net.minecraftforge.items.SlotItemHandler;
//import net.minecraftforge.items.wrapper.InvWrapper;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.init.EvolutionContainers;
//import tgw.evolution.init.EvolutionItems;
//
//public class ContainerTest extends Container {
//
//    private TileEntity tileEntity;
//    private PlayerEntity playerEntity;
//    private IItemHandler playerInventory;
//
//    public ContainerTest(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
//        super(EvolutionContainers.CONTAINER_TEST, windowId);
//        this.tileEntity = world.getTileEntity(pos);
//        this.playerEntity = player;
//        this.playerInventory = new InvWrapper(playerInventory);
//
//        this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
//            addSlot(new SlotItemHandler(h, 0, 64, 24));
//        });
//        layoutPlayerInventorySlots(10, 70);
//        
//        trackInt(new IntReferenceHolder() {
//			
//			@Override
//			public void set(int value) {
//				ContainerTest.this.tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> ((CustomEnergyStorage)h).setEnergy(value));
//			}
//			
//			@Override
//			public int get() {
//				return getEnergy();
//			}
//		});
//    }
//
//    public int getEnergy() {
//    	return this.tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
//    }
//    
//    @Override
//    public boolean canInteractWith(PlayerEntity playerIn) {
//        return isWithinUsableDistance(IWorldPosCallable.of(this.tileEntity.getWorld(), this.tileEntity.getPos()), this.playerEntity, EvolutionBlocks.b_placeholder_block);
//    }
//
//    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
//        for (int i = 0 ; i < amount ; i++) {
//            addSlot(new SlotItemHandler(handler, index, x, y));
//            x += dx;
//            index++;
//        }
//        return index;
//    }
//
//    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
//        for (int j = 0 ; j < verAmount ; j++) {
//            index = addSlotRange(handler, index, x, y, horAmount, dx);
//            y += dy;
//        }
//        return index;
//    }
//
//    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
//    	addSlotBox(this.playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
//        topRow += 58;
//        addSlotRange(this.playerInventory, 0, leftCol, topRow, 9, 18);
//    }
//    
//    @Override
//    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
//    	ItemStack itemStack = ItemStack.EMPTY;
//    	Slot slot = this.inventorySlots.get(index);
//    	if (slot != null && slot.getHasStack()) {
//			ItemStack stack = slot.getStack();
//			itemStack = stack.copy();
//			if (index == 0) {
//				if (!this.mergeItemStack(stack, 1, 37, true)) {
//					return ItemStack.EMPTY;
//				}
//				slot.onSlotChange(stack, itemStack);
//			}
//			else {
//				if (stack.getItem() == EvolutionItems.stick) {
//					if (!this.mergeItemStack(stack, 0, 1, false)) {
//						return ItemStack.EMPTY;
//					}
//				}
//				else if (index < 28) {
//					if (!this.mergeItemStack(stack, 28, 37, false)) {
//						return ItemStack.EMPTY;
//					}
//				}
//				else if (index < 37 && !this.mergeItemStack(stack, 1, 28, false)) {
//					return ItemStack.EMPTY;
//				}
//			}
//			if (stack.isEmpty()) {
//				slot.putStack(ItemStack.EMPTY);
//			}
//			else {
//				slot.onSlotChanged();
//			}
//			if (stack.getCount() == itemStack.getCount()) {
//				return ItemStack.EMPTY;
//			}
//			slot.onTake(playerIn, stack);
//		}
//    	return itemStack;
//    }
//}