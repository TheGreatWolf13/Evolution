package tgw.evolution.patches;

import net.minecraft.world.level.entity.EntityAccess;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OList;

public interface PatchEntitySection<T extends EntityAccess> {

    default @UnmodifiableView OList<T> getEntities_() {
        throw new AbstractMethodError();
    }
}
