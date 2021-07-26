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
    public IPacket<?> createSpawnPacket() {
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
        SoundEvent soundevent = SoundEvents.ENTITY_ARROW_HIT;
        this.playSound(soundevent, 1.0F, 1.0F);
        Evolution.usingPlaceholder(this.world.getClosestPlayer(this, 1_000), "sound");
        BlockUtils.dropItemStack(this.world, this.getPosition(), this.getArrowStack());
        this.remove();
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.timeCreated = compound.getLong("TimeCreated");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            BlockPos pos = this.getPosition();
            this.world.playSound(null,
                                 pos,
                                 SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                 SoundCategory.BLOCKS,
                                 1.0F,
                                 2.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.8F);
            BlockUtils.dropItemStack(this.world, pos, new ItemStack(EvolutionItems.torch_unlit.get()));
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
        if (this.world.isRemote) {
            return;
        }
        BlockPos pos = this.getPosition();
        if (this.world.isAirBlock(pos)) {
            BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromYawAndPitch(this, 1, false);
            Direction face = rayTrace.getFace();
            if (BlockUtils.hasSolidSide(this.world, pos.offset(face.getOpposite()), face)) {
                if (face == Direction.UP) {
                    BlockState state = EvolutionBlocks.TORCH.get().getDefaultState();
                    this.world.setBlockState(pos, state);
                    TileEntity tile = this.world.getTileEntity(pos);
                    if (tile instanceof TETorch) {
                        ((TETorch) tile).setTimePlaced(this.timeCreated);
                    }
                    return;
                }
            }
        }
        BlockUtils.dropItemStack(this.world, pos, this.getArrowStack());
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putLong("TimeCreated", this.timeCreated);
    }
}
