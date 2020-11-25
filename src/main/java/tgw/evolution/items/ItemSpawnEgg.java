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

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos();
        Direction direction = context.getFace();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.SPAWNER) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof MobSpawnerTileEntity) {
                AbstractSpawner spawner = ((MobSpawnerTileEntity) tile).getSpawnerBaseLogic();
                EntityType<?> entityType = this.getType(stack.getTag());
                spawner.setEntityType(entityType);
                tile.markDirty();
                world.notifyBlockUpdate(pos, state, state, 3);
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
            movedPos = pos.offset(direction);
        }
        EntityType<?> entityType = this.getType(stack.getTag());
        if (entityType.spawn(world,
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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        RayTraceResult rayTrace = rayTrace(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (rayTrace.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
        BlockPos pos = blockRayTrace.getPos();
        if (!(world.getBlockState(pos).getBlock() instanceof FlowingFluidBlock)) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        if (world.isBlockModifiable(player, pos) && player.canPlayerEdit(pos, blockRayTrace.getFace(), stack)) {
            EntityType<?> entityType = this.getType(stack.getTag());
            if (entityType.spawn(world, stack, player, pos, SpawnReason.SPAWN_EGG, false, false) == null) {
                return new ActionResult<>(ActionResultType.PASS, stack);
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            player.addStat(Stats.ITEM_USED.get(this));
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("EntityTag", NBTTypes.COMPOUND_NBT.getId())) {
            CompoundNBT entityTag = nbt.getCompound("EntityTag");
            if (entityTag.contains("id", NBTTypes.STRING.getId())) {
                return EntityType.byKey(entityTag.getString("id")).orElseGet(this.type);
            }
        }
        return this.type.get();
    }
}
