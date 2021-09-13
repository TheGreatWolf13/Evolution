package tgw.evolution.blocks.tileentities;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.template.IntegrityProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.extensions.IForgeTileEntity;
import tgw.evolution.client.gui.ScreenSchematic;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.BlockFlags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static tgw.evolution.init.EvolutionBStates.SCHEMATIC_MODE;

public class TESchematic extends TileEntity {

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
    private ResourceLocation name;
    private Rotation rotation = Rotation.NONE;
    private BlockPos schematicPos = new BlockPos(0, 1, 0);
    private long seed;
    private BlockPos size = BlockPos.ZERO;

    public TESchematic() {
        super(EvolutionTEs.SCHEMATIC.get());
    }

    private static MutableBoundingBox calculateEnclosingBoundingBox(BlockPos startingPos, List<TESchematic> relatedCornerBlocks) {
        MutableBoundingBox boundingBox;
        if (relatedCornerBlocks.size() > 1) {
            BlockPos pos = relatedCornerBlocks.get(0).getBlockPos();
            boundingBox = new MutableBoundingBox(pos, pos);
        }
        else {
            boundingBox = new MutableBoundingBox(startingPos, startingPos);
        }
        for (TESchematic tile : relatedCornerBlocks) {
            BlockPos pos = tile.getBlockPos();
            if (pos.getX() < boundingBox.x0) {
                boundingBox.x0 = pos.getX();
            }
            else if (pos.getX() > boundingBox.x1) {
                boundingBox.x1 = pos.getX();
            }
            if (pos.getY() < boundingBox.y0) {
                boundingBox.y0 = pos.getY();
            }
            else if (pos.getY() > boundingBox.y1) {
                boundingBox.y1 = pos.getY();
            }
            if (pos.getZ() < boundingBox.z0) {
                boundingBox.z0 = pos.getZ();
            }
            else if (pos.getZ() > boundingBox.z1) {
                boundingBox.z1 = pos.getZ();
            }
        }
        return boundingBox;
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
        List<TESchematic> nearbyCornerBlocks = this.getNearbySchematicBlocks(startPos, endPos);
        List<TESchematic> relatedCornerBlocks = this.filterRelatedCornerBlocks(nearbyCornerBlocks);
        if (relatedCornerBlocks.size() < 1) {
            return false;
        }
        MutableBoundingBox boundingBox = calculateEnclosingBoundingBox(pos, relatedCornerBlocks);
        if (boundingBox.x1 - boundingBox.x0 > 1 && boundingBox.y1 - boundingBox.y0 > 1 && boundingBox.z1 - boundingBox.z0 > 1) {
            this.schematicPos = new BlockPos(boundingBox.x0 - pos.getX() + 1, boundingBox.y0 - pos.getY() + 1, boundingBox.z0 - pos.getZ() + 1);
            this.size = new BlockPos(boundingBox.x1 - boundingBox.x0 - 1, boundingBox.y1 - boundingBox.y0 - 1, boundingBox.z1 - boundingBox.z0 - 1);
            this.setChanged();
            BlockState state = this.level.getBlockState(pos);
            this.level.sendBlockUpdated(pos, state, state, 3);
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

    private List<TESchematic> getNearbySchematicBlocks(BlockPos startPos, BlockPos endPos) {
        List<TESchematic> schematicBlocks = Lists.newArrayList();
        for (BlockPos pos : BlockPos.betweenClosed(startPos, endPos)) {
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
                TileEntity tile = this.level.getBlockEntity(pos);
                if (tile instanceof TESchematic) {
                    schematicBlocks.add((TESchematic) tile);
                }
            }
        }
        return schematicBlocks;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return IForgeTileEntity.INFINITE_EXTENT_AABB;
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

    public BlockPos getStructureSize() {
        return this.size;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 7, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public double getViewDistance() {
        return 16_384;
    }

    public boolean hasName() {
        return this.name != null;
    }

    public boolean ignoresEntities() {
        return (this.flags & 1) != 0;
    }

    public boolean isStructureLoadable() {
        if (this.mode == SchematicMode.LOAD && !this.level.isClientSide && this.name != null) {
            ServerWorld serverWorld = (ServerWorld) this.level;
            TemplateManager templateManager = serverWorld.getStructureManager();
            try {
                return templateManager.get(this.name) != null;
            }
            catch (ResourceLocationException exception) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.setName(compound.getString("Name"));
        int posX = MathHelper.clamp(compound.getInt("PosX"), -CAP, CAP);
        int posY = MathHelper.clamp(compound.getInt("PosY"), -CAP, CAP);
        int posZ = MathHelper.clamp(compound.getInt("PosZ"), -CAP, CAP);
        this.schematicPos = new BlockPos(posX, posY, posZ);
        int sizeX = MathHelper.clamp(compound.getInt("SizeX"), 0, CAP);
        int sizeY = MathHelper.clamp(compound.getInt("SizeY"), 0, CAP);
        int sizeZ = MathHelper.clamp(compound.getInt("SizeZ"), 0, CAP);
        this.size = new BlockPos(sizeX, sizeY, sizeZ);
        try {
            this.rotation = Rotation.valueOf(compound.getString("Rot"));
        }
        catch (IllegalArgumentException exception) {
            this.rotation = Rotation.NONE;
        }
        try {
            this.mirror = Mirror.valueOf(compound.getString("Mirror"));
        }
        catch (IllegalArgumentException exception) {
            this.mirror = Mirror.NONE;
        }
        this.mode = SchematicMode.byId(compound.getByte("Mode"));
        this.flags = compound.getByte("Flags");
        if (compound.contains("Integrity")) {
            this.integrity = compound.getFloat("Integrity");
        }
        else {
            this.integrity = 1.0F;
        }
        this.seed = compound.getLong("Seed");
        this.updateBlockState();
    }

    public boolean load(ServerWorld world) {
        return this.load(world, true);
    }

    public boolean load(ServerWorld world, boolean matchingSize) {
        if (this.mode == SchematicMode.LOAD && this.name != null) {
            TemplateManager templateManager = world.getStructureManager();
            Template template;
            try {
                template = templateManager.get(this.name);
            }
            catch (ResourceLocationException e) {
                return false;
            }
            return template != null && this.load(world, matchingSize, template);
        }
        return false;
    }

    public boolean load(ServerWorld world, boolean matchingSize, Template template) {
        BlockPos pos = this.getBlockPos();
        BlockPos structureSize = template.getSize();
        boolean isSizeEquals = this.size.equals(structureSize);
        if (!isSizeEquals) {
            this.size = structureSize;
            this.setChanged();
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, BlockFlags.NOTIFY_AND_UPDATE);
        }
        if (matchingSize && !isSizeEquals) {
            return false;
        }
        PlacementSettings settings = new PlacementSettings().setMirror(this.mirror)
                                                            .setRotation(this.rotation)
                                                            .setIgnoreEntities(this.ignoresEntities())
                                                            .setChunkPos(null);
        if (this.integrity < 1.0F) {
            settings.clearProcessors()
                    .addProcessor(new IntegrityProcessor(MathHelper.clamp(this.integrity, 0.0F, 1.0F)))
                    .setRandom(createRandom(this.seed));
        }

        BlockPos schematicAbsPos = pos.offset(this.schematicPos);
        template.placeInWorld(world, schematicAbsPos, settings, createRandom(this.seed));
        return true;
    }

    public void nextMode() {
        switch (this.mode) {
            case SAVE:
                this.setMode(SchematicMode.LOAD);
                break;
            case LOAD:
                this.setMode(SchematicMode.CORNER);
                break;
            case CORNER:
                this.setMode(SchematicMode.SAVE);
                break;
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), pkt.getTag());
    }

    public boolean save() {
        return this.save(true);
    }

    public boolean save(boolean writeToDisk) {
        if (this.mode == SchematicMode.SAVE && !this.level.isClientSide && this.name != null) {
            BlockPos pos = this.getBlockPos().offset(this.schematicPos);
            ServerWorld serverWorld = (ServerWorld) this.level;
            TemplateManager templateManager = serverWorld.getStructureManager();
            Template template;
            try {
                template = templateManager.get(this.name);
            }
            catch (ResourceLocationException exception) {
                return false;
            }
            template.fillFromWorld(this.level, pos, this.size, !this.ignoresEntities(), Blocks.STRUCTURE_VOID);
            if (writeToDisk) {
                try {
                    return templateManager.save(this.name);
                }
                catch (ResourceLocationException exception) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putString("Name", this.getName());
        compound.putInt("PosX", this.schematicPos.getX());
        compound.putInt("PosY", this.schematicPos.getY());
        compound.putInt("PosZ", this.schematicPos.getZ());
        compound.putInt("SizeX", this.size.getX());
        compound.putInt("SizeY", this.size.getY());
        compound.putInt("SizeZ", this.size.getZ());
        compound.putString("Rot", this.rotation.toString());
        compound.putString("Mirror", this.mirror.toString());
        compound.putByte("Mode", this.mode.getId());
        compound.putByte("Flags", this.flags);
        compound.putFloat("Integrity", this.integrity);
        compound.putLong("Seed", this.seed);
        return compound;
    }

    public void setIgnoresEntities(boolean ignoreEntitiesIn) {
        if (this.ignoresEntities() != ignoreEntitiesIn) {
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
        BlockState blockstate = this.level.getBlockState(this.getBlockPos());
        if (blockstate.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
            this.level.setBlock(this.getBlockPos(), blockstate.setValue(SCHEMATIC_MODE, mode), BlockFlags.BLOCK_UPDATE);
        }
    }

    public void setName(@Nullable String name) {
        this.name = StringUtils.isNullOrEmpty(name) ? null : ResourceLocation.tryParse(name);
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

    public boolean usedBy(PlayerEntity player) {
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