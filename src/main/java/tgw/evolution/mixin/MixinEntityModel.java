package tgw.evolution.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.hitbox.hms.HMEntity;

import java.util.function.Function;

@Mixin(EntityModel.class)
public abstract class MixinEntityModel<T extends Entity> extends Model implements HMEntity<T> {

    @Shadow public float attackTime;
    @Shadow public boolean riding;
    @Shadow public boolean young;

    public MixinEntityModel(Function<ResourceLocation, RenderType> pRenderType) {
        super(pRenderType);
    }

    @Override
    public float attackTime() {
        return this.attackTime;
    }

    /**
     * @author TheGreatWolf
     * @reason Use HMs
     */
    @Overwrite
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.prepare(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    @Override
    public boolean riding() {
        return this.riding;
    }

    @Override
    public void setAttackTime(float attackTime) {
        this.attackTime = attackTime;
    }

    @Override
    public void setRiding(boolean riding) {
        this.riding = riding;
    }

    @Override
    public void setYoung(boolean young) {
        this.young = young;
    }

    @Override
    public boolean young() {
        return this.young;
    }
}
