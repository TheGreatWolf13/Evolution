package tgw.evolution.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;

public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static void trim(ObjectList list) {
        if (list instanceof ObjectArrayList l) {
            l.trim();
        }
    }

    public static void trim(ReferenceList list) {
        if (list instanceof ReferenceArrayList l) {
            l.trim();
        }
    }

    public static void trim(Int2ObjectMap map) {
        if (map instanceof Int2ObjectOpenHashMap m) {
            m.trim();
        }
    }
}
