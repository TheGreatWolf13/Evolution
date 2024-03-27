package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchProperty;

import java.util.Optional;

@Mixin(Property.class)
public abstract class Mixin_CF_Property<T extends Comparable<T>> implements PatchProperty<T> {

    @Mutable @Shadow @Final @RestoreFinal private Class<T> clazz;
    @Mutable @Shadow @Final @RestoreFinal private Codec<T> codec;
    @Mutable @Shadow @Final @RestoreFinal private String name;
    @Mutable @Shadow @Final @RestoreFinal private Codec<Property.Value<T>> valueCodec;

    @ModifyConstructor
    public Mixin_CF_Property(String string, Class<T> class_) {
        this.codec = Codec.STRING.comapFlatMap(this::dumpCodec, this::getName);
        this.valueCodec = this.codec.xmap(this::value, Property.Value::value);
        this.clazz = class_;
        this.name = string;
    }

    @Shadow
    public abstract String getName(T comparable);

    @Shadow
    public abstract Optional<T> getValue(String string);

    @Shadow
    public abstract Property.Value<T> value(T comparable);

    @Unique
    private DataResult<? extends T> dumpCodec(String s) {
        T value = this.getValue_(s);
        if (value == null) {
            return DataResult.error("Unable to read property: " + this + " with value: " + s);
        }
        return DataResult.success(value);
    }
}
