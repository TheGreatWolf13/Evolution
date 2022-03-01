package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.LazyDataFixerBuilder;

@Mixin(DataFixers.class)
public abstract class DataFixesManagerMixin {

    @Redirect(method = "createFixerUpper", at = @At(value = "NEW", target = "com/mojang/datafixers/DataFixerBuilder"))
    private static DataFixerBuilder createFixerUpperProxy(int dataVersion) {
        return new LazyDataFixerBuilder(dataVersion);
    }
}
