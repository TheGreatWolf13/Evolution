package tgw.evolution.mixin;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface AccessorCreativeModeTab {

    @Mutable
    @Accessor(value = "TABS")
    static void setTabs(CreativeModeTab[] tabs) {
        throw new AbstractMethodError();
    }
}
