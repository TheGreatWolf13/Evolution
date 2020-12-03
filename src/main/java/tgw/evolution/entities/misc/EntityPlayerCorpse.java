package tgw.evolution.entities.misc;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.NBTTypes;
import tgw.evolution.util.Time;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityPlayerCorpse extends Entity implements IEntityAdditionalSpawnData {

    public static final DataParameter<Boolean> SKELETON = EntityDataManager.createKey(EntityPlayerCorpse.class, DataSerializers.BOOLEAN);
    private static PlayerProfileCache profileCache;
    private static MinecraftSessionService sessionService;
    private int deathTimer;
    private String playerName;
    private GameProfile playerProfile;
    private UUID playerUUID;
    private boolean skeleton;

    public EntityPlayerCorpse(PlayerEntity player) {
        this(EvolutionEntities.PLAYER_CORPSE.get(), player.world);
        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;
        this.setPosition(x, y, z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.playerUUID = player.getUniqueID();
        this.playerName = player.getScoreboardName();
        this.rotationYaw = player.rotationYaw;
        this.setPlayerProfile(player.getGameProfile());
    }

    public EntityPlayerCorpse(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.preventEntitySpawning = true;
        this.setInvulnerable(true);
    }

    public EntityPlayerCorpse(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawnMessage, World world) {
        this(EvolutionEntities.PLAYER_CORPSE.get(), world);
    }

    public static void setProfileCache(PlayerProfileCache profileCacheIn) {
        profileCache = profileCacheIn;
    }

    public static void setSessionService(MinecraftSessionService sessionServiceIn) {
        sessionService = sessionServiceIn;
    }

    public static GameProfile updateGameProfile(GameProfile input) {
        if (input != null && !StringUtils.isNullOrEmpty(input.getName())) {
            if (input.isComplete() && input.getProperties().containsKey("textures")) {
                return input;
            }
            if (profileCache != null && sessionService != null) {
                GameProfile gameprofile = profileCache.getGameProfileForUsername(input.getName());
                if (gameprofile == null) {
                    return input;
                }
                Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), null);
                if (property == null) {
                    return sessionService.fillProfileProperties(gameprofile, true);
                }
                return gameprofile;
            }
            return input;
        }
        return input;
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
    public boolean canBePushed() {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ITextComponent getName() {
        if (this.isSkeleton()) {
            return new TranslationTextComponent("entity.evolution.player_skeleton", this.playerName);
        }
        return new TranslationTextComponent("entity.evolution.player_corpse", this.playerName);
    }

    @Nullable
    public GameProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public void setPlayerProfile(@Nullable GameProfile gameProfile) {
        this.playerProfile = gameProfile;
        this.updatePlayerProfile();
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public boolean isSkeleton() {
        return this.dataManager.get(SKELETON);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.deathTimer = compound.getInt("DeathTimer");
        this.skeleton = compound.getBoolean("Skeleton");
        this.dataManager.set(SKELETON, this.skeleton);
        if (compound.hasUniqueId("PlayerUUID")) {
            this.playerUUID = compound.getUniqueId("PlayerUUID");
        }
        if (compound.contains("PlayerName", NBTTypes.STRING.getId())) {
            this.playerName = compound.getString("PlayerName");
        }
        this.setPlayerProfile(new GameProfile(this.playerUUID, this.playerName));
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.playerUUID = buffer.readUniqueId();
        this.playerName = buffer.readString();
        this.setPlayerProfile(new GameProfile(this.playerUUID, this.playerName));
    }

    @Override
    protected void registerData() {
        this.dataManager.register(SKELETON, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isRemote) {
            if (!this.skeleton) {
                this.deathTimer++;
            }
            if (this.deathTimer >= Time.MONTH_IN_TICKS) {
                this.dataManager.set(SKELETON, true);
                this.skeleton = true;
            }
        }
        Vec3d motion = this.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        double gravity = 0;
        if (!this.hasNoGravity()) {
            gravity = Gravity.gravity(this.world.dimension);
        }
        else {
            motionY = 0;
        }
        BlockPos blockBelow = new BlockPos(this.posX, this.getBoundingBox().minY - 1, this.posZ);
        float slipperiness = this.world.getBlockState(blockBelow).getSlipperiness(this.world, blockBelow, this);
        if (Float.compare(slipperiness, 0.6F) < 0.01F) {
            slipperiness = 0.15F;
        }
        float frictionCoef = this.onGround ? 1.0F - slipperiness : 0.0F;
        double frictionX = 0;
        double frictionZ = 0;
        if (this.onGround) {
            double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (norm != 0) {
                double frictionAcc = frictionCoef * gravity;
                frictionX = motionX / norm * frictionAcc;
                frictionZ = motionZ / norm * frictionAcc;
            }
            if (Math.abs(motionX) < Math.abs(frictionX)) {
                frictionX = motionX;
            }
            if (Math.abs(motionZ) < Math.abs(frictionZ)) {
                frictionZ = motionZ;
            }
        }
        double horizontalDrag = this.isInWater() ? Gravity.horizontalWaterDrag(this) / 70 : Gravity.horizontalDrag(this) / 70;
        double verticalDrag = this.isInWater() ? Gravity.verticalWaterDrag(this) / 70 : Gravity.verticalDrag(this) / 70;
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
        motionX += -frictionX - dragX;
        motionY += -gravity - dragY;
        motionZ += -frictionZ - dragZ;
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
    }

    private void updatePlayerProfile() {
        this.playerProfile = updateGameProfile(this.playerProfile);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("Skeleton", this.skeleton);
        if (this.playerUUID != null) {
            compound.putUniqueId("PlayerUUID", this.playerUUID);
        }
        if (this.playerName != null) {
            compound.putString("PlayerName", this.playerName);
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeUniqueId(this.playerUUID);
        buffer.writeString(this.playerName);
    }
}
