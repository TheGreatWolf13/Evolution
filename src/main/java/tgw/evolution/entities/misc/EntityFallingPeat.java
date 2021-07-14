package tgw.evolution.entities.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.BlockPeat;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.NBTTypes;

import javax.annotation.Nullable;

public class EntityFallingPeat extends Entity implements IEntityAdditionalSpawnData {

    public static final EntitySize SIZE_1 = EntitySize.flexible(1.0f, 0.25f);
    public static final EntitySize SIZE_2 = EntitySize.flexible(1.0f, 0.5f);
    public static final EntitySize SIZE_3 = EntitySize.flexible(1.0f, 0.75f);
    public static final EntitySize SIZE_4 = EntitySize.flexible(1.0f, 1.0f);
    public static final EntitySize[] SIZES = {SIZE_1, SIZE_2, SIZE_3, SIZE_4};
    public int fallTime;
    private boolean isSizeCorrect;
    private int layers;
    private int mass = 289;
    private BlockPos prevPos;

    public EntityFallingPeat(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.FALLING_PEAT.get(), worldIn);
    }

    public EntityFallingPeat(EntityType<EntityFallingPeat> type, World worldIn) {
        super(type, worldIn);
    }

    public EntityFallingPeat(World worldIn, double x, double y, double z, int layers) {
        super(EvolutionEntities.FALLING_PEAT.get(), worldIn);
        this.preventEntitySpawning = true;
        this.setPosition(x, y, z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.layers = layers;
        this.mass = 289 * this.layers;
        this.prevPos = new BlockPos(this);
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public BlockState getBlockState() {
        return EvolutionBlocks.PEAT.get().getDefaultState().with(EvolutionBStates.LAYERS_1_4, this.layers);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.isAlive() ? this.getBoundingBox() : null;
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return SIZES[this.layers - 1];
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.fallTime = compound.getInt("Time");
        if (compound.contains("Layers", NBTTypes.BYTE)) {
            this.layers = compound.getByte("Layers");
        }
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.layers = buffer.readByte();
    }

    @Override
    protected void registerData() {
    }

    @Override
    public void tick() {
        if (!this.isSizeCorrect) {
            this.recalculateSize();
            this.isSizeCorrect = true;
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.fallTime;
        Vec3d motion = this.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        double gravity = 0;
        if (!this.hasNoGravity()) {
            gravity = Gravity.gravity(this.world.dimension);
        }
        double horizontalDrag = this.isInWater() ? Gravity.horizontalWaterDrag(this) / this.mass : Gravity.horizontalDrag(this) / this.mass;
        double verticalDrag = this.isInWater() ? Gravity.verticalWaterDrag(this) / this.mass : Gravity.verticalDrag(this) / this.mass;
        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
        if (Math.abs(dragX) > Math.abs(motionX)) {
            dragX = motionX;
        }
        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
        if (Math.abs(dragY) > Math.abs(motionY)) {
            dragY = motionY;
        }
        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
        if (Math.abs(dragZ) > Math.abs(motionZ)) {
            dragZ = motionZ;
        }
        motionX -= dragX;
        motionY += -gravity - dragY;
        motionZ -= dragZ;
        if (Math.abs(motionX) < 1e-6) {
            motionX = 0;
        }
        if (Math.abs(motionY) < 1e-6) {
            motionY = 0;
        }
        if (Math.abs(motionZ) < 1e-6) {
            motionZ = 0;
        }
        this.setMotion(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getMotion());
        BlockPos pos = new BlockPos(this);
        if (!this.world.isRemote) {
            if (!this.onGround) {
                if (this.fallTime > 100 && (pos.getY() < 1 || pos.getY() > 256) || this.fallTime > 600) {
                    this.remove();
                }
                else if (!pos.equals(this.prevPos)) {
                    this.prevPos = pos;
                }
            }
            else {
                BlockState state = this.world.getBlockState(pos);
                if (state.getBlock() != Blocks.MOVING_PISTON) {
                    BlockPeat.placeLayersOn(this.world, pos, this.layers);
                    if (state.getBlock() instanceof IReplaceable && state.getBlock() != this.getBlockState().getBlock()) {
                        this.entityDropItem(((IReplaceable) state.getBlock()).getDrops(this.world, pos, state));
                    }
                    this.remove();
                }
            }
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("Time", this.fallTime);
        compound.putByte("Layers", (byte) this.layers);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeByte(this.layers);
    }
}
