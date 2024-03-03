package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(AbstractHorse.class)
public abstract class Mixin_M_AbstractHorse extends Animal implements ContainerListener, PlayerRideableJumping, Saddleable {

    @Shadow protected boolean canGallop;

    @Shadow protected int gallopSoundCounter;

    public Mixin_M_AbstractHorse(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract void playGallopSound(SoundType soundType);

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
        if (!state.getMaterial().isLiquid()) {
            BlockState stateAbove = this.level.getBlockState_(x, y + 1, z);
            SoundType soundType = state.getSoundType();
            if (stateAbove.is(Blocks.SNOW)) {
                soundType = stateAbove.getSoundType();
            }
            if (this.isVehicle() && this.canGallop) {
                ++this.gallopSoundCounter;
                if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                    this.playGallopSound(soundType);
                }
                else if (this.gallopSoundCounter <= 5) {
                    this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15F, soundType.getPitch());
                }
            }
            else if (soundType == SoundType.WOOD) {
                this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15F, soundType.getPitch());
            }
            else {
                this.playSound(SoundEvents.HORSE_STEP, soundType.getVolume() * 0.15F, soundType.getPitch());
            }
        }
    }
}
