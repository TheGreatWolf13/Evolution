package tgw.evolution.entities.projectiles;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.MathHelper;

public class EntityTorch extends AbstractArrowEntity {

    private final double gravity;
    private final double verticalDrag;
    private final double horizontalDrag;

    public EntityTorch(EntityType<EntityTorch> type, World worldIn) {
        super(type, worldIn);
        this.gravity = -Gravity.gravity(worldIn.dimension);
        this.verticalDrag = Gravity.verticalDrag(worldIn.dimension, this.getWidth());
        this.horizontalDrag = Gravity.horizontalDrag(worldIn.dimension, this.getWidth(), this.getHeight());
    }

    public EntityTorch(World worldIn, LivingEntity shooter) {
        super(EvolutionEntities.TORCH.get(), shooter, worldIn);
        this.gravity = -Gravity.gravity(worldIn.dimension);
        this.verticalDrag = Gravity.verticalDrag(worldIn.dimension, this.getWidth());
        this.horizontalDrag = Gravity.horizontalDrag(worldIn.dimension, this.getWidth(), this.getHeight());
    }

    @SuppressWarnings("unused")
    public EntityTorch(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.TORCH.get(), worldIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(EvolutionItems.torch.get());
    }

    @Override
    protected void tryDespawn() {
    }

    @Override
    public void tick() {
        if (this.timeInGround > 0) {
            this.tryPlaceBlock();
            this.remove();
        }
        if (!this.hasNoGravity() && !this.getNoClip()) {
            Vec3d motion = this.getMotion();
            this.setMotion(motion.x * this.horizontalDrag, (motion.y + 0.05F + this.gravity) * this.verticalDrag, motion.z * this.horizontalDrag);
        }
        if (this.isInWater()) {
            BlockPos pos = this.getPosition();
            this.world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1F, 2.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.8F);
            BlockUtils.dropItemStack(this.world, pos, new ItemStack(EvolutionItems.stick.get()));
            this.remove();
            return;
        }
        super.tick();
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        SoundEvent soundevent = SoundEvents.ENTITY_ARROW_HIT;
        this.playSound(soundevent, 1.0F, 1.0F);
        BlockUtils.dropItemStack(this.world, this.getPosition(), this.getArrowStack());
        this.remove();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
                    return;
                }
            }
        }
        BlockUtils.dropItemStack(this.world, pos, this.getArrowStack());
    }
}
