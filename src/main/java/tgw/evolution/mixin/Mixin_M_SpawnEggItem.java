package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SpawnEggItem.class)
public abstract class Mixin_M_SpawnEggItem extends Item {

    public Mixin_M_SpawnEggItem(Properties properties) {
        super(properties);
    }

    @Shadow
    public abstract EntityType<?> getType(@Nullable CompoundTag compoundTag);

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        Direction direction = hitResult.getDirection();
        BlockState state = level.getBlockState_(x, y, z);
        if (state.is(Blocks.SPAWNER)) {
            if (level.getBlockEntity_(x, y, z) instanceof SpawnerBlockEntity tile) {
                BaseSpawner baseSpawner = tile.getSpawner();
                EntityType<?> entityType = this.getType(stack.getTag());
                baseSpawner.setEntityId(entityType);
                tile.setChanged();
                level.sendBlockUpdated(new BlockPos(x, y, z), state, state, 3);
                stack.shrink(1);
                return InteractionResult.CONSUME;
            }
        }
        int spawnX = x;
        int spawnY = y;
        int spawnZ = z;
        boolean changed = false;
        if (!state.getCollisionShape_(level, x, y, z).isEmpty()) {
            switch (direction) {
                case WEST -> --spawnX;
                case EAST -> ++spawnX;
                case DOWN -> --spawnY;
                case UP -> ++spawnY;
                case NORTH -> --spawnZ;
                case SOUTH -> ++spawnZ;
            }
            changed = true;
        }
        EntityType<?> type = this.getType(stack.getTag());
        if (type.spawn((ServerLevel) level, stack, player, new BlockPos(spawnX, spawnY, spawnZ), MobSpawnType.SPAWN_EGG, true,
                       changed && direction == Direction.UP) != null) {
            stack.shrink(1);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, new BlockPos(x, y, z));
        }
        return InteractionResult.CONSUME;
    }
}
