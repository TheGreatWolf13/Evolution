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
import java.util.Objects;
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
        ItemStack itemstack = context.getItem();
        BlockPos blockpos = context.getPos();
        Direction direction = context.getFace();
        BlockState blockstate = world.getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (block == Blocks.SPAWNER) {
            TileEntity tileentity = world.getTileEntity(blockpos);
            if (tileentity instanceof MobSpawnerTileEntity) {
                AbstractSpawner abstractspawner = ((MobSpawnerTileEntity) tileentity).getSpawnerBaseLogic();
                EntityType<?> entitytype1 = this.getType(itemstack.getTag());
                abstractspawner.setEntityType(entitytype1);
                tileentity.markDirty();
                world.notifyBlockUpdate(blockpos, blockstate, blockstate, 3);
                itemstack.shrink(1);
                return ActionResultType.SUCCESS;
            }
        }
        BlockPos blockpos1;
        if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
            blockpos1 = blockpos;
        }
        else {
            blockpos1 = blockpos.offset(direction);
        }
        EntityType<?> entitytype = this.getType(itemstack.getTag());
        if (entitytype.spawn(world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
            itemstack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (raytraceresult.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) raytraceresult;
        BlockPos blockpos = blockraytraceresult.getPos();
        if (!(worldIn.getBlockState(blockpos).getBlock() instanceof FlowingFluidBlock)) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        if (worldIn.isBlockModifiable(playerIn, blockpos) && playerIn.canPlayerEdit(blockpos, blockraytraceresult.getFace(), stack)) {
            EntityType<?> entitytype = this.getType(stack.getTag());
            if (entitytype.spawn(worldIn, stack, playerIn, blockpos, SpawnReason.SPAWN_EGG, false, false) == null) {
                return new ActionResult<>(ActionResultType.PASS, stack);
            }
            if (!playerIn.abilities.isCreativeMode) {
                stack.shrink(1);
            }
            playerIn.addStat(Stats.ITEM_USED.get(this));
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
