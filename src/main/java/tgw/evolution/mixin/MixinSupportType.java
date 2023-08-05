package tgw.evolution.mixin;

import net.minecraft.world.level.block.SupportType;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchSupportType;

@Mixin(SupportType.class)
public abstract class MixinSupportType implements PatchSupportType {
}
