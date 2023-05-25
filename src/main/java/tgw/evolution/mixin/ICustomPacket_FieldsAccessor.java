package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraftforge.network.ICustomPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.lang.reflect.Field;
import java.util.Optional;

@Mixin(ICustomPacket.Fields.class)
public interface ICustomPacket_FieldsAccessor {

    @Accessor(remap = false)
    static Reference2ReferenceArrayMap<Class<?>, ICustomPacket.Fields> getLookup() {
        throw new AbstractMethodError();
    }

    @Accessor(remap = false)
    Optional<Field> getChannel();

    @Accessor(remap = false)
    Optional<Field> getData();

    @Accessor(remap = false)
    Optional<Field> getIndex();
}
