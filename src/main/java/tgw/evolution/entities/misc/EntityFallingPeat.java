package tgw.evolution.entities.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.BlockPeat;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.util.physics.Physics;
import tgw.evolution.util.physics.SI;

public class EntityFallingPeat extends Entity implements IEntityAdditionalSpawnData, IEntityPatch<EntityFallingPeat> {

    public static final EntityDimensions[] DIMENSIONS = {EntityDimensions.scalable(1.0f, 0.25f),
                                                         EntityDimensions.scalable(1.0f, 0.5f),
                                                         EntityDimensions.scalable(1.0f, 0.75f),
                                                         EntityDimensions.scalable(1.0f, 1.0f)};
    public int fallTime;
    private boolean isSizeCorrect;
    private int layers;
    private int mass = 289;
    @Nullable
    private BlockPos prevPos;

    public EntityFallingPeat(EntityType<EntityFallingPeat> type, Level level) {
        super(type, level);
    }

    public EntityFallingPeat(Level level, double x, double y, double z, int layers) {
        super(EvolutionEntities.FALLING_PEAT.get(), level);
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.layers = layers;
        this.mass = 289 * this.layers;
        this.prevPos = this.blockPosition();
    }

    public EntityFallingPeat(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(EvolutionEntities.FALLING_PEAT.get(), level);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Time", this.fallTime);
        tag.putByte("Layers", (byte) this.layers);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public double getBaseMass() {
        return this.mass;
    }

    public BlockState getBlockState() {
        return EvolutionBlocks.PEAT.get().defaultBlockState().setValue(EvolutionBStates.LAYERS_1_4, this.layers);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return DIMENSIONS[this.layers - 1];
    }

    @Override
    public float getFrictionModifier() {
        return 2.0f;
    }

    @Override
    public @Nullable HitboxEntity<EntityFallingPeat> getHitboxes() {
        return null;
    }

    //TODO
//    @Override
//    public boolean func_241845_aY() {
//        return this.isAlive();
//    }

    @Override
    public double getLegSlowdown() {
        return 0;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public double getVolume() {
        return this.layers * SI.CUBIC_CENTIMETER / 4;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

//    @Override
//    protected boolean isMovementNoisy() {
//        return false;
//    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.fallTime = tag.getInt("Time");
        if (tag.contains("Layers", Tag.TAG_BYTE)) {
            this.layers = tag.getByte("Layers");
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.layers = buffer.readByte();
    }

    @Override
    public void tick() {
        if (!this.isSizeCorrect) {
            this.refreshDimensions();
            this.isSizeCorrect = true;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        ++this.fallTime;
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        try (Physics physics = Physics.getInstance(this, this.isInWater() ? Fluid.WATER : this.isInLava() ? Fluid.LAVA : Fluid.AIR)) {
            double accY = 0;
            if (!this.isNoGravity()) {
                accY += physics.calcAccGravity();
            }
            if (!this.isOnGround()) {
                accY += physics.calcForceBuoyancy(this) / this.mass;
            }
            //Pseudo-forces
            double accCoriolisX = physics.calcAccCoriolisX();
            double accCoriolisY = physics.calcAccCoriolisY();
            double accCoriolisZ = physics.calcAccCoriolisZ();
            double accCentrifugalY = physics.calcAccCentrifugalY();
            double accCentrifugalZ = physics.calcAccCentrifugalZ();
            //Dissipative Forces
            double dissipativeX = 0;
            double dissipativeZ = 0;
            if (this.isOnGround() && (motionX != 0 || motionZ != 0)) {
                double norm = Mth.fastInvSqrt(motionX * motionX + motionZ * motionZ);
                double frictionAcc = physics.calcAccNormal() * physics.calcKineticFrictionCoef(this);
                double frictionX = motionX * norm * frictionAcc;
                double frictionZ = motionZ * norm * frictionAcc;
                dissipativeX = frictionX;
                if (Math.abs(dissipativeX) > Math.abs(motionX)) {
                    dissipativeX = motionX;
                }
                dissipativeZ = frictionZ;
                if (Math.abs(dissipativeZ) > Math.abs(motionZ)) {
                    dissipativeZ = motionZ;
                }
            }
            //Drag
            //TODO wind speed
//            double windVelX = 0;
//            double windVelY = 0;
//            double windVelZ = 0;
//            double dragX = physics.calcForceDragX(windVelX) / this.mass;
//            double dragY = physics.calcForceDragY(windVelY) / this.mass;
//            double dragZ = physics.calcForceDragZ(windVelZ) / this.mass;
//            double maxDrag = Math.abs(windVelX - motionX);
//            if (Math.abs(dragX) > maxDrag) {
//                dragX = Math.signum(dragX) * maxDrag;
//            }
//            maxDrag = Math.abs(windVelY - motionY);
//            if (Math.abs(dragY) > maxDrag) {
//                dragY = Math.signum(dragY) * maxDrag;
//            }
//            maxDrag = Math.abs(windVelZ - motionZ);
//            if (Math.abs(dragZ) > maxDrag) {
//                dragZ = Math.signum(dragZ) * maxDrag;
//            }
            //Update Motion
            motionX += -dissipativeX + /*dragX +*/ accCoriolisX;
            motionY += accY + /*dragY +*/ accCoriolisY + accCentrifugalY;
            motionZ += -dissipativeZ + /*dragZ +*/ accCoriolisZ + accCentrifugalZ;
        }
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
        BlockPos pos = this.blockPosition();
        if (!this.level.isClientSide) {
            if (!this.onGround) {
                if (this.fallTime > 100 && (pos.getY() < 1 || pos.getY() > 256) || this.fallTime > 600) {
                    this.discard();
                }
                else if (!pos.equals(this.prevPos)) {
                    this.prevPos = pos;
                }
            }
            else {
                BlockState state = this.level.getBlockState(pos);
                if (state.getBlock() != Blocks.MOVING_PISTON) {
                    BlockPeat.placeLayersOn(this.level, pos, this.layers);
//                    if (state.getBlock() instanceof IReplaceable && state.getBlock() != this.getBlockState().getBlock()) {
//                        for (ItemStack stack : ((IReplaceable) state.getBlock()).getDrops(this.level, pos, state)) {
//                            this.spawnAtLocation(stack);
//                        }
//                    }
                    this.discard();
                }
            }
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeByte(this.layers);
    }
}
