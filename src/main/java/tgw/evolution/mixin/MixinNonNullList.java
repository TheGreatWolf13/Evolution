package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.AbstractList;
import java.util.List;

@Mixin(NonNullList.class)
public abstract class MixinNonNullList<E> extends AbstractList<E> {

    @Shadow @Final private @Nullable E defaultValue;

    @Shadow @Final private List<E> list;

    /**
     * @author TheGreatWolf
     * @reason Replace List
     */
    @Overwrite
    public static <E> NonNullList<E> create() {
        return new NonNullList(new OArrayList(), null);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace List
     */
    @Overwrite
    public static <E> NonNullList<E> createWithCapacity(int i) {
        return new NonNullList(new OArrayList(i), null);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace List
     */
    @SafeVarargs
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public static <E> NonNullList<E> of(E object, E... objects) {
        return new NonNullList(new OArrayList(objects), object);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace List
     */
    @Overwrite
    public static <E> NonNullList<E> withSize(int i, E object) {
        Validate.notNull(object);
        OList list = new OArrayList(i);
        list.addMany(object, i);
        return new NonNullList(list, object);
    }

    /**
     * @author TheGreatWolf
     * @reason Make faster
     */
    @Overwrite
    @Override
    public void clear() {
        if (this.defaultValue == null) {
            super.clear();
        }
        else {
            ((OList<E>) this.list).setMany(this.defaultValue, 0, this.size());
        }
    }

    @Override
    @Shadow
    public abstract int size();
}
