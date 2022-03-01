package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.NBTTypes;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemSpawnEgg<E extends Entity> extends ItemEv {

    private final Supplier<EntityType<E>> type;

    public ItemSpawnEgg(Supplier<EntityType<E>> type) {
        super(EvolutionItems.propEgg());
        this.type = type;
    }

    public EntityType<?> getType(@Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains("EntityTag", NBTTypes.COMPOUND_NBT)) {
            CompoundTag entityTag = nbt.getCompound("EntityTag");
            if (entityTag.contains("id", NBTTypes.STRING)) {
                return EntityType.byString(entityTag.getString("id")).orElseGet(this.type);
            }
        }
        return this.type.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.success(stack);
        }
        BlockPos pos = hitResult.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.mayInteract(player, pos) && player.mayUseItemAt(pos, hitResult.getDirection(), stack)) {
            EntityType<?> entityType = this.getType(stack.getTag());
            if (entityType.spawn((ServerLevel) level, stack, player, pos, MobSpawnType.SPAWN_EGG, false, false) == null) {
                return InteractionResultHolder.pass(stack);
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.SPAWNER) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof SpawnerBlockEntity spawnerBlockEntity) {
                BaseSpawner spawner = spawnerBlockEntity.getSpawner();
                EntityType<?> entityType = this.getType(stack.getTag());
                spawner.setEntityId(entityType);
                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                if (!context.getPlayer().isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        BlockPos movedPos;
        if (state.getCollisionShape(level, pos).isEmpty()) {
            movedPos = pos;
        }
        else {
            movedPos = pos.relative(direction);
        }
        EntityType<?> entityType = this.getType(stack.getTag());
        if (entityType.spawn((ServerLevel) level,
                             stack,
                             context.getPlayer(),
                             movedPos,
                             MobSpawnType.SPAWN_EGG,
                             true,
                             !pos.equals(movedPos) && direction == Direction.UP) != null) {
            if (!context.getPlayer().isCreative()) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
