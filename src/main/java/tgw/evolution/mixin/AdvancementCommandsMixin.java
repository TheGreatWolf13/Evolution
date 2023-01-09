package tgw.evolution.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.capabilities.toast.CapabilityToast;
import tgw.evolution.capabilities.toast.IToastData;
import tgw.evolution.init.EvolutionCapabilities;

import java.util.Collection;

@Mixin(AdvancementCommands.class)
public abstract class AdvancementCommandsMixin {

    /**
     * @author TheGreatWolf
     * @reason Include custom toasts
     */
    @Overwrite
    private static int perform(CommandSourceStack source,
                               Collection<ServerPlayer> targets,
                               AdvancementCommands.Action action,
                               Collection<Advancement> advancements) {
        int i = 0;
        for (ServerPlayer player : targets) {
            i += action.perform(player, advancements);
        }
        if (action == AdvancementCommands.Action.REVOKE) {
            for (ServerPlayer player : targets) {
                IToastData toastData = EvolutionCapabilities.getCapabilityOrThrow(player, CapabilityToast.INSTANCE);
                toastData.reset();
            }
        }
        if (i == 0) {
            if (advancements.size() == 1) {
                if (targets.size() == 1) {
                    throw new CommandRuntimeException(
                            new TranslatableComponent(action.getKey() + ".one.to.one.failure", advancements.iterator().next().getChatComponent(),
                                                      targets.iterator().next().getDisplayName()));
                }
                throw new CommandRuntimeException(
                        new TranslatableComponent(action.getKey() + ".one.to.many.failure", advancements.iterator().next().getChatComponent(),
                                                  targets.size()));
            }
            if (targets.size() == 1) {
                throw new CommandRuntimeException(new TranslatableComponent(action.getKey() + ".many.to.one.failure", advancements.size(),
                                                                            targets.iterator().next().getDisplayName()));
            }
            throw new CommandRuntimeException(
                    new TranslatableComponent(action.getKey() + ".many.to.many.failure", advancements.size(), targets.size()));
        }
        if (advancements.size() == 1) {
            if (targets.size() == 1) {
                source.sendSuccess(
                        new TranslatableComponent(action.getKey() + ".one.to.one.success", advancements.iterator().next().getChatComponent(),
                                                  targets.iterator().next().getDisplayName()), true);
            }
            else {
                source.sendSuccess(
                        new TranslatableComponent(action.getKey() + ".one.to.many.success", advancements.iterator().next().getChatComponent(),
                                                  targets.size()), true);
            }
        }
        else if (targets.size() == 1) {
            source.sendSuccess(new TranslatableComponent(action.getKey() + ".many.to.one.success", advancements.size(),
                                                         targets.iterator().next().getDisplayName()), true);
        }
        else {
            source.sendSuccess(new TranslatableComponent(action.getKey() + ".many.to.many.success", advancements.size(), targets.size()), true);
        }
        return i;
    }
}
