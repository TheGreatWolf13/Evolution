package tgw.evolution.mixin;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.util.physics.EarthHelper;

@Mixin(Mth.class)
public abstract class MixinMth {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static long getSeed(Vec3i v) {
        Evolution.deprecatedMethod();
        return getSeed(v.getX(), v.getY(), v.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static long getSeed(int x, int y, int z) {
        x = EarthHelper.wrapBlockCoordinate(x);
        z = EarthHelper.wrapBlockCoordinate(z);
        long l = x * 3_129_871L ^ z * 116_129_781L ^ y;
        l = l * l * 42_317_861L + l * 11L;
        return l >> 16;
    }
}
