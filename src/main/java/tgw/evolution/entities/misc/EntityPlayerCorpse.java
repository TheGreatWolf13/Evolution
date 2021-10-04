package tgw.evolution.entities.misc;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
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
import tgw.evolution.entities.EntityPlayerDummy;
import tgw.evolution.entities.EntitySkeletonDummy;
import tgw.evolution.entities.IEntityProperties;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.inventory.corpse.ContainerCorpseProvider;
import tgw.evolution.inventory.extendedinventory.IExtendedItemHandler;
import tgw.evolution.util.*;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.reflection.StaticFieldHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityPlayerCorpse extends Entity implements IEntityAdditionalSpawnData, IEntityProperties, IEvolutionEntity<EntityPlayerCorpse> {

    public static final DataParameter<Boolean> SKELETON = EntityDataManager.defineId(EntityPlayerCorpse.class, DataSerializers.BOOLEAN);
    private static final StaticFieldHandler<PlayerEntity, DataParameter<Byte>> DATA_PLAYER_MODE = new StaticFieldHandler<>(PlayerEntity.class,
                                                                                                                           "field_184827_bp");
    private static final DataParameter<NonNullList<ItemStack>> EQUIPMENT = EntityDataManager.defineId(EntityPlayerCorpse.class,
                                                                                                      DataSerializer.ITEM_LIST);
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
    private ITextComponent deathMessage;
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

    public EntityPlayerCorpse(PlayerEntity player) {
        this(EvolutionEntities.PLAYER_CORPSE.get(), player.level);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        this.setPos(x, y, z);
        this.setDeltaMovement(Vector3d.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.playerUUID = player.getUUID();
        this.playerName = player.getScoreboardName();
        this.yRot = player.yRot;
        this.setPlayerProfile(player.getGameProfile());
        this.model = player.getEntityData().get(DATA_PLAYER_MODE.get());
        if (player.getMainArm() == HandSide.RIGHT) {
            this.model |= 1 << 7;
        }
        NonNullList<ItemStack> equip = NonNullList.withSize(SlotType.SLOTS.length + SlotType.VALUES.length, ItemStack.EMPTY);
        int i = 0;
        for (EquipmentSlotType slot : SlotType.SLOTS) {
            equip.set(i++, player.getItemBySlot(slot).copy());
        }
        IExtendedItemHandler additionalEquip = player.getCapability(CapabilityExtendedInventory.INSTANCE).orElseThrow(IllegalStateException::new);
        for (SlotType slot : SlotType.VALUES) {
            equip.set(i++, additionalEquip.getStackInSlot(slot.ordinal()).copy());
        }
        this.setEquipment(equip);
        this.selected = player.inventory.selected;
        this.lastTick = this.level.getGameTime();
        this.gameDeathTime = this.level.getDayTime();
        this.systemDeathTime = System.currentTimeMillis();
        this.deathMessage = player.getCombatTracker().getDeathMessage();
    }

    public EntityPlayerCorpse(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.blocksBuilding = true;
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
                GameProfile gameprofile = profileCache.get(input.getName());
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
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.put("Inventory", this.itemHandler.serializeNBT());
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("IsSkeleton", this.isSkeleton);
        compound.putByte("Model", this.model);
        compound.putLong("LastTick", this.lastTick);
        compound.putLong("SystemDeathTime", this.systemDeathTime);
        compound.putLong("GameDeathTime", this.gameDeathTime);
        compound.putString("DeathMessage", ITextComponent.Serializer.toJson(this.deathMessage));
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
        this.entityData.define(EQUIPMENT, NonNullList.withSize(SlotType.SLOTS.length + SlotType.VALUES.length, ItemStack.EMPTY));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
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

    public ITextComponent getDeathMessage() {
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
    public ITextComponent getName() {
        if (this.isSkeleton()) {
            return new TranslationTextComponent("entity.evolution.player_skeleton", this.playerName);
        }
        return new TranslationTextComponent("entity.evolution.player_corpse", this.playerName);
    }

    @Nullable
    public EntityPlayerDummy getPlayer() {
        if (this.level.isClientSide) {
            if (this.player == null) {
                this.player = new EntityPlayerDummy((ClientWorld) this.level, this.playerProfile, this.getEquipment(), this.model);
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
                this.skeleton = new EntitySkeletonDummy(this.level, this.getEquipment(), (this.model & 1 << 7) != 0 ? HandSide.RIGHT : HandSide.LEFT);
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
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (!this.level.isClientSide) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerCorpseProvider(this), packet -> packet.writeInt(this.getId()));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.removed;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public boolean isSkeleton() {
        return this.entityData.get(SKELETON);
    }

    public void onClose(PlayerEntity player) {
        this.playersInteracting.remove(player.getId());
    }

    public void onOpen(PlayerEntity player) {
        this.playersInteracting.add(player.getId());
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> data) {
        if (data == EQUIPMENT) {
            if (this.level.isClientSide) {
                NonNullList<ItemStack> equipment = this.entityData.get(EQUIPMENT);
                if (this.player != null) {
                    for (EquipmentSlotType type : SlotType.SLOTS) {
                        this.player.setItemSlot(type, equipment.get(type.ordinal()));
                    }
                }
                if (this.skeleton != null) {
                    for (EquipmentSlotType type : SlotType.SLOTS) {
                        this.skeleton.setItemSlot(type, equipment.get(type.ordinal()));
                    }
                }
            }
        }
        super.onSyncedDataUpdated(data);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        this.itemHandler.deserializeNBT(compound.getCompound("Inventory"));
        this.deathTimer = compound.getInt("DeathTimer");
        this.isSkeleton = compound.getBoolean("IsSkeleton");
        this.entityData.set(SKELETON, this.isSkeleton);
        this.model = compound.getByte("Model");
        this.lastTick = compound.getLong("LastTick");
        this.entityData.set(EQUIPMENT, NBTHelper.readStackList(compound.getCompound("Equipment")));
        this.systemDeathTime = compound.getLong("SystemDeathTime");
        this.gameDeathTime = compound.getLong("GameDeathTime");
        this.deathMessage = ITextComponent.Serializer.fromJson(compound.getString("DeathMessage"));
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
    public void readSpawnData(PacketBuffer buffer) {
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

    public void setInventory(PlayerEntity player) {
        NonNullList<ItemStack> inv = player.inventory.armor;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = inv.get(3 - i);
            this.itemHandler.setStackInSlot(i, stack);
            inv.set(3 - i, ItemStack.EMPTY);
        }
        inv = player.inventory.offhand;
        for (int i = 0; i < 1; i++) {
            ItemStack stack = inv.get(0);
            this.itemHandler.setStackInSlot(10, stack);
            inv.set(0, ItemStack.EMPTY);
        }
        inv = player.inventory.items;
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
                case EvolutionResources.HAT: {
                    this.itemHandler.setStackInSlot(7, stack);
                    break;
                }
                case EvolutionResources.BODY: {
                    this.itemHandler.setStackInSlot(6, stack);
                    break;
                }
                case EvolutionResources.LEGS: {
                    this.itemHandler.setStackInSlot(5, stack);
                    break;
                }
                case EvolutionResources.FEET: {
                    this.itemHandler.setStackInSlot(4, stack);
                    break;
                }
                case EvolutionResources.MASK: {
                    this.itemHandler.setStackInSlot(8, stack);
                    break;
                }
                case EvolutionResources.CLOAK: {
                    this.itemHandler.setStackInSlot(9, stack);
                    break;
                }
                case EvolutionResources.BACK: {
                    this.itemHandler.setStackInSlot(11, stack);
                    break;
                }
                case EvolutionResources.TACTICAL: {
                    this.itemHandler.setStackInSlot(12, stack);
                    break;
                }
            }
        }
    }

    public void setPlayerProfile(@Nullable GameProfile gameProfile) {
        this.playerProfile = gameProfile;
        this.updatePlayerProfile();
    }

    public void setSlot(EquipmentSlotType slot, ItemStack stack) {
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
        Vector3d motion = this.getDeltaMovement();
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
        float slipperiness = this.level.getBlockState(blockBelow).getSlipperiness(this.level, blockBelow, this);
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
        this.remove();
    }

    private void updatePlayerProfile() {
        this.playerProfile = updateGameProfile(this.playerProfile);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeUUID(this.playerUUID);
        buffer.writeUtf(this.playerName);
        buffer.writeByte(this.model);
        buffer.writeByte(this.selected);
        buffer.writeLong(this.systemDeathTime);
        buffer.writeLong(this.gameDeathTime);
        buffer.writeComponent(this.deathMessage);
    }
}
