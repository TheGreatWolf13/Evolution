package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.List;

@Mixin(EndCrystalItem.class)
public abstract class Mixin_M_EndCrystalItem extends Item {

    public Mixin_M_EndCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        }
        if (!level.isEmptyBlock_(x, y + 1, z)) {
            return InteractionResult.FAIL;
        }
        List<Entity> list = level.getEntities(null, new AABB(x, y + 1, z, x + 1, y + 3, z + 1));
        if (!list.isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel l) {
            EndCrystal endCrystal = new EndCrystal(level, x + 0.5, y + 1, z + 0.5);
            endCrystal.setShowBottom(false);
            level.addFreshEntity(endCrystal);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, new BlockPos(x, y + 1, z));
            EndDragonFight endDragonFight = l.dragonFight();
            if (endDragonFight != null) {
                endDragonFight.tryRespawn();
            }
        }
        player.getItemInHand(hand).shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
