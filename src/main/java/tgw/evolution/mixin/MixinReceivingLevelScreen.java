package tgw.evolution.mixin;

import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ReceivingLevelScreen.class)
public abstract class MixinReceivingLevelScreen extends Screen {

    @Shadow @Final private long createdAt;
    @Shadow private boolean loadingPacketsReceived;
    @Shadow private boolean oneTickSkipped;

    public MixinReceivingLevelScreen(Component pTitle) {
        super(pTitle);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void tick() {
        if ((this.oneTickSkipped || System.currentTimeMillis() > this.createdAt + 2_000L) &&
            this.minecraft != null &&
            this.minecraft.player != null) {
            BlockPos pos = this.minecraft.player.blockPosition();
            if (this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(pos.getY()) ||
                this.minecraft.lvlRenderer().isChunkCompiled(pos.getX(), pos.getY(), pos.getZ())) {
                this.onClose();
            }
            if (this.loadingPacketsReceived) {
                this.oneTickSkipped = true;
            }
        }
    }
}
