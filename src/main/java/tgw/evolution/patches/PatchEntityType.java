package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public interface PatchEntityType {

    static void loadEntitiesRecursive(List<? extends Tag> list, Level level, Consumer<Entity> consumer) {
        for (int i = 0, len = list.size(); i < len; ++i) {
            CompoundTag tag = (CompoundTag) list.get(i);
            //noinspection ObjectAllocationInLoop
            EntityType.loadEntityRecursive(tag, level, e -> {
                consumer.accept(e);
                return e;
            });
        }
    }
}
