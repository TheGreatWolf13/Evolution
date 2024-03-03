package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(HoeItem.class)
public abstract class Mixin_M_HoeItem extends DiggerItem {

    @Shadow @Final protected static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES;

    public Mixin_M_HoeItem(float f,
                           float g,
                           Tier tier,
                           TagKey<Block> tagKey,
                           Properties properties) {
        super(f, g, tier, tagKey, properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = TILLABLES.get(level.getBlockState_(x, y, z).getBlock());
        if (pair == null) {
            return InteractionResult.PASS;
        }
        Predicate<UseOnContext> predicate = pair.getFirst();
        Consumer<UseOnContext> consumer = pair.getSecond();
        UseOnContext context = new UseOnContext(player, hand, hitResult);
        if (predicate.test(context)) {
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
                consumer.accept(context);
                context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
