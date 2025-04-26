package tgw.evolution.mixin;

import net.minecraft.CrashReportCategory;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.math.StrUtil;

import java.util.Set;

@Mixin(WorldData.class)
public interface MixinWorldData {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    default void fillCrashReportCategory(CrashReportCategory category) {
        category.setDetail("Known server brands", () -> StrUtil.join(", ", (OSet<String>) this.getKnownServerBrands()));
        category.setDetail("Level was modded", () -> Boolean.toString(this.wasModded()));
        category.setDetail("Level storage version", () -> {
            int version = this.getVersion();
            return String.format("0x%05X - %s", version, this.getStorageVersionName(version));
        });
    }

    @Shadow
    Set<String> getKnownServerBrands();

    @Shadow
    String getStorageVersionName(int i);

    @Shadow
    int getVersion();

    @Shadow
    boolean wasModded();
}
