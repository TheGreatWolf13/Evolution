package tgw.evolution.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ItemInput.class)
public abstract class ItemInputMixin {

    @Shadow
    @Final
    private static Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG;
    @Shadow
    @Final
    private Item item;
    @Shadow
    @Final
    @Nullable
    private CompoundTag tag;

    /**
     * @author TheGreatWolf
     * @reason Modify the created stack for modular items.
     */
    @Overwrite
    public ItemStack createItemStack(int count, boolean allowOversizedStacks) throws CommandSyntaxException {
        ItemStack stack = this.item.getDefaultInstance();
        stack.setCount(count);
        if (this.tag != null) {
            stack.setTag(this.tag);
        }
        if (allowOversizedStacks && count > stack.getMaxStackSize()) {
            throw ERROR_STACK_TOO_BIG.create(Registry.ITEM.getKey(this.item), stack.getMaxStackSize());
        }
        return stack;
    }
}
