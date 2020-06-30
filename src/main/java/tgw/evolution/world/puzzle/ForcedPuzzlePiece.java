package tgw.evolution.world.puzzle;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import tgw.evolution.util.NBTHelper;

import java.util.List;

public class ForcedPuzzlePiece extends SinglePuzzlePiece {

    private final ForceType forceType;

    public ForcedPuzzlePiece(INBT nbt) {
        super(nbt);
        this.forceType = ForceType.byName(NBTHelper.asString(nbt, "force_type", "soft"));
    }

    public ForcedPuzzlePiece(String location, List<StructureProcessor> processors, ForceType forceType) {
        this(location, processors, PuzzlePattern.PlacementBehaviour.RIGID, forceType);
    }

    public ForcedPuzzlePiece(String location, List<StructureProcessor> processors, PuzzlePattern.PlacementBehaviour placementBehaviour, ForceType forceType) {
        super(location, processors, placementBehaviour);
        this.forceType = forceType;
    }

    public ForcedPuzzlePiece(String location, ForceType forceType) {
        this(location, ImmutableList.of(), forceType);
    }

    public ForceType getForceType() {
        return this.forceType;
    }

    @Override
    public EnumPuzzleType getType() {
        return EnumPuzzleType.FORCED;
    }

    @Override
    public INBT serialize0() {
        return NBTHelper.mergeInto(super.serialize0(), new StringNBT("force_type"), new StringNBT(this.forceType.getName()));
    }

    @Override
    public String toString() {
        return "ForcedPuzzlePiece[" + this.location + ", FORCETYPE=" + this.forceType + "]";
    }

    public enum ForceType {
        HARD("hard"),
        SOFT("soft");

        private final String name;

        ForceType(String name) {
            this.name = name;
        }

        public static ForceType byName(String name) {
            for (ForceType type : ForceType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            throw new IllegalStateException("Unknown ForceType " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}
