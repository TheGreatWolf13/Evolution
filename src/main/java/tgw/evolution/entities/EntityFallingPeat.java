package tgw.evolution.entities;

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
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.NBTTypes;

import javax.annotation.Nullable;

public class EntityFallingPeat extends Entity implements IEntityAdditionalSpawnData {

    public static final EntitySize SIZE_1 = EntitySize.flexible(1f, 0.25f);
    public static final EntitySize SIZE_2 = EntitySize.flexible(1f, 0.5f);
    public static final EntitySize SIZE_3 = EntitySize.flexible(1f, 0.75f);
    public static final EntitySize SIZE_4 = EntitySize.flexible(1f, 1f);
    public static final EntitySize[] SIZES = {SIZE_1, SIZE_2, SIZE_3, SIZE_4};
    public int fallTime;
    private int layers;
    private BlockPos prevPos;
    private double verticalDrag;
    private double horizontalDrag;
    private boolean isSizeCorrect;
    private double gravity;

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
        this.prevPos = new BlockPos(this);
    }

    @Override
    protected void registerData() {
    }

    @Override
    public void tick() {
        if (!this.isSizeCorrect) {
            this.recalculateSize();
//            float width = this.getSize(null).width;
//            float height = this.getSize(null).height;
            this.verticalDrag = 1/*Gravity.verticalDrag(this.world.getDimension(), width)*/;
            this.horizontalDrag = 1/*Gravity.horizontalDrag(this.world.getDimension(), width, height)*/;
            this.gravity = -Gravity.gravity(this.world.getDimension());
            this.isSizeCorrect = true;
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.fallTime;
        if (!this.hasNoGravity()) {
            this.setMotion(this.getMotion().add(0, this.gravity, 0));
        }
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
                        this.entityDropItem(((IReplaceable) state.getBlock()).getDrops(state));
                    }
                    this.remove();
                    return;
                }
            }
        }
        this.setMotion(this.getMotion().mul(this.horizontalDrag, this.verticalDrag, this.horizontalDrag));
    }

    public BlockState getBlockState() {
        return EvolutionBlocks.PEAT.get().getDefaultState().with(BlockPeat.LAYERS, this.layers);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.isAlive() ? this.getBoundingBox() : null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.fallTime = compound.getInt("Time");
        if (compound.contains("Layers", NBTTypes.BYTE.getId())) {
            this.layers = compound.getByte("Layers");
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("Time", this.fallTime);
        compound.putByte("Layers", (byte) this.layers);
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return SIZES[this.layers - 1];
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeByte(this.layers);
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.layers = buffer.readByte();
    }
}
