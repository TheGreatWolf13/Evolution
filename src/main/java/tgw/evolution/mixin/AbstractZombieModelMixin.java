package tgw.evolution.mixin;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.hitbox.hms.LegacyHMAbstractZombie;

@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin<T extends Monster> extends HumanoidModel<T> implements LegacyHMAbstractZombie<T> {

    public AbstractZombieModelMixin(ModelPart pRoot) {
        super(pRoot);
    }

    @Override
    public boolean aggresive(T entity) {
        return this.isAggressive(entity);
    }

    @Shadow
    public abstract boolean isAggressive(T pEntity);

    /**
     * @author TheGreatWolf
     * @reason Use HMs
     */
    @Override
    @Overwrite
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
