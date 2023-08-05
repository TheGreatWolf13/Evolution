package tgw.evolution.entities.projectiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.IProjectile;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.physics.SI;

public class EntityTorch extends EntityGenericProjectile {

    private long timeCreated;

    public EntityTorch(Level level, LivingEntity shooter, long timeCreated) {
        super(EvolutionEntities.TORCH, shooter, level, 0.2);
        this.timeCreated = timeCreated;
    }

    public EntityTorch(EntityType<EntityTorch> type, Level level) {
        super(type, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("TimeCreated", this.timeCreated);
    }

    @Override
    protected DamageSourceEv createDamageSource() {
        return EvolutionDamage.DUMMY;
    }

    @Override
    protected boolean damagesEntities() {
        return false;
    }

    @Override
    protected ItemStack getArrowStack() {
        return ItemTorch.createStack(this.timeCreated, 1);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected @Nullable IProjectile getProjectile() {
        return null;
    }

    @Override
    public double getVolume() {
        return 10.0 * 2 * 2 / 16 / 16 / 16 * SI.CUBIC_METER;
    }

    @Override
    protected void modifyMovementOnCollision() {
    }

    @Override
    protected void onBlockHit(BlockState state, int x, int y, int z) {
    }

    @Override
    protected boolean onHitEntityLogic() {
        SoundEvent soundevent = SoundEvents.ARROW_HIT;
        this.playSound(soundevent, 1.0F, 1.0F);
        Player player = this.level.getNearestPlayer(this, 128);
        if (player != null) {
            Evolution.usingPlaceholder(player, "sound");
        }
        BlockPos pos = this.blockPosition();
        BlockUtils.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.getArrowStack());
        this.discard();
        return true;
    }

    @Override
    protected void playHitEntitySound() {
    }

    @Override
    protected void postHitLogic(boolean attackSuccessful) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.timeCreated = tag.getLong("TimeCreated");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            BlockPos pos = this.blockPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            this.level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F,
                                 2.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.8F);
            BlockUtils.dropItemStack(this.level, x, y, z, new ItemStack(EvolutionItems.TORCH_UNLIT));
            this.discard();
            return;
        }
        if (this.timeInGround > 0) {
            this.tryPlaceBlock();
            this.discard();
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
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (this.level.isEmptyBlock_(x, y, z)) {
            BlockHitResult hitResult = MathHelper.rayTraceBlocksFromYawAndPitch(this, 1, false);
            Direction face = hitResult.getDirection();
            if (BlockUtils.hasSolidFaceAtSide(this.level, x, y, z, face.getOpposite())) {
                if (face == Direction.UP) {
                    BlockState state = EvolutionBlocks.TORCH.defaultBlockState();
                    this.level.setBlockAndUpdate(pos, state);
                    BlockEntity tile = this.level.getBlockEntity_(x, y, z);
                    if (tile instanceof TETorch teTorch) {
                        teTorch.setTimePlaced(this.timeCreated);
                    }
                    return;
                }
            }
        }
        BlockUtils.dropItemStack(this.level, x, y, z, this.getArrowStack());
    }
}
