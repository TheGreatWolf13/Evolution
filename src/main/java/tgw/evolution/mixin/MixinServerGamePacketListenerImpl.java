package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.ICollisionBlock;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.blocks.tileentities.TESchematic;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.hooks.LivingHooks;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.inventory.extendedinventory.ContainerInventoryProvider;
import tgw.evolution.network.*;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.patches.PatchMinecraftServer;
import tgw.evolution.patches.PatchPlayer;
import tgw.evolution.patches.PatchServerPacketListener;
import tgw.evolution.util.EntityHelper;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl implements ServerGamePacketListener, PatchServerPacketListener {

    @Shadow @Final static Logger LOGGER;
    @Shadow public ServerPlayer player;
    @Shadow private int aboveGroundTickCount;
    @Shadow private int aboveGroundVehicleTickCount;
    @Shadow private int chatSpamTickCount;
    @Shadow private boolean clientIsFloating;
    @Shadow private boolean clientVehicleIsFloating;
    @Shadow private int dropSpamTickCount;
    @Shadow private double firstGoodX;
    @Shadow private double firstGoodY;
    @Shadow private double firstGoodZ;
    @Shadow private long keepAliveChallenge;
    @Shadow private boolean keepAlivePending;
    @Shadow private long keepAliveTime;
    @Shadow private int knownMovePacketCount;
    @Shadow private @Nullable Entity lastVehicle;
    @Shadow private int receivedMovePacketCount;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int tickCount;
    @Shadow private double vehicleFirstGoodX;
    @Shadow private double vehicleFirstGoodY;
    @Shadow private double vehicleFirstGoodZ;
    @Shadow private double vehicleLastGoodX;
    @Shadow private double vehicleLastGoodY;
    @Shadow private double vehicleLastGoodZ;

    @Shadow
    public abstract void disconnect(Component p_9943_);

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Override
    public void handleChangeBlock(PacketCSChangeBlock packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        Item item = this.player.getMainHandItem().getItem();
        if (item instanceof BlockItem blockItem) {
            Level level = this.player.level;
            BlockHitResult result = new BlockHitResult(packet.vec, packet.direction, packet.pos, packet.isInside);
            UseOnContext itemContext = new UseOnContext(this.player, InteractionHand.MAIN_HAND, result);
            BlockPlaceContext blockContext = new BlockPlaceContext(itemContext);
            BlockState state = blockItem.getBlock().getStateForPlacement(blockContext);
            if (state != null) {
                if (state.getBlock() instanceof ButtonBlock) {
                    return;
                }
                level.setBlockAndUpdate(packet.pos, state);
            }
        }
    }

    @Override
    public void handleCollision(PacketCSCollision packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        Level level = this.player.level;
        long pos = packet.pos;
        BlockState state = level.getBlockState_(pos);
        if (state.getBlock() instanceof ICollisionBlock collisionBlock) {
            double mass = this.player.getAttributeValue(EvolutionAttributes.MASS);
            collisionBlock.collision(level, BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), this.player, packet.speed, mass, packet.axis);
        }
    }

    @Override
    public void handleImpactDamage(PacketCSImpactDamage packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        this.player.hurt(EvolutionDamage.WALL_IMPACT, packet.damage);
    }

    @Override
    public void handlePlaySoundEntityEmitted(PacketCSPlaySoundEntityEmitted packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        this.player.getLevel()
                   .getChunkSource()
                   .broadcast(this.player,
                              new PacketSCPlaySoundEntityEmitted(this.player, packet.sound, packet.category, packet.volume, packet.pitch));

    }

    @Override
    public void handlePlayerFall(PacketCSPlayerFall packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        LivingHooks.calculateFallDamage(this.player, packet.velocity, packet.distanceOfSlowDown, packet.water);
    }

    @Override
    public void handleSetCrawling(PacketCSSetCrawling packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        ((PatchPlayer) this.player).setCrawling(packet.crawling);
    }

    @Override
    public void handleSetKnappingType(PacketCSSetKnappingType packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        BlockEntity tile = this.player.level.getBlockEntity(packet.pos);
        if (tile instanceof TEKnapping knapping) {
            knapping.setType(packet.type);
        }
        else {
            Evolution.warn("Could not find TEKnapping at {}", packet.pos);
        }
    }

    @Override
    public void handleSetMoldingType(PacketCSSetMoldingType packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        BlockEntity tile = this.player.level.getBlockEntity(packet.pos);
        if (tile instanceof TEMolding molding) {
            molding.setType(packet.molding);
        }
        else {
            Evolution.warn("Could not find TEMolding at {}", packet.pos);
        }
    }

    @Override
    public void handleSimpleMessage(PacketCSSimpleMessage packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        switch (packet.message) {
            case OPEN_INVENTORY -> this.player.openMenu(new ContainerInventoryProvider());
            case STOP_USING_ITEM -> this.player.stopUsingItem();
            default -> throw new IllegalStateException("Unhandled Simple Message: " + packet.message);
        }
    }

    @Override
    public void handleSkinType(PacketCSSkinType packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        EntityEvents.SKIN_TYPE.put(this.player.getUUID(), packet.skin);
    }

    @Override
    public void handleSpecialAttackStart(PacketCSSpecialAttackStart packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        ((PatchLivingEntity) this.player).startSpecialAttack(packet.type);
    }

    @Override
    public void handleSpecialAttackStop(PacketCSSpecialAttackStop packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        ((PatchLivingEntity) this.player).stopSpecialAttack(packet.reason);
    }

    @Override
    public void handleSpecialHit(PacketCSSpecialHit packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        Entity victim = this.player.level.getEntity(packet.victimId);
        if (victim != null) {
            EntityHelper.attackEntity(this.player, victim, packet.type, packet.hitboxSet);
        }
    }

    @Override
    public void handleUpdateBeltBackItem(PacketCSUpdateBeltBackItem packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        this.player.getLevel()
                   .getChunkSource()
                   .broadcast(this.player, new PacketSCUpdateBeltBackItem(this.player, packet.back, packet.stack));
    }

    @Override
    public void handleUpdatePuzzle(PacketCSUpdatePuzzle packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        BlockEntity tile = this.player.level.getBlockEntity(packet.pos);
        if (!(tile instanceof TEPuzzle puzzle)) {
            Evolution.warn("Could not find TEPuzzle at " + packet.pos);
            return;
        }
        puzzle.setAttachmentType(packet.attachmentType);
        puzzle.setTargetPool(packet.targetPool);
        puzzle.setFinalState(packet.finalState);
        puzzle.setCheckBB(packet.checkBB);
    }

    @Override
    public void handleUpdateSchematicBlock(PacketCSUpdateSchematicBlock packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos tilePos = packet.tilePos;
            BlockState state = this.player.level.getBlockState(tilePos);
            BlockEntity tile = this.player.level.getBlockEntity(tilePos);
            if (tile instanceof TESchematic teSchematic) {
                teSchematic.setMode(packet.mode);
                teSchematic.setName(packet.name);
                teSchematic.setSchematicPos(packet.schematicPos);
                teSchematic.setSize(packet.size);
                teSchematic.setMirror(packet.mirror);
                teSchematic.setRotation(packet.rotation);
                teSchematic.setIgnoresEntities(packet.ignoresEntities);
                teSchematic.setShowAir(packet.showAir);
                teSchematic.setShowBoundingBox(packet.showBB);
                teSchematic.setIntegrity(packet.integrity);
                teSchematic.setSeed(packet.seed);
                if (teSchematic.hasName()) {
                    String s = teSchematic.getName();
                    switch (packet.command) {
                        case SAVE_AREA -> {
                            if (teSchematic.saveStructure()) {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.save_success", s), false);
                            }
                            else {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.save_failure", s), false);
                            }
                        }
                        case LOAD_AREA -> {
                            if (!teSchematic.isStructureLoadable()) {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.load_not_found", s), false);
                            }
                            else if (teSchematic.loadStructure(this.player.getLevel())) {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.load_success", s), false);
                            }
                            else {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.load_prepare", s), false);
                            }
                        }
                        case SCAN_AREA -> {
                            if (teSchematic.detectSize()) {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.size_success", s), false);
                            }
                            else {
                                this.player.displayClientMessage(new TranslatableComponent("structure_block.size_failure"), false);
                            }
                        }
                    }
                }
                else {
                    this.player.displayClientMessage(new TranslatableComponent("structure_block.invalid_structure_name", packet.name), false);
                }
                teSchematic.setChanged();
                this.player.level.sendBlockUpdated(tilePos, state, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
            }
        }
    }

    @Shadow
    public abstract void resetPosition();

    @Shadow
    public abstract void send(Packet<?> p_9830_);

    /**
     * @author TheGreatWolf
     * @reason Overwrite to prevent kicking players for flying when the server is multiplayer paused.
     * Also prevents players from being ticked while the server is paused.
     */
    @Overwrite
    public void tick() {
        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        if (!((PatchMinecraftServer) this.server).isMultiplayerPaused()) { //Added check for multiplayer pause
            this.player.doTick();
        }
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating &&
            !this.player.isSleeping() &&
            !((PatchMinecraftServer) this.server).isMultiplayerPaused()) { //Added check for multiplayer pause
            if (++this.aboveGroundTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
                this.disconnect(new TranslatableComponent("multiplayer.disconnect.flying"));
                return;
            }
        }
        else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }
        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating &&
                this.player.getRootVehicle().getControllingPassenger() == this.player &&
                !((PatchMinecraftServer) this.server).isMultiplayerPaused()) { //Added check for multiplayer pause
                if (++this.aboveGroundVehicleTickCount > 80) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
                    this.disconnect(new TranslatableComponent("multiplayer.disconnect.flying"));
                    return;
                }
            }
            else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        }
        else {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        }
        this.server.getProfiler().push("keepAlive");
        long i = Util.getMillis();
        if (i - this.keepAliveTime >= 15_000L) {
            if (this.keepAlivePending) {
                this.disconnect(new TranslatableComponent("disconnect.timeout"));
            }
            else {
                this.keepAlivePending = true;
                this.keepAliveTime = i;
                this.keepAliveChallenge = i;
                this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
            }
        }
        this.server.getProfiler().pop();
        if (this.chatSpamTickCount > 0) {
            --this.chatSpamTickCount;
        }
        if (this.dropSpamTickCount > 0) {
            --this.dropSpamTickCount;
        }
        if (this.player.getLastActionTime() > 0L &&
            this.server.getPlayerIdleTimeout() > 0 &&
            //Added check for multiplayer pause
            !((PatchMinecraftServer) this.server).isMultiplayerPaused() &&
            Util.getMillis() - this.player.getLastActionTime() > this.server.getPlayerIdleTimeout() * 1_000L * 60L) {
            this.disconnect(new TranslatableComponent("multiplayer.disconnect.idling"));
        }
    }
}
