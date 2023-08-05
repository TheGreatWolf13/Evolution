package tgw.evolution.util.collection.maps;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface O2IMap<K> extends Object2IntMap<K>, ICollectionExtension {

    @CanIgnoreReturnValue
    int addTo(@Nullable K k, int i);

    @Override
    void clear();
}
