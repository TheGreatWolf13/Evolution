package tgw.evolution.world.puzzle.pieces;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.world.puzzle.EnumPuzzleType;
import tgw.evolution.world.puzzle.pieces.config.CaveConfigPuzzle;
import tgw.evolution.world.puzzle.pieces.config.CivilizationType;

public class CavePuzzlePiece extends ConfiguredPuzzlePiece {

    private final float danger;
    private final float civilization;
    private final CivilizationType civType;

    public CavePuzzlePiece(String location, CaveConfigPuzzle config) {
        super(location, config);
        this.danger = config.getDanger();
        this.civilization = config.getCivilization();
        this.civType = config.getCivType();
    }

    public CavePuzzlePiece(INBT nbt) {
        super(nbt);
        this.danger = NBTHelper.asFloat(nbt, "Danger", 0);
        this.civilization = NBTHelper.asFloat(nbt, "Civ", 0);
        this.civType = CivilizationType.byId(NBTHelper.asByte(nbt, "CivType", 0));
    }

    @Override
    public EnumPuzzleType getType() {
        return EnumPuzzleType.CAVE;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CavePuzzlePiece)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CavePuzzlePiece that = (CavePuzzlePiece) o;
        return this.civType == that.civType && Float.compare(that.danger, this.danger) == 0 && Float.compare(that.civilization, this.civilization) == 0;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 31 + Float.floatToIntBits(this.danger);
        hash = hash * 31 + Float.floatToIntBits(this.civilization);
        hash = hash * 31 + this.civType.hashCode();
        return hash;
    }

    @Override
    public INBT serialize0() {
        INBT map = NBTHelper.createMap(ImmutableMap.of(new StringNBT("Danger"), new FloatNBT(this.danger), new StringNBT("Civ"), new FloatNBT(this.civilization), new StringNBT("CivType"), new ByteNBT(this.civType.getId())));
        return NBTHelper.mergeInto(super.serialize0(), map);
    }
}
