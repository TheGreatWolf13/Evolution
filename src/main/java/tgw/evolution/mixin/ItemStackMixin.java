package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IItemStackPatch;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin extends CapabilityProvider<ItemStack> implements IItemStackPatch {

    @Shadow
    @Final
    public static ItemStack EMPTY;
    @Shadow
    private CompoundTag capNBT;
    @Shadow
    private int count;
    @Shadow
    @Final
    private IRegistryDelegate<Item> delegate;
    @Shadow
    private boolean emptyCacheFlag;

    public ItemStackMixin(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    @Override
    public CompoundTag getCapNBT() {
        return this.capNBT;
    }

    private Item getItemInternal() {
        return this.delegate == null ? Items.AIR : this.delegate.get();
    }

    /**
     * @author MGSchultz
     * <p>
     * Use cached state
     */
    @Overwrite
    public boolean isEmpty() {
        return this.emptyCacheFlag;
    }

    private boolean isInternal(Item item) {
        return this.getItemInternal() == item;
    }

    /**
     * @author MGSchultz
     * <p>
     * Use cached state
     */
    @Overwrite
    private void updateEmptyCacheFlag() {
        if ((Object) this == EMPTY) {
            this.emptyCacheFlag = true;
        }
        else if (this.getItemInternal() != null && !this.isInternal(Items.AIR)) {
            this.emptyCacheFlag = this.count <= 0;
        }
        else {
            this.emptyCacheFlag = true;
        }
    }
}
