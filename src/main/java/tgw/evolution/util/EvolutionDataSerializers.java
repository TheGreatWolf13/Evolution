package tgw.evolution.util;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.ItemStack;

public final class EvolutionDataSerializers {

    public static final EntityDataSerializer<NonNullList<ItemStack>> ITEM_LIST = new EntityDataSerializer<>() {

        @Override
        public NonNullList<ItemStack> copy(NonNullList<ItemStack> itemStacks) {
            NonNullList<ItemStack> list = NonNullList.withSize(itemStacks.size(), ItemStack.EMPTY);
            for (int i = 0; i < itemStacks.size(); i++) {
                list.set(i, itemStacks.get(i).copy());
            }
            return list;
        }

        @Override
        public NonNullList<ItemStack> read(FriendlyByteBuf buffer) {
            int length = buffer.readInt();
            NonNullList<ItemStack> list = NonNullList.withSize(length, ItemStack.EMPTY);
            for (int i = 0; i < list.size(); i++) {
                list.set(i, buffer.readItem());
            }
            return list;
        }

        @Override
        public void write(FriendlyByteBuf buffer, NonNullList<ItemStack> stacks) {
            buffer.writeInt(stacks.size());
            for (ItemStack stack : stacks) {
                buffer.writeItem(stack);
            }
        }
    };

    private EvolutionDataSerializers() {
    }

    public static void register() {
        EntityDataSerializers.registerSerializer(ITEM_LIST);
    }
}
