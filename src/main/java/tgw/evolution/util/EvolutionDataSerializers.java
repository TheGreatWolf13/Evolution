package tgw.evolution.util;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.DataSerializerEntry;

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
        public void write(FriendlyByteBuf buffer, NonNullList<ItemStack> itemStacks) {
            buffer.writeInt(itemStacks.size());
            for (ItemStack itemStack : itemStacks) {
                buffer.writeItem(itemStack);
            }
        }
    };

    private EvolutionDataSerializers() {
    }

    public static void register(RegistryEvent.Register<DataSerializerEntry> event, ResourceLocation registryName) {
        DataSerializerEntry dataSerializerEntryItemList = new DataSerializerEntry(ITEM_LIST);
        dataSerializerEntryItemList.setRegistryName(registryName);
        event.getRegistry().register(dataSerializerEntryItemList);
    }
}
