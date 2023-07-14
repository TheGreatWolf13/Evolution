package tgw.evolution.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;

@Mixin(GiveCommand.class)
public abstract class MixinGiveCommand {

    /**
     * @author TheGreatWolf
     * @reason Create the item only once
     */
    @Overwrite
    private static int giveItem(CommandSourceStack source, ItemInput item, Collection<ServerPlayer> targets, int count) throws
                                                                                                                        CommandSyntaxException {
        int maxStackSize = item.getItem().getMaxStackSize();
        int max = maxStackSize * 100;
        ItemStack stack = item.createItemStack(Math.min(maxStackSize, count), false);
        if (count > max) {
            source.sendFailure(new TranslatableComponent("commands.give.failed.toomanyitems", max, stack.getDisplayName()));
            return 0;
        }
        for (ServerPlayer player : targets) {
            int k = count;
            while (k > 0) {
                int l = Math.min(maxStackSize, k);
                k -= l;
                ItemStack itemStack = stack.copy();
                boolean couldAdd = player.getInventory().add(itemStack);
                if (couldAdd && itemStack.isEmpty()) {
                    itemStack.setCount(1);
                    ItemEntity entity = player.drop(itemStack, false);
                    if (entity != null) {
                        entity.makeFakeItem();
                    }
                    player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                           SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                                           ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) *
                                           2.0F);
                    player.containerMenu.broadcastChanges();
                }
                else {
                    ItemEntity entity = player.drop(itemStack, false);
                    if (entity != null) {
                        entity.setNoPickUpDelay();
                        entity.setOwner(player.getUUID());
                    }
                }
            }
        }
        if (targets.size() == 1) {
            source.sendSuccess(new TranslatableComponent("commands.give.success.single", count, stack.getDisplayName(),
                                                         targets.iterator().next().getDisplayName()), true);
        }
        else {
            source.sendSuccess(new TranslatableComponent("commands.give.success.single", count, stack.getDisplayName(), targets.size()), true);
        }
        return targets.size();
    }
}
