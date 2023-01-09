package tgw.evolution.patches.obj;

import com.mojang.datafixers.DataFixerUpper;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DummyDataFixerUpper extends DataFixerUpper {

    public DummyDataFixerUpper() {
        super(Int2ObjectSortedMaps.emptyMap(), List.of(), IntSortedSets.EMPTY_SET);
    }

    @Override
    public @Nullable Schema getSchema(int key) {
        return null;
    }
}
