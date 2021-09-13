package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.NBTTypes;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemSpawnEgg<E extends Entity> extends ItemEv {

    private final Supplier<EntityType<E>> type;

    public ItemSpawnEgg(Supplier<EntityType<E>> type) {
        super(EvolutionItems.propEgg());
        this.type = type;
    }

    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("EntityTag", NBTTypes.COMPOUND_NBT)) {
            CompoundNBT entityTag = nbt.getCompound("EntityTag");
            if (entityTag.contains("id", NBTTypes.STRING)) {
                return EntityType.byString(entityTag.getString("id")).orElseGet(this.type);
            }
        }
        return this.type.get();
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockRayTraceResult rayTrace = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (rayTrace.getType() != RayTraceResult.Type.BLOCK) {
            return ActionResult.pass(stack);
        }
        if (!(world instanceof ServerWorld)) {
            return ActionResult.success(stack);
        }
        BlockPos pos = rayTrace.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof FlowingFluidBlock)) {
            return ActionResult.pass(stack);
        }
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, rayTrace.getDirection(), stack)) {
            EntityType<?> entityType = this.getType(stack.getTag());
            if (entityType.spawn((ServerWorld) world, stack, player, pos, SpawnReason.SPAWN_EGG, false, false) == null) {
                return ActionResult.pass(stack);
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        if (!(world instanceof ServerWorld)) {
            return ActionResultType.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.SPAWNER) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof MobSpawnerTileEntity) {
                AbstractSpawner spawner = ((MobSpawnerTileEntity) tile).getSpawner();
                EntityType<?> entityType = this.getType(stack.getTag());
                spawner.setEntityId(entityType);
                tile.setChanged();
                world.sendBlockUpdated(pos, state, state, 3);
                if (!context.getPlayer().isCreative()) {
                    stack.shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
        }
        BlockPos movedPos;
        if (state.getCollisionShape(world, pos).isEmpty()) {
            movedPos = pos;
        }
        else {
            movedPos = pos.relative(direction);
        }
        EntityType<?> entityType = this.getType(stack.getTag());
        if (entityType.spawn((ServerWorld) world,
                             stack,
                             context.getPlayer(),
                             movedPos,
                             SpawnReason.SPAWN_EGG,
                             true,
                             !pos.equals(movedPos) && direction == Direction.UP) != null) {
            if (!context.getPlayer().isCreative()) {
                stack.shrink(1);
            }
        }
        return ActionResultType.SUCCESS;
    }
}
