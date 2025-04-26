package tgw.evolution.mixin;

import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchEntityStorage;

@Mixin(EntityPersistentStorage.class)
public interface MixinEntityPersistentStorage<T> extends PatchEntityStorage<T> {

}
