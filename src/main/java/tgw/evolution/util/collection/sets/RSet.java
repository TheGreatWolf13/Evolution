package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface RSet<K> extends ReferenceSet<K>, ICollectionExtension {

    @Nullable K fastEntries();
}
