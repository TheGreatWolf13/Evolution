package tgw.evolution.blocks.tileentities;

import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.gui.ScreenSchematic;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.MathHelper;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tgw.evolution.init.EvolutionBStates.SCHEMATIC_MODE;

public class TESchematic extends BlockEntity {

    private static final int CAP = 64;
    /**
     * Bit 0: ignoresEntities;<br>
     * Bit 1: showAir;<br>
     * Bit 2: showBoundingBox;
     */
    private byte flags = 0b101;
    private float integrity = 1.0F;
    private Mirror mirror = Mirror.NONE;
    private SchematicMode mode = SchematicMode.SAVE;
    private @Nullable ResourceLocation name;
    private Rotation rotation = Rotation.NONE;
    private BlockPos schematicPos = new BlockPos(0, 1, 0);
    private long seed;
    private Vec3i size = Vec3i.ZERO;

    public TESchematic(BlockPos pos, BlockState state) {
        super(EvolutionTEs.SCHEMATIC.get(), pos, state);
    }

    @Nullable
    private static BoundingBox calculateEnclosingBoundingBox(BlockPos startingPos, Stream<BlockPos> relatedCornerBlocks) {
        Iterator<BlockPos> iterator = relatedCornerBlocks.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        BlockPos pos = iterator.next();
        BoundingBox bb = new BoundingBox(pos);
        if (iterator.hasNext()) {
            iterator.forEachRemaining(bb::encapsulate);
        }
        else {
            bb.encapsulate(startingPos);
        }
        return bb;
    }

    private static Random createRandom(long seed) {
        return seed == 0L ? new Random(Util.getMillis()) : new Random(seed);
    }

    public boolean detectSize() {
        if (this.mode != SchematicMode.SAVE) {
            return false;
        }
        BlockPos pos = this.getBlockPos();
        int searchLimit = CAP + 30;
        BlockPos startPos = new BlockPos(pos.getX() - searchLimit, 0, pos.getZ() - searchLimit);
        BlockPos endPos = new BlockPos(pos.getX() + searchLimit, 255, pos.getZ() + searchLimit);
        Stream<BlockPos> stream = this.getRelatedCorners(startPos, endPos);
        BoundingBox bb = calculateEnclosingBoundingBox(pos, stream);
        if (bb == null) {
            return false;
        }
        int dx = bb.maxX() - bb.minX();
        int dy = bb.maxY() - bb.minY();
        int dz = bb.maxZ() - bb.minZ();
        if (dx > 1 && dy > 1 && dz > 1) {
            this.schematicPos = new BlockPos(bb.minX() - pos.getX() + 1, bb.minY() - pos.getY() + 1, bb.minZ() - pos.getZ() + 1);
            this.size = new Vec3i(dx - 1, dy - 1, dz - 1);
            this.setChanged();
            assert this.level != null;
            BlockState state = this.level.getBlockState(pos);
            this.level.sendBlockUpdated(pos, state, state, BlockFlags.NOTIFY_AND_UPDATE);
            return true;
        }
        return false;
    }

    private List<TESchematic> filterRelatedCornerBlocks(List<TESchematic> nearbySchematicBlocks) {
        Predicate<TESchematic> predicate = schematic -> schematic.mode == SchematicMode.CORNER && Objects.equals(this.name, schematic.name);
        return nearbySchematicBlocks.stream().filter(predicate).collect(Collectors.toList());
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public SchematicMode getMode() {
        return this.mode;
    }

    public String getName() {
        return this.name == null ? "" : this.name.toString();
    }

//    private List<TESchematic> getNearbySchematicBlocks(BlockPos startPos, BlockPos endPos) {
//        List<TESchematic> schematicBlocks = Lists.newArrayList();
//        for (BlockPos pos : BlockPos.betweenClosed(startPos, endPos)) {
//            BlockState state = this.level.getBlockState(pos);
//            if (state.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
//                TileEntity tile = this.level.getBlockEntity(pos);
//                if (tile instanceof TESchematic) {
//                    schematicBlocks.add((TESchematic) tile);
//                }
//            }
//        }
//        return schematicBlocks;
//    }

    private Stream<BlockPos> getRelatedCorners(BlockPos startPos, BlockPos endPos) {
        assert this.level != null;
        return BlockPos.betweenClosedStream(startPos, endPos)
                       .filter(pos -> this.level.getBlockState(pos).is(EvolutionBlocks.SCHEMATIC_BLOCK.get()))
                       .map(this.level::getBlockEntity)
                       .filter(tile -> tile instanceof TESchematic)
                       .map(tile2 -> (TESchematic) tile2)
                       .filter(teSchematic -> teSchematic.mode == SchematicMode.CORNER && Objects.equals(this.name, teSchematic.name))
                       .map(BlockEntity::getBlockPos);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return IForgeBlockEntity.INFINITE_EXTENT_AABB;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public BlockPos getSchematicPos() {
        return this.schematicPos;
    }

    public long getSeed() {
        return this.seed;
    }

    public Vec3i getStructureSize() {
        return this.size;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

//    @Override
//    public double getViewDistance() {
//        return 16_384;
//    }

    public boolean hasName() {
        return this.name != null;
    }

    public boolean ignoresEntities() {
        return (this.flags & 1) != 0;
    }

    public boolean isStructureLoadable() {
        if (this.mode == SchematicMode.LOAD && this.level instanceof ServerLevel level && this.name != null) {
            StructureManager manager = level.getStructureManager();
            try {
                return manager.get(this.name).isPresent();
            }
            catch (ResourceLocationException exception) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.setName(tag.getString("Name"));
        int posX = MathHelper.clamp(tag.getInt("PosX"), -CAP, CAP);
        int posY = MathHelper.clamp(tag.getInt("PosY"), -CAP, CAP);
        int posZ = MathHelper.clamp(tag.getInt("PosZ"), -CAP, CAP);
        this.schematicPos = new BlockPos(posX, posY, posZ);
        int sizeX = MathHelper.clamp(tag.getInt("SizeX"), 0, CAP);
        int sizeY = MathHelper.clamp(tag.getInt("SizeY"), 0, CAP);
        int sizeZ = MathHelper.clamp(tag.getInt("SizeZ"), 0, CAP);
        this.size = new Vec3i(sizeX, sizeY, sizeZ);
        try {
            this.rotation = Rotation.valueOf(tag.getString("Rot"));
        }
        catch (IllegalArgumentException exception) {
            this.rotation = Rotation.NONE;
        }
        try {
            this.mirror = Mirror.valueOf(tag.getString("Mirror"));
        }
        catch (IllegalArgumentException exception) {
            this.mirror = Mirror.NONE;
        }
        this.mode = SchematicMode.byId(tag.getByte("Mode"));
        this.flags = tag.getByte("Flags");
        if (tag.contains("Integrity")) {
            this.integrity = tag.getFloat("Integrity");
        }
        else {
            this.integrity = 1.0F;
        }
        this.seed = tag.getLong("Seed");
        this.updateBlockState();
    }

    public boolean loadStructure(ServerLevel level) {
        return this.loadStructure(level, true);
    }

    public boolean loadStructure(ServerLevel level, boolean matchingSize) {
        if (this.mode == SchematicMode.LOAD && this.name != null) {
            StructureManager manager = level.getStructureManager();
            Optional<StructureTemplate> template;
            try {
                template = manager.get(this.name);
            }
            catch (ResourceLocationException e) {
                return false;
            }
            return template.isPresent() && this.loadStructure(level, matchingSize, template.get());
        }
        return false;
    }

    public boolean loadStructure(ServerLevel level, boolean matchingSize, StructureTemplate template) {
        BlockPos pos = this.getBlockPos();
        Vec3i structureSize = template.getSize();
        boolean isSizeEquals = this.size.equals(structureSize);
        if (!isSizeEquals) {
            this.size = structureSize;
            this.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, BlockFlags.NOTIFY_AND_UPDATE);
        }
        if (matchingSize && !isSizeEquals) {
            return false;
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(this.mirror)
                                                                      .setRotation(this.rotation)
                                                                      .setIgnoreEntities(this.ignoresEntities());
        if (this.integrity < 1.0F) {
            settings.clearProcessors()
                    .addProcessor(new BlockRotProcessor(MathHelper.clamp(this.integrity, 0.0F, 1.0F)))
                    .setRandom(createRandom(this.seed));
        }
        BlockPos schematicAbsPos = pos.offset(this.schematicPos);
        template.placeInWorld(level, schematicAbsPos, schematicAbsPos, settings, createRandom(this.seed), 2);
        return true;
    }

    public void nextMode() {
        switch (this.mode) {
            case SAVE -> this.setMode(SchematicMode.LOAD);
            case LOAD -> this.setMode(SchematicMode.CORNER);
            case CORNER -> this.setMode(SchematicMode.SAVE);
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag());
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Name", this.getName());
        tag.putInt("PosX", this.schematicPos.getX());
        tag.putInt("PosY", this.schematicPos.getY());
        tag.putInt("PosZ", this.schematicPos.getZ());
        tag.putInt("SizeX", this.size.getX());
        tag.putInt("SizeY", this.size.getY());
        tag.putInt("SizeZ", this.size.getZ());
        tag.putString("Rot", this.rotation.toString());
        tag.putString("Mirror", this.mirror.toString());
        tag.putByte("Mode", this.mode.getId());
        tag.putByte("Flags", this.flags);
        tag.putFloat("Integrity", this.integrity);
        tag.putLong("Seed", this.seed);
    }

    public boolean saveStructure() {
        return this.saveStructure(true);
    }

    public boolean saveStructure(boolean writeToDisk) {
        if (this.mode == SchematicMode.SAVE && this.level instanceof ServerLevel level && this.name != null) {
            BlockPos pos = this.getBlockPos().offset(this.schematicPos);
            StructureManager structureManager = level.getStructureManager();
            StructureTemplate template;
            try {
                template = structureManager.getOrCreate(this.name);
            }
            catch (ResourceLocationException exception) {
                return false;
            }
            template.fillFromWorld(this.level, pos, this.size, !this.ignoresEntities(), Blocks.STRUCTURE_VOID);
            if (writeToDisk) {
                try {
                    return structureManager.save(this.name);
                }
                catch (ResourceLocationException exception) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void setIgnoresEntities(boolean ignoreEntities) {
        if (this.ignoresEntities() != ignoreEntities) {
            this.flags ^= 1;
        }
    }

    public void setIntegrity(float integrity) {
        this.integrity = integrity;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public void setMode(SchematicMode mode) {
        this.mode = mode;
        assert this.level != null;
        BlockState blockstate = this.level.getBlockState(this.getBlockPos());
        if (blockstate.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
            this.level.setBlock(this.getBlockPos(), blockstate.setValue(SCHEMATIC_MODE, mode), BlockFlags.BLOCK_UPDATE);
        }
    }

    public void setName(@Nullable String name) {
        this.name = StringUtil.isNullOrEmpty(name) ? null : ResourceLocation.tryParse(name);
    }

    public void setName(@Nullable ResourceLocation p_210163_1_) {
        this.name = p_210163_1_;
    }

    public void setRotation(Rotation rotationIn) {
        this.rotation = rotationIn;
    }

    public void setSchematicPos(BlockPos posIn) {
        this.schematicPos = posIn;
    }

    public void setSeed(long seedIn) {
        this.seed = seedIn;
    }

    public void setShowAir(boolean showAir) {
        if (this.showsAir() != showAir) {
            this.flags ^= 1 << 1;
        }
    }

    public void setShowBoundingBox(boolean showBoundingBox) {
        if (this.showsBoundingBox() != showBoundingBox) {
            this.flags ^= 1 << 2;
        }
    }

    public void setSize(BlockPos sizeIn) {
        this.size = sizeIn;
    }

    public boolean showsAir() {
        return (this.flags & 2) != 0;
    }

    public boolean showsBoundingBox() {
        return (this.flags & 4) != 0;
    }

    private void updateBlockState() {
        if (this.level != null) {
            BlockPos pos = this.getBlockPos();
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
                this.level.setBlock(pos, state.setValue(SCHEMATIC_MODE, this.mode), BlockFlags.BLOCK_UPDATE);
            }
        }
    }

    public boolean usedBy(Player player) {
        if (!player.canUseGameMasterBlocks()) {
            return false;
        }
        if (player.getCommandSenderWorld().isClientSide) {
            ScreenSchematic.open(this);
        }
        return true;
    }

    public enum UpdateCommand {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA
    }
}