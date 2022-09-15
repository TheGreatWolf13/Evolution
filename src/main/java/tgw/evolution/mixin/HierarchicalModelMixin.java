package tgw.evolution.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.util.hitbox.hms.HMHierarchical;

@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin<T extends Entity> extends EntityModel<T> implements HMHierarchical<T> {

}
