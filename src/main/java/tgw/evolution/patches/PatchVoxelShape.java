package tgw.evolution.patches;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OList;

public interface PatchVoxelShape {

    default @UnmodifiableView OList<AABB> cachedBoxes() {
        throw new AbstractMethodError();
    }

    default @Nullable BlockHitResult clip_(Vec3 start, Vec3 end, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
