package tgw.evolution.mixin;

import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.ResetChunksCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.init.EvolutionCommands;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(Commands.class)
public abstract class MixinCommands {

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V"),
            remap = false)
    private <S> void onInit(CommandDispatcher instance, AmbiguityConsumer<S> consumer) {
        EvolutionCommands.register(instance);
        ResetChunksCommand.register(instance);
        instance.findAmbiguities(consumer);
    }
}
