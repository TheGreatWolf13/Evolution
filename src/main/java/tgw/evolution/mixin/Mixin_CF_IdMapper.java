package tgw.evolution.mixin;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.IdMap;
import net.minecraft.core.IdMapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2IHashMap;
import tgw.evolution.util.collection.maps.R2IMap;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Mixin(IdMapper.class)
public abstract class Mixin_CF_IdMapper<T> implements IdMap<T> {

    @Shadow @Final @DeleteField private List<T> idToT;
    @Unique private final OList<T> idToT_;
    @Shadow private int nextId;
    @Shadow @Final @DeleteField private Object2IntMap<T> tToId;
    @Unique private final R2IMap<T> tToId_;

    @ModifyConstructor
    public Mixin_CF_IdMapper() {
        this(512);
    }

    @ModifyConstructor
    public Mixin_CF_IdMapper(int i) {
        this.idToT_ = new OArrayList<>(i);
        this.tToId_ = new R2IHashMap<>(i);
        this.tToId_.defaultReturnValue(-1);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void addMapping(T object, int i) {
        this.tToId_.put(object, i);
        while (this.idToT_.size() <= i) {
            this.idToT_.add(null);
        }
        this.idToT_.set(i, object);
        if (this.nextId <= i) {
            this.nextId = i + 1;
        }
    }

    @Override
    public long beginIteration() {
        OList<T> idToT = this.idToT_;
        int size = this.tToId_.size();
        for (int i = 0, len = idToT.size(); i < len; ++i) {
            if (idToT.get(i) != null) {
                return (long) i << 32 | size;
            }
        }
        return 0;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public final @Nullable T byId(int i) {
        return i >= 0 && i < this.idToT_.size() ? this.idToT_.get(i) : null;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int getId(T object) {
        return this.tToId_.getInt(object);
    }

    @Override
    public T getIteration(long it) {
        return this.idToT_.get((int) (it >> 32));
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it > 0;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Iterator<T> iterator() {
        Evolution.deprecatedMethod();
        return Iterators.filter(this.idToT_.iterator(), Objects::nonNull);
    }

    @Override
    public long nextEntry(long it) {
        int size = (int) it;
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32) + 1;
        OList<T> idToT = this.idToT_;
        for (int i = pos, len = idToT.size(); i < len; ++i) {
            if (idToT.get(i) != null) {
                return (long) i << 32 | size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int size() {
        return this.tToId_.size();
    }
}
