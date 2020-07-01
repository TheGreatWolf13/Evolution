package tgw.evolution.world.puzzle;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.world.puzzle.pieces.EmptyPuzzlePiece;

import java.util.List;
import java.util.Random;

public abstract class StructurePuzzlePiece extends StructurePiece {

    protected final PuzzlePiece puzzlePiece;
    protected final Rotation rotation;
    private final int groundLevelDelta;
    private final List<PuzzleJunction> junctions = Lists.newArrayList();
    private final TemplateManager templateManager;
    protected BlockPos pos;

    public StructurePuzzlePiece(IStructurePieceType pieceType, TemplateManager manager, PuzzlePiece puzzlePiece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox boundingBox) {
        super(pieceType, 0);
        this.templateManager = manager;
        this.puzzlePiece = puzzlePiece;
        this.pos = pos;
        this.groundLevelDelta = groundLevelDelta;
        this.rotation = rotation;
        this.boundingBox = boundingBox;
    }

    public StructurePuzzlePiece(TemplateManager manager, CompoundNBT nbt, IStructurePieceType pieceType) {
        super(pieceType, nbt);
        this.templateManager = manager;
        this.pos = new BlockPos(nbt.getInt("PosX"), nbt.getInt("PosY"), nbt.getInt("PosZ"));
        this.groundLevelDelta = nbt.getInt("ground_level_delta");
        this.puzzlePiece = PuzzleDeserializerHelper.deserialize(nbt.getCompound("pool_element"), "element_type", EmptyPuzzlePiece.INSTANCE);
        this.rotation = Rotation.valueOf(nbt.getString("rotation"));
        this.boundingBox = this.puzzlePiece.getBoundingBox(manager, this.pos, this.rotation);
        ListNBT listnbt = nbt.getList("junctions", 10);
        this.junctions.clear();
        listnbt.forEach(p_214827_1_ -> this.junctions.add(PuzzleJunction.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, p_214827_1_))));
    }

    @Override
    protected void readAdditional(CompoundNBT tagCompound) {
        tagCompound.putInt("PosX", this.pos.getX());
        tagCompound.putInt("PosY", this.pos.getY());
        tagCompound.putInt("PosZ", this.pos.getZ());
        tagCompound.putInt("ground_level_delta", this.groundLevelDelta);
        tagCompound.put("pool_element", this.puzzlePiece.serialize());
        tagCompound.putString("rotation", this.rotation.name());
        ListNBT junctionList = new ListNBT();
        for (PuzzleJunction puzzleJunction : this.junctions) {
            junctionList.add(puzzleJunction.serialize(NBTDynamicOps.INSTANCE).getValue());
        }
        tagCompound.put("junctions", junctionList);
    }

    @Override
    public boolean addComponentParts(IWorld world, Random random, MutableBoundingBox boundingBox, ChunkPos chunkPos) {
        return this.puzzlePiece.place(this.templateManager, world, this.pos, this.rotation, boundingBox, random);
    }

    @Override
    public void offset(int x, int y, int z) {
        super.offset(x, y, z);
        this.pos = this.pos.add(x, y, z);
    }

    @Override
    public Rotation getRotation() {
        return this.rotation;
    }

    @Override
    public String toString() {
        return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.pos, this.rotation, this.puzzlePiece);
    }

    public PuzzlePiece getPuzzlePiece() {
        return this.puzzlePiece;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(PuzzleJunction junction) {
        this.junctions.add(junction);
    }

    public List<PuzzleJunction> getJunctions() {
        return this.junctions;
    }
}
