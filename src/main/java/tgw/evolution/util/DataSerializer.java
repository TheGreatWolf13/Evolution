package tgw.evolution.util;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.DataSerializerEntry;

public final class DataSerializer {

    public static final IDataSerializer<NonNullList<ItemStack>> ITEM_LIST = new IDataSerializer<NonNullList<ItemStack>>() {

        @Override
        public NonNullList<ItemStack> copy(NonNullList<ItemStack> itemStacks) {
            NonNullList<ItemStack> list = NonNullList.withSize(itemStacks.size(), ItemStack.EMPTY);
            for (int i = 0; i < itemStacks.size(); i++) {
                list.set(i, itemStacks.get(i).copy());
            }
            return list;
        }

        public DataParameter<NonNullList<ItemStack>> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public NonNullList<ItemStack> read(PacketBuffer buf) {
            int length = buf.readInt();
            NonNullList<ItemStack> list = NonNullList.withSize(length, ItemStack.EMPTY);
            for (int i = 0; i < list.size(); i++) {
                list.set(i, buf.readItem());
            }
            return list;
        }

        @Override
        public void write(PacketBuffer packetBuffer, NonNullList<ItemStack> itemStacks) {
            packetBuffer.writeInt(itemStacks.size());

            for (ItemStack itemStack : itemStacks) {
                packetBuffer.writeItem(itemStack);
            }
        }
    };

    private DataSerializer() {
    }

    public static void register(RegistryEvent.Register<DataSerializerEntry> event, ResourceLocation registryName) {
        DataSerializerEntry dataSerializerEntryItemList = new DataSerializerEntry(ITEM_LIST);
        dataSerializerEntryItemList.setRegistryName(registryName);
        event.getRegistry().register(dataSerializerEntryItemList);
    }
}
