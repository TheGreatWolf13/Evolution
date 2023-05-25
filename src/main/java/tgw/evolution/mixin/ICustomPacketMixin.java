package tgw.evolution.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.Field;
import java.util.Optional;

@Mixin(ICustomPacket.class)
public interface ICustomPacketMixin {

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default int getIndex() {
        Optional<Field> field = ((ICustomPacket_FieldsAccessor) (Object) ICustomPacket_FieldsAccessor.getLookup().get(this.getClass())).getIndex();
        if (field.isPresent()) {
            return UnsafeHacks.getIntField(field.get(), this);
        }
        return Integer.MIN_VALUE;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default @Nullable FriendlyByteBuf getInternalData() {
        Optional<Field> data = ((ICustomPacket_FieldsAccessor) (Object) ICustomPacket_FieldsAccessor.getLookup().get(this.getClass())).getData();
        if (data.isPresent()) {
            return UnsafeHacks.getField(data.get(), this);
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default ResourceLocation getName() {
        Optional<Field> channel = ((ICustomPacket_FieldsAccessor) (Object) ICustomPacket_FieldsAccessor.getLookup()
                                                                                                       .get(this.getClass())).getChannel();
        if (channel.isPresent()) {
            return UnsafeHacks.getField(channel.get(), this);
        }
        return LoginWrapperAccessor.getWrapper();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default void setData(FriendlyByteBuf buffer) {
        Optional<Field> data = ((ICustomPacket_FieldsAccessor) (Object) ICustomPacket_FieldsAccessor.getLookup().get(this.getClass())).getData();
        if (data.isPresent()) {
            UnsafeHacks.setField(data.get(), this, buffer);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default void setIndex(int index) {
        Optional<Field> field = ((ICustomPacket_FieldsAccessor) (Object) ICustomPacket_FieldsAccessor.getLookup().get(this.getClass())).getIndex();
        if (field.isPresent()) {
            UnsafeHacks.setIntField(field.get(), this, index);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default void setName(ResourceLocation channelName) {
        Optional<Field> channel = ((ICustomPacket_FieldsAccessor) (Object) ICustomPacket_FieldsAccessor.getLookup()
                                                                                                       .get(this.getClass())).getChannel();
        if (channel.isPresent()) {
            UnsafeHacks.setField(channel.get(), this, channelName);
        }
    }
}
