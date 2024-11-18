package tgw.evolution.entities.misc;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import joptsimple.internal.Strings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.player.CapabilityInventory;
import tgw.evolution.entities.EntityPlayerDummy;
import tgw.evolution.entities.EntitySkeletonDummy;
import tgw.evolution.entities.EntityUtils;
import tgw.evolution.entities.IEntitySpawnData;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.inventory.BasicContainer;
import tgw.evolution.inventory.ContainerChecker;
import tgw.evolution.inventory.IContainerCheckable;
import tgw.evolution.inventory.corpse.ContainerCorpse;
import tgw.evolution.network.PacketSCCustomEntity;
import tgw.evolution.patches.PatchSynchedEntityData;
import tgw.evolution.util.EvolutionDataSerializers;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.util.physics.Physics;
import tgw.evolution.util.physics.SI;
import tgw.evolution.util.time.Time;

import java.util.Optional;
import java.util.UUID;

public class EntityPlayerCorpse extends Entity implements IContainerCheckable, IEntitySpawnData, MenuProvider, Container {

    public static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(EntityPlayerCorpse.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<NonNullList<ItemStack>> EQUIPMENT = SynchedEntityData.defineId(EntityPlayerCorpse.class,
                                                                                                           EvolutionDataSerializers.ITEM_LIST);
    private static @Nullable GameProfileCache profileCache;
    private static @Nullable MinecraftSessionService sessionService;
    private final ContainerChecker<EntityPlayerCorpse> containerChecker;
    protected Component deathMessage = EvolutionTexts.EMPTY;
    private int deathTimer;
    protected long gameDeathTime;
    private final BasicContainer inventory;
    private boolean isSkeleton;
    private long lastTick;
    protected byte model;
    private @Nullable EntityPlayerDummy player;
    protected String playerName = "";
    private GameProfile playerProfile = EntityUtils.EMPTY_PROFILE;
    protected UUID playerUUID = EntityUtils.UUID_ZERO;
    private byte recheckTime;
    protected int selected;
    private @Nullable EntitySkeletonDummy skeleton;
    protected long systemDeathTime;

    {
        this.containerChecker = new ContainerChecker<>() {

            @Override
            protected boolean isOwnContainer(Player player) {
                if (!(player.containerMenu instanceof ContainerCorpse container)) {
                    return false;
                }
                Container corpse = container.getCorpse();
                return EntityPlayerCorpse.this == corpse;
            }

            @Override
            protected void onClose(Level level, BlockPos pos, EntityPlayerCorpse obj) {
                EntityPlayerCorpse.this.tryDespawn();
            }

            @Override
            protected void onOpen(Level level, BlockPos pos, EntityPlayerCorpse obj) {
            }

            @Override
            protected void openerCountChanged(Level level, BlockPos pos, EntityPlayerCorpse obj, int count, int newCount) {
            }

            @Override
            public void scheduleRecheck(Level level, BlockPos pos, EntityPlayerCorpse state) {
                EntityPlayerCorpse.this.recheckTime = 5;
            }
        };
    }

    public EntityPlayerCorpse(ServerPlayer player) {
        this(EvolutionEntities.PLAYER_CORPSE, player.level);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        this.setPos(x, y, z);
        this.setDeltaMovement(player.getMotionX(), player.getMotionY(), player.getMotionZ());
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.playerUUID = player.getUUID();
        this.playerName = player.getScoreboardName();
        this.setYRot(player.getYRot());
        this.setPlayerProfile(player.getGameProfile());
        this.model = player.getEntityData().get(Player.DATA_PLAYER_MODE_CUSTOMISATION);
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            this.model |= (byte) (1 << 7);
        }
        NonNullList<ItemStack> equip = NonNullList.withSize(AdditionalSlotType.SLOTS.length + AdditionalSlotType.VALUES.length, ItemStack.EMPTY);
        int i = 0;
        for (EquipmentSlot slot : AdditionalSlotType.SLOTS) {
            equip.set(i++, player.getItemBySlot(slot).copy());
        }
        CapabilityInventory additionalEquip = player.getExtraInventory();
        for (AdditionalSlotType slot : AdditionalSlotType.VALUES) {
            equip.set(i++, additionalEquip.getItem(slot.ordinal()).copy());
        }
        this.setEquipment(equip);
        this.selected = player.getInventory().selected;
        this.lastTick = this.level.getGameTime();
        this.gameDeathTime = this.level.getDayTime();
        this.systemDeathTime = System.currentTimeMillis();
        this.deathMessage = player.getCombatTracker().getDeathMessage();
    }

    public EntityPlayerCorpse(EntityType<? extends EntityPlayerCorpse> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.setInvulnerable(true);
        this.inventory = new BasicContainer(AdditionalSlotType.VALUES.length + 36 + 4 + 1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(int slot, Player player, ItemStack stackTaken, ItemStack newStack) {
                //TODO implementation

            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
    }

    public static void setProfileCache(GameProfileCache profileCacheIn) {
        profileCache = profileCacheIn;
    }

    public static void setSessionService(MinecraftSessionService sessionServiceIn) {
        sessionService = sessionServiceIn;
    }

    public static GameProfile updateGameProfile(@Nullable GameProfile input) {
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
        return EntityUtils.EMPTY_PROFILE;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Inventory", this.inventory.serializeNBT());
        if (this.recheckTime != 0) {
            compound.putByte("RecheckTime", this.recheckTime);
        }
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("IsSkeleton", this.isSkeleton);
        compound.putByte("Model", this.model);
        compound.putLong("LastTick", this.lastTick);
        compound.putLong("SystemDeathTime", this.systemDeathTime);
        compound.putLong("GameDeathTime", this.gameDeathTime);
        compound.putString("DeathMessage", Component.Serializer.toJson(this.deathMessage));
        compound.put("Equipment", NBTHelper.writeStackList(this.entityData.get(EQUIPMENT)));
        compound.putByte("Selected", (byte) this.selected);
        if (this.playerUUID != EntityUtils.UUID_ZERO) {
            compound.putUUID("PlayerUUID", this.playerUUID);
        }
        if (!this.playerName.isBlank()) {
            compound.putString("PlayerName", this.playerName);
        }
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ContainerCorpse(i, inventory, this.inventory);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SKELETON, false);
        this.entityData.define(EQUIPMENT, NonNullList.withSize(AdditionalSlotType.SLOTS.length + AdditionalSlotType.VALUES.length, ItemStack.EMPTY));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new PacketSCCustomEntity<>(this);
    }

    @Override
    public double getBaseMass() {
        return 70;
    }

    @Override
    public int getContainerSize() {
        return this.inventory.getContainerSize();
    }

    public Component getDeathMessage() {
        return this.deathMessage;
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
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

    @Override
    public @Nullable HitboxEntity<? extends EntityPlayerCorpse> getHitboxes() {
        return null;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.getItem(slot);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public Component getName() {
        if (this.isSkeleton()) {
            return new TranslatableComponent("entity.evolution.player_skeleton", this.playerName);
        }
        return new TranslatableComponent("entity.evolution.player_corpse", this.playerName);
    }

    public @Nullable EntityPlayerDummy getPlayer() {
        if (this.level.isClientSide) {
            if (this.player == null) {
                this.player = new EntityPlayerDummy((ClientLevel) this.level, this.playerProfile, this.getEquipment(), this.model);
            }
            return this.player;
        }
        return null;
    }

    public @Nullable GameProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public int getSelected() {
        return this.selected;
    }

    public @Nullable EntitySkeletonDummy getSkeleton() {
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

    @Override
    public EntityData getSpawnData() {
        return new PlayerCorpseData(this);
    }

    public long getSystemDeathTime() {
        return this.systemDeathTime;
    }

    @Override
    public double getVolume() {
        return 60_000 * SI.CUBIC_CENTIMETER;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level.isClientSide) {
            player.openMenu(this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
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

    @Override
    public void onClose(Player player) {
        this.containerChecker.decrementOpeners(player, this.level, this.blockPosition(), this);
    }

    @Override
    public void onOpen(Player player) {
        this.containerChecker.incrementOpeners(player, this.level, this.blockPosition(), this);
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
        this.inventory.deserializeNBT(compound.getCompound("Inventory"));
        this.recheckTime = compound.getByte("RecheckTime");
        this.deathTimer = compound.getInt("DeathTimer");
        this.isSkeleton = compound.getBoolean("IsSkeleton");
        this.entityData.set(SKELETON, this.isSkeleton);
        this.model = compound.getByte("Model");
        this.lastTick = compound.getLong("LastTick");
        this.entityData.set(EQUIPMENT, NBTHelper.readStackList(compound.getCompound("Equipment")));
        this.systemDeathTime = compound.getLong("SystemDeathTime");
        this.gameDeathTime = compound.getLong("GameDeathTime");
        MutableComponent deathMessage = Component.Serializer.fromJson(compound.getString("DeathMessage"));
        this.deathMessage = deathMessage == null ? EvolutionTexts.EMPTY : deathMessage;
        this.selected = compound.getByte("Selected");
        if (compound.hasUUID("PlayerUUID")) {
            this.playerUUID = compound.getUUID("PlayerUUID");
        }
        if (compound.contains("PlayerName", Tag.TAG_STRING)) {
            this.playerName = compound.getString("PlayerName");
        }
        this.setPlayerProfile(new GameProfile(this.playerUUID, this.playerName));
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.inventory.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.inventory.removeItemNoUpdate(slot);
    }

    @Override
    public void setChanged() {
        //TODO implementation
    }

    public void setEquipment(NonNullList<ItemStack> equipment) {
        this.entityData.set(EQUIPMENT, equipment);
    }

    public void setInventory(ServerPlayer player) {
        NonNullList<ItemStack> inv = player.getInventory().armor;
        int slot = 0;
        //Armour
        for (int i = 0; i < 4; ++i) {
            ItemStack stack = inv.get(3 - i);
            this.inventory.setItem(slot++, stack);
            inv.set(3 - i, ItemStack.EMPTY);
        }
        //Extra Inventory
        CapabilityInventory extraInventory = player.getExtraInventory();
        for (int i = 0, len = extraInventory.getContainerSize(); i < len; ++i) {
            ItemStack stack = extraInventory.getItem(i);
            extraInventory.setItem(i, ItemStack.EMPTY);
            this.inventory.setItem(slot++, stack);
        }
        //Offhand
        inv = player.getInventory().offhand;
        ItemStack stack = inv.get(0);
        this.inventory.setItem(slot++, stack);
        inv.set(0, ItemStack.EMPTY);
        //Main Inventory
        inv = player.getInventory().items;
        for (int i = 9; i < 36; i++) {
            stack = inv.get(i);
            this.inventory.setItem(slot++, stack);
            inv.set(i, ItemStack.EMPTY);
        }
        //Hotbar
        for (int i = 0; i < 9; i++) {
            stack = inv.get(i);
            this.inventory.setItem(slot++, stack);
            inv.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.setItem(slot, stack);
    }

    public void setPlayerProfile(GameProfile gameProfile) {
        this.playerProfile = gameProfile;
        this.updatePlayerProfile();
    }

    public void setSlot(EquipmentSlot slot, ItemStack stack) {
        NonNullList<ItemStack> equip = this.entityData.get(EQUIPMENT);
        equip.set(slot.ordinal(), stack);
        ((PatchSynchedEntityData) this.entityData).forceDirty(EQUIPMENT);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.isRemoved()) {
            return false;
        }
        return player.distanceToSqr(this) <= 8 * 8;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            long currentTick = this.level.getGameTime();
            long passedTicks = currentTick - this.lastTick;
            if (!this.isSkeleton) {
                this.deathTimer += (int) passedTicks;
            }
            if (this.deathTimer >= 7 * Time.TICKS_PER_DAY) {
                this.entityData.set(SKELETON, true);
                this.isSkeleton = true;
            }
            this.lastTick = currentTick;
            if (this.recheckTime > 0) {
                if (--this.recheckTime == 0) {
                    this.containerChecker.recheckOpeners(this.level, this.blockPosition(), this);
                }
            }
        }
        Vec3 velocity = this.getDeltaMovement();
        double velX = velocity.x;
        double velY = velocity.y;
        double velZ = velocity.z;
        double mass = this.getBaseMass();
        try (Physics physics = Physics.getInstance(this, this.isInWater() ? Fluid.WATER : this.isInLava() ? Fluid.LAVA : Fluid.AIR)) {
            double accY = 0;
            if (!this.isNoGravity()) {
                accY += physics.calcAccGravity();
            }
            if (!this.isOnGround()) {
                accY += physics.calcForceBuoyancy(this) / mass;
            }
            //Pseudo-forces
            double accCoriolisY = physics.calcAccCoriolisY();
            double accCentrifugalY = physics.calcAccCentrifugalY();
            //Dissipative Forces
            double dissipativeX = 0;
            double dissipativeZ = 0;
            if (this.isOnGround() && (velX != 0 || velZ != 0)) {
                double norm = Mth.fastInvSqrt(velX * velX + velZ * velZ);
                double frictionAcc = physics.calcAccNormal() * physics.calcKineticFrictionCoef(this);
                double frictionX = velX * norm * frictionAcc;
                double frictionZ = velZ * norm * frictionAcc;
                dissipativeX = frictionX;
                if (Math.abs(dissipativeX) > Math.abs(velX)) {
                    dissipativeX = velX;
                }
                dissipativeZ = frictionZ;
                if (Math.abs(dissipativeZ) > Math.abs(velZ)) {
                    dissipativeZ = velZ;
                }
            }
            //Update Motion
            velX -= dissipativeX;
            velY += accY + accCoriolisY + accCentrifugalY;
            velZ -= dissipativeZ;
        }
        this.setDeltaMovement(velX, velY, velZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    public void tryDespawn() {
        if (this.containerChecker.getOpenerCount() != 0) {
            return;
        }
        if (!this.inventory.isEmpty()) {
            return;
        }
        this.discard();
    }

    private void updatePlayerProfile() {
        this.playerProfile = updateGameProfile(this.playerProfile);
    }

    public static class PlayerCorpseData<T extends EntityPlayerCorpse> extends EntityData<T> {

        private final Component deathMessage;
        private final long gameDeathTime;
        private final byte model;
        private final String playerName;
        private final UUID playerUUID;
        private final byte selected;
        private final long systemDeathTime;

        public PlayerCorpseData(T entity) {
            this.playerUUID = entity.playerUUID;
            this.playerName = entity.playerName;
            this.model = entity.model;
            this.gameDeathTime = entity.gameDeathTime;
            this.systemDeathTime = entity.systemDeathTime;
            this.deathMessage = entity.deathMessage;
            this.selected = (byte) entity.selected;
        }

        public PlayerCorpseData(FriendlyByteBuf buf) {
            this.playerUUID = buf.readUUID();
            this.playerName = buf.readUtf();
            this.model = buf.readByte();
            this.selected = buf.readByte();
            this.systemDeathTime = buf.readLong();
            this.gameDeathTime = buf.readLong();
            this.deathMessage = buf.readComponent();
        }

        @Override
        @MustBeInvokedByOverriders
        public void read(T entity) {
            entity.playerUUID = this.playerUUID;
            entity.playerName = this.playerName;
            entity.model = this.model;
            entity.selected = this.selected;
            entity.systemDeathTime = this.systemDeathTime;
            entity.gameDeathTime = this.gameDeathTime;
            entity.deathMessage = this.deathMessage;
            entity.setPlayerProfile(new GameProfile(this.playerUUID, this.playerName));
        }

        @Override
        @MustBeInvokedByOverriders
        public void writeToBuffer(FriendlyByteBuf buf) {
            buf.writeUUID(this.playerUUID);
            buf.writeUtf(this.playerName);
            buf.writeByte(this.model);
            buf.writeByte(this.selected);
            buf.writeLong(this.systemDeathTime);
            buf.writeLong(this.gameDeathTime);
            buf.writeComponent(this.deathMessage);
        }
    }
}
