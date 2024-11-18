package tgw.evolution.mixin;

import com.google.common.collect.Iterators;
import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchClassInstanceMultiMap;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

import java.util.*;

@Mixin(ClassInstanceMultiMap.class)
public abstract class Mixin_CFM_ClassInstanceMultiMap<T> extends AbstractCollection<T> implements PatchClassInstanceMultiMap<T> {

    @DeleteField @Shadow @Final private List<T> allInstances;
    @Unique private final OList<T> allInstances_;
    @Mutable @Shadow @Final @RestoreFinal private Class<T> baseClass;
    @DeleteField @Shadow @Final private Map<Class<?>, List<T>> byClass;
    @Unique private final R2OMap<Class<T>, OList<T>> byClass_;

    @ModifyConstructor
    public Mixin_CFM_ClassInstanceMultiMap(Class<T> baseClass) {
        this.byClass_ = new R2OHashMap<>();
        this.allInstances_ = new OArrayList<>();
        this.baseClass = baseClass;
        this.byClass_.put(baseClass, this.allInstances_);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean add(T object) {
        boolean add = false;
        R2OMap<Class<T>, OList<T>> byClass = this.byClass_;
        for (long it = byClass.beginIteration(); byClass.hasNextIteration(it); it = byClass.nextEntry(it)) {
            //noinspection DataFlowIssue
            if (byClass.getIterationKey(it).isInstance(object)) {
                add |= byClass.getIterationValue(it).add(object);
            }
        }
        return add;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean contains(Object object) {
        return this.find_((Class<T>) object.getClass()).contains(object);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    public <S> Collection<S> find(Class<S> clazz) {
        throw new AbstractMethodError();
    }

    @Override
    public OList<? extends T> find_(Class<T> clazz) {
        assert this.baseClass.isAssignableFrom(clazz) : "Don't know how to search for " + clazz;
        R2OMap<Class<T>, OList<T>> byClass = this.byClass_;
        OList<T> list = byClass.get(clazz);
        if (list == null) {
            list = new OArrayList<>();
            byClass.put(clazz, list);
            OList<T> allInstances = this.allInstances_;
            for (int i = 0, len = allInstances.size(); i < len; ++i) {
                T t = allInstances.get(i);
                if (clazz.isInstance(t)) {
                    list.add(t);
                }
            }
        }
        return list.view();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public List<T> getAllInstances() {
        return this.allInstances_.view();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Iterator<T> iterator() {
        Evolution.deprecatedMethod();
        return this.allInstances_.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances_.iterator());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean remove(Object object) {
        boolean removed = false;
        R2OMap<Class<T>, OList<T>> byClass = this.byClass_;
        for (long it = byClass.beginIteration(); byClass.hasNextIteration(it); it = byClass.nextEntry(it)) {
            //noinspection DataFlowIssue
            if (byClass.getIterationKey(it).isInstance(object)) {
                removed |= byClass.getIterationValue(it).remove(object);
            }
        }
        return removed;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int size() {
        return this.allInstances_.size();
    }
}
