package tgw.evolution.world.puzzle;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import tgw.evolution.util.NBTHelper;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ListPuzzlePiece extends PuzzlePiece {

    private final List<PuzzlePiece> elements;

    public ListPuzzlePiece(List<PuzzlePiece> elementList) {
        this(elementList, PuzzlePattern.PlacementBehaviour.RIGID);
    }

    public ListPuzzlePiece(List<PuzzlePiece> elementList, PuzzlePattern.PlacementBehaviour placementBehaviour) {
        super(placementBehaviour);
        if (elementList.isEmpty()) {
            throw new IllegalArgumentException("Elements are empty");
        }
        this.elements = elementList;
        this.setChildrenPlacementBehaviour(placementBehaviour);
    }

    public ListPuzzlePiece(INBT nbt) {
        super(nbt);
        List<PuzzlePiece> list = NBTHelper.asList(nbt, "elements", inbt -> PuzzleDeserializerHelper.deserialize(inbt, "element_type", EmptyPuzzlePiece.INSTANCE));
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Elements are empty");
        }
        this.elements = list;
    }

    @Override
    public List<Template.BlockInfo> getPuzzleBlocks(TemplateManager manager, BlockPos pos, Rotation rotation, Random rand) {
        return this.elements.get(0).getPuzzleBlocks(manager, pos, rotation, rand);
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager manager, BlockPos pos, Rotation rotation) {
        MutableBoundingBox boundingBox = MutableBoundingBox.getNewBoundingBox();
        for (PuzzlePiece puzzlePiece : this.elements) {
            MutableBoundingBox individualBoundingBox = puzzlePiece.getBoundingBox(manager, pos, rotation);
            boundingBox.expandTo(individualBoundingBox);
        }
        return boundingBox;
    }

    @Override
    public boolean place(TemplateManager manager, IWorld world, BlockPos pos, Rotation rotation, MutableBoundingBox boundingBox, Random rand) {
        for (PuzzlePiece puzzlePiece : this.elements) {
            if (!puzzlePiece.place(manager, world, pos, rotation, boundingBox, rand)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public EnumPuzzleType getType() {
        return EnumPuzzleType.LIST;
    }

    @Override
    public PuzzlePiece setPlacementBehaviour(PuzzlePattern.PlacementBehaviour placementBehaviour) {
        super.setPlacementBehaviour(placementBehaviour);
        this.setChildrenPlacementBehaviour(placementBehaviour);
        return this;
    }

    @Override
    public INBT serialize0() {
        INBT nbt = NBTHelper.createList(this.elements.stream().map(PuzzlePiece::serialize));
        return NBTHelper.createMap(ImmutableMap.of(new StringNBT("elements"), nbt));
    }

    @Override
    public String toString() {
        return "List[" + this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    private void setChildrenPlacementBehaviour(PuzzlePattern.PlacementBehaviour placementBehaviour) {
        this.elements.forEach(puzzlePiece -> puzzlePiece.setPlacementBehaviour(placementBehaviour));
    }
}
