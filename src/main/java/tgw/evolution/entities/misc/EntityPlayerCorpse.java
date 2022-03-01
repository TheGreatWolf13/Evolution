package tgw.evolution.entities.misc;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import joptsimple.internal.Strings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.entities.EntityPlayerDummy;
import tgw.evolution.entities.EntitySkeletonDummy;
import tgw.evolution.entities.IEntityProperties;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.inventory.corpse.ContainerCorpseProvider;
import tgw.evolution.inventory.extendedinventory.IExtendedInventory;
import tgw.evolution.util.EvolutionDataSerializers;
import tgw.evolution.util.constants.NBTHelper;
import tgw.evolution.util.constants.NBTTypes;
import tgw.evolution.util.earth.Gravity;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.reflection.StaticFieldHandler;
import tgw.evolution.util.time.Time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class EntityPlayerCorpse extends Entity implements IEntityAdditionalSpawnData, IEntityProperties, IEvolutionEntity<EntityPlayerCorpse> {

    public static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(EntityPlayerCorpse.class, EntityDataSerializers.BOOLEAN);
    private static final StaticFieldHandler<Player, EntityDataAccessor<Byte>> DATA_PLAYER_MODE_CUSTOMIZATION = new StaticFieldHandler<>(Player.class,
                                                                                                                                        "f_36089_");
    private static final EntityDataAccessor<NonNullList<ItemStack>> EQUIPMENT = SynchedEntityData.defineId(EntityPlayerCorpse.class,
                                                                                                           EvolutionDataSerializers.ITEM_LIST);
    private static GameProfileCache profileCache;
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
    private final IntSet playersInteracting = new IntOpenHashSet();
    private Component deathMessage;
    private int deathTimer;
    private long gameDeathTime;
    private boolean isSkeleton;
    private long lastTick;
    private byte model;
    private EntityPlayerDummy player;
    private String playerName;
    private GameProfile playerProfile;
    private UUID playerUUID;
    private int selected;
    private EntitySkeletonDummy skeleton;
    private long systemDeathTime;

    public EntityPlayerCorpse(Player player) {
        this(EvolutionEntities.PLAYER_CORPSE.get(), player.level);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        this.setPos(x, y, z);
        this.setDeltaMovement(x - player.xOld, y - player.yOld, z - player.zOld);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.playerUUID = player.getUUID();
        this.playerName = player.getScoreboardName();
        this.setYRot(player.getYRot());
        this.setPlayerProfile(player.getGameProfile());
        this.model = player.getEntityData().get(DATA_PLAYER_MODE_CUSTOMIZATION.get());
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            this.model |= 1 << 7;
        }
        NonNullList<ItemStack> equip = NonNullList.withSize(AdditionalSlotType.SLOTS.length + AdditionalSlotType.VALUES.length, ItemStack.EMPTY);
        int i = 0;
        for (EquipmentSlot slot : AdditionalSlotType.SLOTS) {
            equip.set(i++, player.getItemBySlot(slot).copy());
        }
        IExtendedInventory additionalEquip = player.getCapability(CapabilityExtendedInventory.INSTANCE).orElseThrow(IllegalStateException::new);
        for (AdditionalSlotType slot : AdditionalSlotType.VALUES) {
            equip.set(i++, additionalEquip.getStackInSlot(slot.ordinal()).copy());
        }
        this.setEquipment(equip);
        this.selected = player.getInventory().selected;
        this.lastTick = this.level.getGameTime();
        this.gameDeathTime = this.level.getDayTime();
        this.systemDeathTime = System.currentTimeMillis();
        this.deathMessage = player.getCombatTracker().getDeathMessage();
    }

    public EntityPlayerCorpse(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.setInvulnerable(true);
    }

    public EntityPlayerCorpse(@SuppressWarnings("unused") PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(EvolutionEntities.PLAYER_CORPSE.get(), level);
    }

    public static void setProfileCache(GameProfileCache profileCacheIn) {
        profileCache = profileCacheIn;
    }

    public static void setSessionService(MinecraftSessionService sessionServiceIn) {
        sessionService = sessionServiceIn;
    }

    public static GameProfile updateGameProfile(GameProfile input) {
        if (input != null && !Strings.isNullOrEmpty(input.getName())) {
            if (input.isComplete() && input.getProperties().containsKey("textures")) {
                return input;
            }
            if (profileCache != null && sessionService != null) {
                Optional<GameProfile> gameprofile = profileCache.get(input.getName());
                if (gameprofile.isEmpty()) {
                    return input;
                }
                Property property = Iterables.getFirst(gameprofile.get().getProperties().get("textures"), null);
                if (property == null) {
                    return sessionService.fillProfileProperties(gameprofile.get(), true);
                }
                return gameprofile.get();
            }
            return input;
        }
        return input;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Inventory", this.itemHandler.serializeNBT());
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("IsSkeleton", this.isSkeleton);
        compound.putByte("Model", this.model);
        compound.putLong("LastTick", this.lastTick);
        compound.putLong("SystemDeathTime", this.systemDeathTime);
        compound.putLong("GameDeathTime", this.gameDeathTime);
        compound.putString("DeathMessage", Component.Serializer.toJson(this.deathMessage));
        compound.put("Equipment", NBTHelper.writeStackList(this.entityData.get(EQUIPMENT)));
        compound.putByte("Selected", (byte) this.selected);
        if (this.playerUUID != null) {
            compound.putUUID("PlayerUUID", this.playerUUID);
        }
        if (this.playerName != null) {
            compound.putString("PlayerName", this.playerName);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SKELETON, false);
        this.entityData.define(EQUIPMENT, NonNullList.withSize(AdditionalSlotType.SLOTS.length + AdditionalSlotType.VALUES.length, ItemStack.EMPTY));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public double getBaseMass() {
        return 70;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public Component getDeathMessage() {
        return this.deathMessage;
    }

    public NonNullList<ItemStack> getEquipment() {
        return this.entityData.get(EQUIPMENT);
    }

    @Override
    public float getFrictionModifier() {
        return 2.0f;
    }

    public long getGameDeathTime() {
        return this.gameDeathTime;
    }

    @Nullable
    @Override
    public HitboxEntity<EntityPlayerCorpse> getHitbox() {
        return null;
    }

    @Override
    public double getLegSlowdown() {
        return 0;
    }

    @Override
    public Component getName() {
        if (this.isSkeleton()) {
            return new TranslatableComponent("entity.evolution.player_skeleton", this.playerName);
        }
        return new TranslatableComponent("entity.evolution.player_corpse", this.playerName);
    }

    @Nullable
    public EntityPlayerDummy getPlayer() {
        if (this.level.isClientSide) {
            if (this.player == null) {
                this.player = new EntityPlayerDummy((ClientLevel) this.level, this.playerProfile, this.getEquipment(), this.model);
            }
            return this.player;
        }
        return null;
    }

    @Nullable
    public GameProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public int getSelected() {
        return this.selected;
    }

    @Nullable
    public EntitySkeletonDummy getSkeleton() {
        if (this.level.isClientSide) {
            if (this.skeleton == null) {
                this.player = null;
                this.skeleton = new EntitySkeletonDummy(this.level,
                                                        this.getEquipment(),
                                                        (this.model & 1 << 7) != 0 ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
            }
            return this.skeleton;
        }
        return null;
    }

    public long getSystemDeathTime() {
        return this.systemDeathTime;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level.isClientSide) {
            NetworkHooks.openGui((ServerPlayer) player, new ContainerCorpseProvider(this), packet -> packet.writeInt(this.getId()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public boolean isSkeleton() {
        return this.entityData.get(SKELETON);
    }

    public void onClose(Player player) {
        this.playersInteracting.remove(player.getId());
    }

    public void onOpen(Player player) {
        this.playersInteracting.add(player.getId());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (data == EQUIPMENT) {
            if (this.level.isClientSide) {
                NonNullList<ItemStack> equipment = this.entityData.get(EQUIPMENT);
                if (this.player != null) {
                    for (EquipmentSlot type : AdditionalSlotType.SLOTS) {
                        this.player.setItemSlot(type, equipment.get(type.ordinal()));
                    }
                }
                if (this.skeleton != null) {
                    for (EquipmentSlot type : AdditionalSlotType.SLOTS) {
                        this.skeleton.setItemSlot(type, equipment.get(type.ordinal()));
                    }
                }
            }
        }
        super.onSyncedDataUpdated(data);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.itemHandler.deserializeNBT(compound.getCompound("Inventory"));
        this.deathTimer = compound.getInt("DeathTimer");
        this.isSkeleton = compound.getBoolean("IsSkeleton");
        this.entityData.set(SKELETON, this.isSkeleton);
        this.model = compound.getByte("Model");
        this.lastTick = compound.getLong("LastTick");
        this.entityData.set(EQUIPMENT, NBTHelper.readStackList(compound.getCompound("Equipment")));
        this.systemDeathTime = compound.getLong("SystemDeathTime");
        this.gameDeathTime = compound.getLong("GameDeathTime");
        this.deathMessage = Component.Serializer.fromJson(compound.getString("DeathMessage"));
        this.selected = compound.getByte("Selected");
        if (compound.hasUUID("PlayerUUID")) {
            this.playerUUID = compound.getUUID("PlayerUUID");
        }
        if (compound.contains("PlayerName", NBTTypes.STRING)) {
            this.playerName = compound.getString("PlayerName");
        }
        this.setPlayerProfile(new GameProfile(this.playerUUID, this.playerName));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.playerName = buffer.readUtf();
        this.model = buffer.readByte();
        this.selected = buffer.readByte();
        this.systemDeathTime = buffer.readLong();
        this.gameDeathTime = buffer.readLong();
        this.deathMessage = buffer.readComponent();
        this.setPlayerProfile(new GameProfile(this.playerUUID, this.playerName));
    }

    public void setEquipment(NonNullList<ItemStack> equipment) {
        this.entityData.set(EQUIPMENT, equipment);
    }

    public void setInventory(Player player) {
        NonNullList<ItemStack> inv = player.getInventory().armor;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = inv.get(3 - i);
            this.itemHandler.setStackInSlot(i, stack);
            inv.set(3 - i, ItemStack.EMPTY);
        }
        inv = player.getInventory().offhand;
        for (int i = 0; i < 1; i++) {
            ItemStack stack = inv.get(0);
            this.itemHandler.setStackInSlot(10, stack);
            inv.set(0, ItemStack.EMPTY);
        }
        inv = player.getInventory().items;
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
        IExtendedInventory handler = player.getCapability(CapabilityExtendedInventory.INSTANCE).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            handler.setStackInSlot(i, ItemStack.EMPTY);
            switch (i) {
                case EvolutionResources.HAT -> this.itemHandler.setStackInSlot(7, stack);
                case EvolutionResources.BODY -> this.itemHandler.setStackInSlot(6, stack);
                case EvolutionResources.LEGS -> this.itemHandler.setStackInSlot(5, stack);
                case EvolutionResources.FEET -> this.itemHandler.setStackInSlot(4, stack);
                case EvolutionResources.MASK -> this.itemHandler.setStackInSlot(8, stack);
                case EvolutionResources.CLOAK -> this.itemHandler.setStackInSlot(9, stack);
                case EvolutionResources.BACK -> this.itemHandler.setStackInSlot(11, stack);
                case EvolutionResources.TACTICAL -> this.itemHandler.setStackInSlot(12, stack);
            }
        }
    }

    public void setPlayerProfile(@Nullable GameProfile gameProfile) {
        this.playerProfile = gameProfile;
        this.updatePlayerProfile();
    }

    public void setSlot(EquipmentSlot slot, ItemStack stack) {
        NonNullList<ItemStack> equip = this.entityData.get(EQUIPMENT);
        NonNullList<ItemStack> newEquip = NonNullList.of(ItemStack.EMPTY, equip.toArray(new ItemStack[0]));
        newEquip.set(slot.ordinal(), stack);
        this.entityData.set(EQUIPMENT, newEquip);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            long currentTick = this.level.getGameTime();
            long passedTicks = currentTick - this.lastTick;
            if (!this.isSkeleton) {
                this.deathTimer += passedTicks;
            }
            if (this.deathTimer >= 7 * Time.DAY_IN_TICKS) {
                this.entityData.set(SKELETON, true);
                this.isSkeleton = true;
            }
            this.lastTick = currentTick;
        }
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        double gravity = 0;
        if (!this.isNoGravity()) {
            gravity = Gravity.gravity(this.level.dimensionType());
        }
        else {
            motionY = 0;
        }
        BlockPos blockBelow = new BlockPos(this.getX(), this.getBoundingBox().minY - 1, this.getZ());
        float slipperiness = this.level.getBlockState(blockBelow).getFriction(this.level, blockBelow, this);
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
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
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
        this.discard();
    }

    private void updatePlayerProfile() {
        this.playerProfile = updateGameProfile(this.playerProfile);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerUUID);
        buffer.writeUtf(this.playerName);
        buffer.writeByte(this.model);
        buffer.writeByte(this.selected);
        buffer.writeLong(this.systemDeathTime);
        buffer.writeLong(this.gameDeathTime);
        buffer.writeComponent(this.deathMessage);
    }
}
