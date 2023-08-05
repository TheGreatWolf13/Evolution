package tgw.evolution.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
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
import tgw.evolution.patches.PatchServerPacketListener;
import tgw.evolution.util.EntityHelper;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.BlockPosUtil;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl implements ServerGamePacketListener, PatchServerPacketListener {

    @Shadow @Final static Logger LOGGER;
    @Shadow public ServerPlayer player;
    @Shadow private int aboveGroundTickCount;
    @Shadow private int aboveGroundVehicleTickCount;
    @Shadow private @Nullable Vec3 awaitingPositionFromClient;
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

    @Contract(value = "_, _ -> _")
    @Shadow
    private static boolean wasBlockPlacementAttempt(ServerPlayer serverPlayer, ItemStack itemStack) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Shadow
    public abstract void disconnect(Component p_9943_);

    @Shadow
    public abstract ServerPlayer getPlayer();

//    @Override
//    public void handleChangeBlock(PacketCSChangeBlock packet) {
//        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
//        Item item = this.player.getMainHandItem().getItem();
//        if (item instanceof BlockItem blockItem) {
//            Level level = this.player.level;
//            BlockHitResult result = PatchBlockHitResult.create(packet.hitX, packet.hitY, packet.hitZ, packet.direction, packet.pos, packet
//            .isInside);
//            UseOnContext itemContext = new UseOnContext(this.player, InteractionHand.MAIN_HAND, result);
//            BlockPlaceContext blockContext = new BlockPlaceContext(itemContext);
//            BlockState state = blockItem.getBlock().getStateForPlacement(blockContext);
//            if (state != null) {
//                if (state.getBlock() instanceof ButtonBlock) {
//                    return;
//                }
//                level.setBlockAndUpdate(packet.pos, state);
//            }
//        }
//    }

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
    public void handleEntityInteraction(PacketCSEntityInteraction packet) {
        ServerLevel level = this.player.getLevel();
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        Entity entity = level.getEntity(packet.entityId);
        this.player.resetLastActionTime();
        this.player.setShiftKeyDown(packet.secondaryAction);
        if (entity == null) {
            return;
        }
        BlockPos pos = entity.blockPosition();
        if (!level.getWorldBorder().isWithinBounds_(pos.getX(), pos.getZ())) {
            return;
        }
        if (this.player.distanceToSqr(entity) < 6 * 6) {
            entity.interactAt_(this.player, packet.hitX, packet.hitY, packet.hitZ, packet.hand);
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
    public void handlePlayerAction(PacketCSPlayerAction packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action action = packet.action;
        switch (action) {
            case SWAP_ITEM_WITH_OFFHAND -> {
                if (!this.player.isSpectator()) {
                    ItemStack itemStack = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                    this.player.stopUsingItem();
                }
            }
            case DROP_ITEM -> {
                if (!this.player.isSpectator()) {
                    this.player.drop(false);
                }
            }
            case DROP_ALL_ITEMS -> {
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }
            }
            case RELEASE_USE_ITEM -> this.player.releaseUsingItem();
            case START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK -> {
                this.player.gameMode.handleBlockBreakAction_(packet.pos, action, packet.direction, packet.x, packet.y, packet.z,
                                                             this.player.level.getMaxBuildHeight());
            }
            default -> throw new IllegalArgumentException("Invalid player action");
        }
    }

    @Override
    public void handlePlayerFall(PacketCSPlayerFall packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        LivingHooks.calculateFallDamage(this.player, packet.velocity, packet.distanceOfSlowDown, packet.water);
    }

    @Override
    public void handleSetCrawling(PacketCSSetCrawling packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        this.player.setCrawling(packet.crawling);
    }

    @Override
    public void handleSetKnappingType(PacketCSSetKnappingType packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        long pos = packet.pos;
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockEntity tile = this.player.level.getBlockEntity_(x, y, z);
        if (tile instanceof TEKnapping knapping) {
            knapping.setType(packet.type);
        }
        else {
            Evolution.warn("Could not find TEKnapping at [{}, {}, {}]", x, y, z);
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
        this.player.startSpecialAttack(packet.type);
    }

    @Override
    public void handleSpecialAttackStop(PacketCSSpecialAttackStop packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.getLevel());
        this.player.stopSpecialAttack(packet.reason);
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

    @Override
    @Overwrite
    public void handleUseItemOn(ServerboundUseItemOnPacket packet) {
        ServerLevel level = this.player.getLevel();
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        InteractionHand hand = packet.getHand();
        ItemStack stack = this.player.getItemInHand(hand);
        BlockHitResult hitResult = packet.getHitResult();
        double hitX = hitResult.x();
        double hitY = hitResult.y();
        double hitZ = hitResult.z();
        int x = hitResult.posX();
        int y = hitResult.posY();
        int z = hitResult.posZ();
        if (this.player.level.getServer() != null &&
            BlockPosUtil.getChessBoardDistance(this.player.chunkPosition(), SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)) <
            this.player.level.getServer().getPlayerList().getViewDistance()) {
            if (Math.abs(hitX - x - 0.5) < 1 && Math.abs(hitY - y - 0.5) < 1 && Math.abs(hitZ - z - 0.5) < 1) {
                Direction dir = hitResult.getDirection();
                this.player.resetLastActionTime();
                int buildHeight = this.player.level.getMaxBuildHeight();
                if (y < buildHeight) {
                    if (this.awaitingPositionFromClient == null &&
                        this.player.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) < 9 * 9 &&
                        level.mayInteract_(this.player, x, y, z)) {
                        InteractionResult interactionResult = this.player.gameMode.useItemOn(this.player, level, stack, hand, hitResult);
                        if (dir == Direction.UP &&
                            !interactionResult.consumesAction() &&
                            y >= buildHeight - 1 &&
                            wasBlockPlacementAttempt(this.player, stack)) {
                            this.player.sendMessage(new TranslatableComponent("build.tooHigh", buildHeight - 1).withStyle(ChatFormatting.RED),
                                                    ChatType.GAME_INFO, Util.NIL_UUID);
                        }
                        else if (interactionResult.shouldSwing()) {
                            this.player.swing(hand, true);
                        }
                    }
                }
                else {
                    this.player.sendMessage(new TranslatableComponent("build.tooHigh", buildHeight - 1).withStyle(ChatFormatting.RED),
                                            ChatType.GAME_INFO, Util.NIL_UUID);
                }
                this.player.connection.send(new PacketSCBlockUpdate(level, x, y, z));
                this.player.connection.send(new PacketSCBlockUpdate(level, x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ()));
            }
            else {
                LOGGER.warn("Ignoring UseItemOnPacket from {}: Location [{}, {}, {}] too far away from hit block [{}, {}, {}].",
                            this.player.getGameProfile().getName(), hitX, hitY, hitZ, x, y, z);
            }
        }
        else {
            LOGGER.warn("Ignoring UseItemOnPacket from {}: hit position [{}, {}, {}] too far away from player {}.",
                        this.player.getGameProfile().getName(), x, y, z, this.player.blockPosition());
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
        if (!this.server.isMultiplayerPaused()) { //Added check for multiplayer pause
            this.player.doTick();
        }
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating &&
            !this.player.isSleeping() &&
            !this.server.isMultiplayerPaused()) { //Added check for multiplayer pause
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
                !this.server.isMultiplayerPaused()) { //Added check for multiplayer pause
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
            !this.server.isMultiplayerPaused() &&
            Util.getMillis() - this.player.getLastActionTime() > this.server.getPlayerIdleTimeout() * 1_000L * 60L) {
            this.disconnect(new TranslatableComponent("multiplayer.disconnect.idling"));
        }
    }
}
