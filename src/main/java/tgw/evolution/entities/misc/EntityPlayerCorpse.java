package tgw.evolution.entities.misc;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.corpse.ContainerCorpseProvider;
import tgw.evolution.inventory.extendedinventory.IExtendedItemHandler;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.NBTTypes;
import tgw.evolution.util.Time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityPlayerCorpse extends Entity implements IEntityAdditionalSpawnData {

    public static final DataParameter<Boolean> SKELETON = EntityDataManager.createKey(EntityPlayerCorpse.class, DataSerializers.BOOLEAN);
    private static PlayerProfileCache profileCache;
    private static MinecraftSessionService sessionService;
    private final ItemStackHandler itemHandler = new ItemStackHandler(49) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return false;
        }
    };
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> this.itemHandler);
    private final Set<Integer> playersInteracting = new HashSet<>();
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
        return !this.removed;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.handler.cast();
        }
        return super.getCapability(cap, side);
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

    public void onClose(PlayerEntity player) {
        this.playersInteracting.remove(player.getEntityId());
    }

    public void onOpen(PlayerEntity player) {
        this.playersInteracting.add(player.getEntityId());
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (!this.world.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerCorpseProvider(this), packet -> packet.writeInt(this.getEntityId()));
        }
        return true;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.itemHandler.deserializeNBT(compound.getCompound("inv"));
        this.deathTimer = compound.getInt("DeathTimer");
        this.skeleton = compound.getBoolean("Skeleton");
        this.dataManager.set(SKELETON, this.skeleton);
        if (compound.hasUniqueId("PlayerUUID")) {
            this.playerUUID = compound.getUniqueId("PlayerUUID");
        }
        if (compound.contains("PlayerName", NBTTypes.STRING)) {
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

    public void setInventory(PlayerEntity player) {
        NonNullList<ItemStack> inv = player.inventory.armorInventory;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = inv.get(3 - i);
            this.itemHandler.setStackInSlot(i, stack);
            inv.set(3 - i, ItemStack.EMPTY);
        }
        inv = player.inventory.offHandInventory;
        for (int i = 0; i < 1; i++) {
            ItemStack stack = inv.get(0);
            this.itemHandler.setStackInSlot(10, stack);
            inv.set(0, ItemStack.EMPTY);
        }
        inv = player.inventory.mainInventory;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.get(i);
            this.itemHandler.setStackInSlot(40 + i, stack);
            inv.set(i, ItemStack.EMPTY);
        }
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.get(i);
            this.itemHandler.setStackInSlot(4 + i, stack);
            inv.set(i, ItemStack.EMPTY);
        }
        IExtendedItemHandler handler = player.getCapability(CapabilityExtendedInventory.INSTANCE).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            handler.setStackInSlot(i, ItemStack.EMPTY);
            switch (i) {
                case EvolutionResources.HAT:
                    this.itemHandler.setStackInSlot(7, stack);
                    break;
                case EvolutionResources.BODY:
                    this.itemHandler.setStackInSlot(6, stack);
                    break;
                case EvolutionResources.LEGS:
                    this.itemHandler.setStackInSlot(5, stack);
                    break;
                case EvolutionResources.FEET:
                    this.itemHandler.setStackInSlot(4, stack);
                    break;
                case EvolutionResources.MASK:
                    this.itemHandler.setStackInSlot(8, stack);
                    break;
                case EvolutionResources.CLOAK:
                    this.itemHandler.setStackInSlot(9, stack);
                    break;
                case EvolutionResources.BACK:
                    this.itemHandler.setStackInSlot(11, stack);
                    break;
                case EvolutionResources.TACTICAL:
                    this.itemHandler.setStackInSlot(12, stack);
                    break;
            }
        }
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

    public void tryDespawn() {
        if (!this.playersInteracting.isEmpty()) {
            return;
        }
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            if (!this.itemHandler.getStackInSlot(i).isEmpty()) {
                return;
            }
        }
        this.remove();
    }

    private void updatePlayerProfile() {
        this.playerProfile = updateGameProfile(this.playerProfile);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.put("inv", this.itemHandler.serializeNBT());
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
