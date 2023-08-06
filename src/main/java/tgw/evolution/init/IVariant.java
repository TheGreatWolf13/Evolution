package tgw.evolution.init;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OList;

import java.util.Map;

public interface IVariant<V extends IVariant<V>> {

    @Contract(pure = true, value = "null -> fail")
    default void checkNull(@Nullable Object o) {
        if (o == null) {
            throw new IllegalStateException("This variant (" + this + ") does not have a registry type for this registry!");
        }
    }

    default <T> T get(Map<V, T> registry) {
        T object = registry.get(this);
        this.checkNull(object);
        return object;
    }

    @UnmodifiableView OList<Map<V, ? extends Block>> getBlocks();

    String getName();

    void registerBlocks(Map<V, ? extends Block> blocks);
}
