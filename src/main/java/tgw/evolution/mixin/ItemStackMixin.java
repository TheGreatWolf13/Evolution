package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.registries.IRegistryDelegate;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    private CompoundTag capNBT;
    @Shadow
    private int count;
    @Shadow
    @Final
    @Nullable
    private IRegistryDelegate<Item> delegate;
    @Shadow
    private boolean emptyCacheFlag;

    public ItemStackMixin(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    @Override
    public void forceSerializeCaps() {
        this.capNBT = this.serializeCaps();
    }

    @Override
    public @Nullable CompoundTag getCapNBT() {
        return this.capNBT;
    }

    private Item getItemInternal() {
        return this.delegate == null ? Items.AIR : this.delegate.get();
    }

    /**
     * @author TheGreatWolf
     * @reason Use cached state
     */
    @Overwrite
    public boolean isEmpty() {
        return this.emptyCacheFlag;
    }

    private boolean isInternal(Item item) {
        return this.getItemInternal() == item;
    }

    /**
     * @author TheGreatWolf
     * @reason Use cached state
     */
    @Overwrite
    private void updateEmptyCacheFlag() {
        //noinspection ConstantConditions
        if ((Object) this == EMPTY) {
            this.emptyCacheFlag = true;
        }
        else if (!this.isInternal(Items.AIR)) {
            this.emptyCacheFlag = this.count <= 0;
        }
        else {
            this.emptyCacheFlag = true;
        }
    }
}
