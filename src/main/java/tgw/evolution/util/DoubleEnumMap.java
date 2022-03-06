package tgw.evolution.util;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class DoubleEnumMap<K1 extends Enum<K1>, K2 extends Enum<K2>, V> {

    private final Class<K2> clazz2;
    private final Map<K1, Map<K2, V>> data;

    public DoubleEnumMap(Class<K1> clazz1, Class<K2> clazz2) {
        this.data = new EnumMap<>(clazz1);
        this.clazz2 = clazz2;
    }

    @Nullable
    public V get(K1 k1, K2 k2) {
        Map<K2, V> data2 = this.data.get(k1);
        if (data2 == null) {
            return null;
        }
        return data2.get(k2);
    }

    @Nullable
    public V put(K1 k1, K2 k2, V v) {
        Map<K2, V> data2 = this.data.get(k1);
        V prev = null;
        if (data2 == null) {
            data2 = new EnumMap<>(this.clazz2);
            this.data.put(k1, data2);
        }
        else {
            prev = data2.get(k2);
        }
        data2.put(k2, v);
        return prev;
    }
}
