package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.Main;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.resources.ModResourcePackUtil;

import java.util.function.BooleanSupplier;

@Mixin(Main.class)
public abstract class MixinMain {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, boolean bl, BooleanSupplier booleanSupplier, WorldGenSettings worldGenSettings) {
        //Do nothing
    }

    @Redirect(method = "method_40372", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/DataPackConfig;" +
                                                                          "DEFAULT:Lnet/minecraft/world/level/DataPackConfig;"))
    private static DataPackConfig replaceDefaultDataPackSettings() {
        return ModResourcePackUtil.createDefaultDataPackSettings();
    }
}
