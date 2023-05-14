package tgw.evolution.world.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.OptionalMutableBlockPos;
import tgw.evolution.util.math.BlockPosUtil;

public final class LevelUtils {

    private LevelUtils() {
    }

    @Nullable
    @CanIgnoreReturnValue
    public static BlockPos findSupportingBlockPos(CollisionGetter level,
                                                  Entity entity,
                                                  double minX,
                                                  double minY,
                                                  double minZ,
                                                  double maxX,
                                                  double maxY,
                                                  double maxZ,
                                                  @Nullable OptionalMutableBlockPos pos) {
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        int z = Integer.MAX_VALUE;
        try (CollisionCalculator calculator = CollisionCalculator.getInstance(level, entity, minX, minY, minZ, maxX, maxY, maxZ, false)) {
            double minDist = Double.MAX_VALUE;
            while (calculator.hasNext()) {
                BlockPos nextPos = calculator.next();
                assert nextPos != null;
                double dist = nextPos.distToCenterSqr(entity.position());
                if (dist < minDist || dist == minDist && (x == Integer.MAX_VALUE || BlockPosUtil.compare(x, y, z, nextPos) < 0)) {
                    x = nextPos.getX();
                    y = nextPos.getY();
                    z = nextPos.getZ();
                    minDist = dist;
                }
            }
        }
        if (x == Integer.MAX_VALUE) {
            if (pos != null) {
                pos.remove();
            }
            return null;
        }
        if (pos != null) {
            pos.set(x, y, z);
            return pos.get();
        }
        return new BlockPos(x, y, z);
    }
}
