package tgw.evolution.util.collection.maps.custom;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;

public class ClassInstanceMultiMap<T> extends AbstractCollection<T> {

    private final OList<T> allInstances = new OArrayList<>();
    private final Class<T> baseClass;
    private final R2OMap<Class<T>, OList<T>> byClass = new R2OHashMap<>();

    public ClassInstanceMultiMap(Class<T> baseClass) {
        this.baseClass = baseClass;
        this.byClass.put(baseClass, this.allInstances);
    }

    @Override
    public boolean add(T t) {
        boolean add = false;
        R2OMap<Class<T>, OList<T>> byClass = this.byClass;
        for (long it = byClass.beginIteration(); byClass.hasNextIteration(it); it = byClass.nextEntry(it)) {
            //noinspection DataFlowIssue will not be null
            if (byClass.getIterationKey(it).isInstance(t)) {
                add |= byClass.getIterationValue(it).add(t);
            }
        }
        return add;
    }

    @Override
    public boolean contains(Object object) {
        return this.find((Class<T>) object.getClass()).contains(object);
    }

    public OList<T> find(Class<T> clazz) {
        if (!this.baseClass.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Don't know how to search for " + clazz);
        }
        R2OMap<Class<T>, OList<T>> byClass = this.byClass;
        OList<T> list = byClass.get(clazz);
        if (list == null) {
            list = new OArrayList<>();
            byClass.put(clazz, list);
            OList<T> allInstances = this.allInstances;
            for (int i = 0, len = allInstances.size(); i < len; ++i) {
                T t = allInstances.get(i);
                if (clazz.isInstance(t)) {
                    list.add(t);
                }
            }
        }
        return list.view();
    }

    public @UnmodifiableView OList<T> getAllInstances() {
        return this.allInstances.view();
    }

    @Override
    public Iterator<T> iterator() {
        Evolution.deprecatedMethod();
        return this.allInstances.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances.iterator());
    }

    @Override
    public boolean remove(Object t) {
        boolean removed = false;
        R2OMap<Class<T>, OList<T>> byClass = this.byClass;
        for (long it = byClass.beginIteration(); byClass.hasNextIteration(it); it = byClass.nextEntry(it)) {
            //noinspection DataFlowIssue will not be null
            if (byClass.getIterationKey(it).isInstance(t)) {
                removed |= byClass.getIterationValue(it).remove(t);
            }
        }
        return removed;
    }

    @Override
    public int size() {
        return this.allInstances.size();
    }
}
