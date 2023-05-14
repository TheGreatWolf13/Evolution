package tgw.evolution.mixin;

import com.mojang.text2speech.Narrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.chat.NarratorChatListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.INarratorChatListenerPatch;

@Mixin(NarratorChatListener.class)
public abstract class NarratorChatListenerMixin implements INarratorChatListenerPatch {

    @Shadow
    @Final
    private Narrator narrator;

    /**
     * @author TheGreatWolf
     * @reason A lot of screen computations and allocations take place even if narration is not enabled.
     */
    @Overwrite
    public boolean isActive() {
        return this.isAvailable() && Minecraft.getInstance().options.narratorStatus != NarratorStatus.OFF;
    }

    @Override
    public boolean isAvailable() {
        return this.narrator.active();
    }
}
