package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(Turtle.class)
public abstract class Mixin_M_Turtle extends Animal {

    public Mixin_M_Turtle(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void playStepSound(int x, int y, int z, BlockState state) {
        this.playSound(this.isBaby() ? SoundEvents.TURTLE_SHAMBLE_BABY : SoundEvents.TURTLE_SHAMBLE, 0.15F, 1.0F);
    }
}
