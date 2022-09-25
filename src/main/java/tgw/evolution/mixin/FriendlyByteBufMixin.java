package tgw.evolution.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {

    @Shadow
    public abstract ByteBuf writeBoolean(boolean pValue);

    @Shadow
    public abstract ByteBuf writeByte(int pValue);

    /**
     * @author TheGreatWolf
     * @reason Fix capability data not synched.
     */
    @Overwrite
    public FriendlyByteBuf writeItemStack(ItemStack stack, boolean limitedTag) {
        if (stack.isEmpty()) {
            this.writeBoolean(false);
        }
        else {
            this.writeBoolean(true);
            Item item = stack.getItem();
            this.writeVarInt(Item.getId(item));
            this.writeByte(stack.getCount());
            CompoundTag compoundtag = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundtag = stack.getShareTag();
            }
            this.writeNbt(compoundtag);
        }
        return (FriendlyByteBuf) (Object) this;
    }

    @Shadow
    public abstract FriendlyByteBuf writeNbt(@Nullable CompoundTag pNbt);

    @Shadow
    public abstract FriendlyByteBuf writeVarInt(int p_130131_);
}
