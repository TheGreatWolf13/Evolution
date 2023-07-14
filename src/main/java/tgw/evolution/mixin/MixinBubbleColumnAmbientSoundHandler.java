package tgw.evolution.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BubbleColumnAmbientSoundHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BubbleColumnAmbientSoundHandler.class)
public abstract class MixinBubbleColumnAmbientSoundHandler implements AmbientSoundHandler {

    @Shadow private boolean firstTick;
    @Shadow @Final private LocalPlayer player;
    @Shadow private boolean wasInBubbleColumn;

    @Override
    @Overwrite
    public void tick() {
        Level level = this.player.level;
        AABB bb = this.player.getBoundingBox();
        int x0 = Mth.floor(bb.minX + 1E-6);
        int y0 = Mth.floor(bb.minY + 0.4 + 1E-6);
        int z0 = Mth.floor(bb.minZ + 1E-6);
        int x1 = Mth.floor(bb.maxX - 1E-6);
        int y1 = Mth.floor(bb.maxY - 0.4 - 1E-6);
        int z1 = Mth.floor(bb.maxZ - 1E-6);
        BlockState state = null;
        if (level.hasChunksAt(x0, y0, z0, x1, y1, z1)) {
            for (int x = x0; x <= x1; ++x) {
                for (int y = y0; y <= y1; ++y) {
                    for (int z = z0; z <= z1; ++z) {
                        BlockState bs = level.getBlockState_(x, y, z);
                        if (bs.is(Blocks.BUBBLE_COLUMN)) {
                            state = bs;
                            break;
                        }
                    }
                }
            }
        }
        if (state != null) {
            if (!this.wasInBubbleColumn && !this.firstTick && !this.player.isSpectator()) {
                boolean dragDown = state.getValue(BubbleColumnBlock.DRAG_DOWN);
                if (dragDown) {
                    this.player.playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0F, 1.0F);
                }
                else {
                    this.player.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
                }
            }
            this.wasInBubbleColumn = true;
        }
        else {
            this.wasInBubbleColumn = false;
        }
        this.firstTick = false;
    }
}
