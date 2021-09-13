package tgw.evolution.entities.projectiles;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntityTorch extends EntityGenericProjectile<EntityTorch> {

    private long timeCreated;

    public EntityTorch(World worldIn, LivingEntity shooter, long timeCreated) {
        super(EvolutionEntities.TORCH.get(), shooter, worldIn, 0.2);
        this.timeCreated = timeCreated;
    }

    @SuppressWarnings("unused")
    public EntityTorch(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.TORCH.get(), worldIn);
    }

    public EntityTorch(EntityType<EntityTorch> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putLong("TimeCreated", this.timeCreated);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getArrowStack() {
        return ItemTorch.createStack(this.timeCreated, 1);
    }

    @Nullable
    @Override
    public HitboxEntity<EntityTorch> getHitbox() {
        return null;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        SoundEvent soundevent = SoundEvents.ARROW_HIT;
        this.playSound(soundevent, 1.0F, 1.0F);
        Evolution.usingPlaceholder(this.level.getNearestPlayer(this, 128), "sound");
        BlockUtils.dropItemStack(this.level, this.blockPosition(), this.getArrowStack());
        this.remove();
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.timeCreated = compound.getLong("TimeCreated");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            BlockPos pos = this.blockPosition();
            this.level.playSound(null,
                                 pos,
                                 SoundEvents.FIRE_EXTINGUISH,
                                 SoundCategory.BLOCKS,
                                 1.0F,
                                 2.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.8F);
            BlockUtils.dropItemStack(this.level, pos, new ItemStack(EvolutionItems.torch_unlit.get()));
            this.remove();
            return;
        }
        if (this.timeInGround > 0) {
            this.tryPlaceBlock();
            this.remove();
        }
    }

    @Override
    protected void tryDespawn() {
    }

    public void tryPlaceBlock() {
        if (this.level.isClientSide) {
            return;
        }
        BlockPos pos = this.blockPosition();
        if (this.level.isEmptyBlock(pos)) {
            BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromYawAndPitch(this, 1, false);
            Direction face = rayTrace.getDirection();
            if (BlockUtils.hasSolidSide(this.level, pos.relative(face.getOpposite()), face)) {
                if (face == Direction.UP) {
                    BlockState state = EvolutionBlocks.TORCH.get().defaultBlockState();
                    this.level.setBlockAndUpdate(pos, state);
                    TileEntity tile = this.level.getBlockEntity(pos);
                    if (tile instanceof TETorch) {
                        ((TETorch) tile).setTimePlaced(this.timeCreated);
                    }
                    return;
                }
            }
        }
        BlockUtils.dropItemStack(this.level, pos, this.getArrowStack());
    }
}
