package tgw.evolution.mixin;

import net.minecraft.client.CycleOption;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.Option;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.INarratorChatListenerPatch;

@Mixin(Option.class)
public abstract class OptionMixin {

    @Mutable
    @Shadow
    @Final
    public static CycleOption<NarratorStatus> NARRATOR;

    static {
        NARRATOR = CycleOption.create("options.narrator", NarratorStatus.values(),
                                      status -> ((INarratorChatListenerPatch) NarratorChatListener.INSTANCE).isAvailable() ?
                                                status.getName() : new TranslatableComponent("options.narrator.notavailable"),
                                      op -> op.narratorStatus, (ops, op, status) -> {
                    ops.narratorStatus = status;
                    NarratorChatListener.INSTANCE.updateNarratorStatus(status);
                });
    }
}
