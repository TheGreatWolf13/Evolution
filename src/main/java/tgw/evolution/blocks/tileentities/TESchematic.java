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
import tgw.evolution.blocks.BlockSchematic;
import tgw.evolution.client.gui.ScreenSchematic;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionTileEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TESchematic extends TileEntity {

    private static final int CAP = 64;
    private boolean ignoreEntities = true;
    private float integrity = 1.0F;
    private Mirror mirror = Mirror.NONE;
    private SchematicMode mode = SchematicMode.SAVE;
    private ResourceLocation name;
    private Rotation rotation = Rotation.NONE;
    private BlockPos schematicPos = new BlockPos(0, 1, 0);
    private long seed;
    private boolean showAir;
    private boolean showBoundingBox = true;
    private BlockPos size = BlockPos.ZERO;

    public TESchematic() {
        super(EvolutionTileEntities.TE_SCHEMATIC.get());
    }

    private static MutableBoundingBox calculateEnclosingBoundingBox(BlockPos startingPos, List<TESchematic> relatedCornerBlocks) {
        MutableBoundingBox boundingBox;
        if (relatedCornerBlocks.size() > 1) {
            BlockPos pos = relatedCornerBlocks.get(0).getPos();
            boundingBox = new MutableBoundingBox(pos, pos);
        }
        else {
            boundingBox = new MutableBoundingBox(startingPos, startingPos);
        }
        for (TESchematic tile : relatedCornerBlocks) {
            BlockPos pos = tile.getPos();
            if (pos.getX() < boundingBox.minX) {
                boundingBox.minX = pos.getX();
            }
            else if (pos.getX() > boundingBox.maxX) {
                boundingBox.maxX = pos.getX();
            }
            if (pos.getY() < boundingBox.minY) {
                boundingBox.minY = pos.getY();
            }
            else if (pos.getY() > boundingBox.maxY) {
                boundingBox.maxY = pos.getY();
            }
            if (pos.getZ() < boundingBox.minZ) {
                boundingBox.minZ = pos.getZ();
            }
            else if (pos.getZ() > boundingBox.maxZ) {
                boundingBox.maxZ = pos.getZ();
            }
        }
        return boundingBox;
    }

    private static Random createRandom(long seed) {
        return seed == 0L ? new Random(Util.milliTime()) : new Random(seed);
    }

    public boolean detectSize() {
        if (this.mode != SchematicMode.SAVE) {
            return false;
        }
        BlockPos pos = this.getPos();
        int searchLimit = CAP + 30;
        BlockPos startPos = new BlockPos(pos.getX() - searchLimit, 0, pos.getZ() - searchLimit);
        BlockPos endPos = new BlockPos(pos.getX() + searchLimit, 255, pos.getZ() + searchLimit);
        List<TESchematic> nearbyCornerBlocks = this.getNearbySchematicBlocks(startPos, endPos);
        List<TESchematic> relatedCornerBlocks = this.filterRelatedCornerBlocks(nearbyCornerBlocks);
        if (relatedCornerBlocks.size() < 1) {
            return false;
        }
        MutableBoundingBox boundingBox = calculateEnclosingBoundingBox(pos, relatedCornerBlocks);
        if (boundingBox.maxX - boundingBox.minX > 1 && boundingBox.maxY - boundingBox.minY > 1 && boundingBox.maxZ - boundingBox.minZ > 1) {
            this.schematicPos = new BlockPos(boundingBox.minX - pos.getX() + 1, boundingBox.minY - pos.getY() + 1, boundingBox.minZ - pos.getZ() + 1);
            this.size = new BlockPos(boundingBox.maxX - boundingBox.minX - 1,
                                     boundingBox.maxY - boundingBox.minY - 1,
                                     boundingBox.maxZ - boundingBox.minZ - 1);
            this.markDirty();
            BlockState state = this.world.getBlockState(pos);
            this.world.notifyBlockUpdate(pos, state, state, 3);
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

    public void setIntegrity(float integrityIn) {
        this.integrity = integrityIn;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 16_384;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror mirrorIn) {
        this.mirror = mirrorIn;
    }

    public SchematicMode getMode() {
        return this.mode;
    }

    public void setMode(SchematicMode modeIn) {
        this.mode = modeIn;
        BlockState blockstate = this.world.getBlockState(this.getPos());
        if (blockstate.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
            this.world.setBlockState(this.getPos(), blockstate.with(BlockSchematic.MODE, modeIn), 2);
        }
    }

    public String getName() {
        return this.name == null ? "" : this.name.toString();
    }

    public void setName(@Nullable String nameIn) {
        this.name = StringUtils.isNullOrEmpty(nameIn) ? null : ResourceLocation.tryCreate(nameIn);
    }

    public void setName(@Nullable ResourceLocation p_210163_1_) {
        this.name = p_210163_1_;
    }

    private List<TESchematic> getNearbySchematicBlocks(BlockPos startPos, BlockPos endPos) {
        List<TESchematic> schematicBlocks = Lists.newArrayList();
        for (BlockPos pos : BlockPos.getAllInBoxMutable(startPos, endPos)) {
            BlockState state = this.world.getBlockState(pos);
            if (state.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
                TileEntity tile = this.world.getTileEntity(pos);
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

    public void setRotation(Rotation rotationIn) {
        this.rotation = rotationIn;
    }

    public BlockPos getSchematicPos() {
        return this.schematicPos;
    }

    public void setSchematicPos(BlockPos posIn) {
        this.schematicPos = posIn;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seedIn) {
        this.seed = seedIn;
    }

    public BlockPos getStructureSize() {
        return this.size;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 7, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    public boolean hasName() {
        return this.name != null;
    }

    public boolean ignoresEntities() {
        return this.ignoreEntities;
    }

    public boolean isStructureLoadable() {
        if (this.mode == SchematicMode.LOAD && !this.world.isRemote && this.name != null) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            TemplateManager templateManager = serverWorld.getStructureTemplateManager();
            try {
                return templateManager.getTemplate(this.name) != null;
            }
            catch (ResourceLocationException exception) {
                return false;
            }
        }
        return false;
    }

    public boolean load() {
        return this.load(true);
    }

    public boolean load(boolean requireMatchingSize) {
        if (this.mode == SchematicMode.LOAD && !this.world.isRemote && this.name != null) {
            BlockPos tilePos = this.getPos();
            BlockPos schematicPos = tilePos.add(this.schematicPos);
            ServerWorld serverWorld = (ServerWorld) this.world;
            TemplateManager templateManager = serverWorld.getStructureTemplateManager();
            Template template;
            try {
                template = templateManager.getTemplate(this.name);
            }
            catch (ResourceLocationException exception) {
                return false;
            }
            if (template == null) {
                return false;
            }
            BlockPos size = template.getSize();
            boolean sizeMatches = this.size.equals(size);
            if (!sizeMatches) {
                this.size = size;
                this.markDirty();
                BlockState state = this.world.getBlockState(tilePos);
                this.world.notifyBlockUpdate(tilePos, state, state, 3);
            }
            if (requireMatchingSize && !sizeMatches) {
                return false;
            }
            //noinspection ConstantConditions
            PlacementSettings placementSettings = new PlacementSettings().setMirror(this.mirror)
                                                                         .setRotation(this.rotation)
                                                                         .setIgnoreEntities(this.ignoreEntities)
                                                                         .setChunk(null);
            if (this.integrity < 1.0F) {
                placementSettings.clearProcessors()
                                 .addProcessor(new IntegrityProcessor(MathHelper.clamp(this.integrity, 0.0F, 1.0F)))
                                 .setRandom(createRandom(this.seed));
            }
            template.addBlocksToWorldChunk(this.world, schematicPos, placementSettings);
            return true;
        }
        return false;
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
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.setName(compound.getString("name"));
        int posX = MathHelper.clamp(compound.getInt("posX"), -CAP, CAP);
        int posY = MathHelper.clamp(compound.getInt("posY"), -CAP, CAP);
        int posZ = MathHelper.clamp(compound.getInt("posZ"), -CAP, CAP);
        this.schematicPos = new BlockPos(posX, posY, posZ);
        int sizeX = MathHelper.clamp(compound.getInt("sizeX"), 0, CAP);
        int sizeY = MathHelper.clamp(compound.getInt("sizeY"), 0, CAP);
        int sizeZ = MathHelper.clamp(compound.getInt("sizeZ"), 0, CAP);
        this.size = new BlockPos(sizeX, sizeY, sizeZ);
        try {
            this.rotation = Rotation.valueOf(compound.getString("rotation"));
        }
        catch (IllegalArgumentException exception) {
            this.rotation = Rotation.NONE;
        }
        try {
            this.mirror = Mirror.valueOf(compound.getString("mirror"));
        }
        catch (IllegalArgumentException exception) {
            this.mirror = Mirror.NONE;
        }
        this.mode = SchematicMode.byId(compound.getByte("mode"));
        this.ignoreEntities = compound.getBoolean("ignoreEntities");
        this.showAir = compound.getBoolean("showair");
        this.showBoundingBox = compound.getBoolean("showboundingbox");
        if (compound.contains("integrity")) {
            this.integrity = compound.getFloat("integrity");
        }
        else {
            this.integrity = 1.0F;
        }
        this.seed = compound.getLong("seed");
        this.updateBlockState();
    }

    public boolean save() {
        return this.save(true);
    }

    public boolean save(boolean writeToDisk) {
        if (this.mode == SchematicMode.SAVE && !this.world.isRemote && this.name != null) {
            BlockPos pos = this.getPos().add(this.schematicPos);
            ServerWorld serverWorld = (ServerWorld) this.world;
            TemplateManager templateManager = serverWorld.getStructureTemplateManager();
            Template template;
            try {
                template = templateManager.getTemplateDefaulted(this.name);
            }
            catch (ResourceLocationException exception) {
                return false;
            }
            template.takeBlocksFromWorld(this.world, pos, this.size, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
            if (writeToDisk) {
                try {
                    return templateManager.writeToFile(this.name);
                }
                catch (ResourceLocationException exception) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void setIgnoresEntities(boolean ignoreEntitiesIn) {
        this.ignoreEntities = ignoreEntitiesIn;
    }

    public void setShowAir(boolean showAirIn) {
        this.showAir = showAirIn;
    }

    public void setShowBoundingBox(boolean showBoundingBoxIn) {
        this.showBoundingBox = showBoundingBoxIn;
    }

    public void setSize(BlockPos sizeIn) {
        this.size = sizeIn;
    }

    public boolean showsAir() {
        return this.showAir;
    }

    public boolean showsBoundingBox() {
        return this.showBoundingBox;
    }

    private void updateBlockState() {
        if (this.world != null) {
            BlockPos pos = this.getPos();
            BlockState state = this.world.getBlockState(pos);
            if (state.getBlock() == EvolutionBlocks.SCHEMATIC_BLOCK.get()) {
                this.world.setBlockState(pos, state.with(BlockSchematic.MODE, this.mode), 2);
            }
        }
    }

    public boolean usedBy(PlayerEntity player) {
        if (!player.canUseCommandBlock()) {
            return false;
        }
        if (player.getEntityWorld().isRemote) {
            ScreenSchematic.open(this);
        }
        return true;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putString("name", this.getName());
        compound.putInt("posX", this.schematicPos.getX());
        compound.putInt("posY", this.schematicPos.getY());
        compound.putInt("posZ", this.schematicPos.getZ());
        compound.putInt("sizeX", this.size.getX());
        compound.putInt("sizeY", this.size.getY());
        compound.putInt("sizeZ", this.size.getZ());
        compound.putString("rotation", this.rotation.toString());
        compound.putString("mirror", this.mirror.toString());
        compound.putByte("mode", this.mode.getId());
        compound.putBoolean("ignoreEntities", this.ignoreEntities);
        compound.putBoolean("showair", this.showAir);
        compound.putBoolean("showboundingbox", this.showBoundingBox);
        compound.putFloat("integrity", this.integrity);
        compound.putLong("seed", this.seed);
        return compound;
    }

    public enum UpdateCommand {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA
    }
}