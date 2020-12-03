package tgw.evolution.events;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.entities.misc.EntityFallingTimber;

public class FallingEvents {

    public static boolean isFalling;
    public static boolean sound = true;

    public static void chopEvent(IWorld worldIn, BlockState state, BlockPos pos, Direction direction) {
        if (isFalling) {
            return;
        }
        if (!(state.getBlock() instanceof BlockLog)) {
            return;
        }
        if (!state.get(BlockLog.TREE)) {
            return;
        }
        isFalling = true;
        FallingManager.fallingManagers.computeIfAbsent(worldIn, FallingManager::new).onChop(pos, direction);
    }

    @SubscribeEvent
    public void chopEvent(BlockEvent.BreakEvent event) {
        if (!(event.getState().getBlock() instanceof BlockLog)) {
            return;
        }
        if (!event.getState().get(BlockLog.TREE)) {
            return;
        }
        isFalling = true;
        Direction fallingDirection = event.getPlayer().getHorizontalFacing();
        IWorld worldIn = event.getWorld();
        FallingManager.fallingManagers.computeIfAbsent(worldIn, FallingManager::new).onChop(event.getPos(), fallingDirection);
    }

    @SubscribeEvent
    public void entityJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityFallingTimber)) {
            return;
        }
        EntityFallingTimber falling = (EntityFallingTimber) event.getEntity();
        if (falling.getBlockState() == null) {
            return;
        }
        if (!(falling.getBlockState().getBlock() instanceof BlockLog)) {
            if (falling.getBlockState().getMaterial() != Material.LEAVES) {
                return;
            }
        }
        if (falling.fallTime <= 600) {
            return;
        }
        falling.remove();
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.side != LogicalSide.SERVER) {
            return;
        }
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        FallingManager fallingManager = FallingManager.fallingManagers.get(event.world);
        if (fallingManager == null) {
            return;
        }
        fallingManager.tick();
        if (!fallingManager.isEmpty()) {
            return;
        }
        FallingManager.fallingManagers.remove(event.world);
        isFalling = false;
        sound = true;
    }
}
