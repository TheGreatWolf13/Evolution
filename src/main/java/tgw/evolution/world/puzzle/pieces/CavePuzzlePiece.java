package tgw.evolution.world.puzzle.pieces;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.world.feature.structures.config.ConfigStructCave;
import tgw.evolution.world.feature.structures.config.IConfigStruct;
import tgw.evolution.world.puzzle.EnumPuzzleType;
import tgw.evolution.world.puzzle.pieces.config.CaveConfigPuzzle;
import tgw.evolution.world.puzzle.pieces.config.CivilizationType;

public class CavePuzzlePiece extends ConfiguredPuzzlePiece {

    private final float danger;
    private final boolean isMega;
    private final CivilizationType civType;

    public CavePuzzlePiece(String location, CaveConfigPuzzle config) {
        super(location, config);
        this.danger = config.getDanger();
        this.isMega = config.isMega();
        this.civType = config.getCiv();
    }

    public CavePuzzlePiece(INBT nbt) {
        super(nbt);
        this.danger = NBTHelper.asFloat(nbt, "Danger", -1);
        this.civType = CivilizationType.byId(NBTHelper.asByte(nbt, "CivType", 0));
        this.isMega = NBTHelper.asBoolean(nbt, "Mega", false);
    }

    public boolean isMega() {
        return this.isMega;
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
        return this.isMega == that.isMega && this.civType == that.civType && Float.compare(that.danger, this.danger) == 0;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 31 + (this.isMega ? 1 : 0);
        hash = hash * 31 + Float.floatToIntBits(this.danger);
        hash = hash * 31 + this.civType.hashCode();
        return hash;
    }

    @Override
    public INBT serialize0() {
        INBT map = NBTHelper.createMap(ImmutableMap.of(new StringNBT("Danger"), new FloatNBT(this.danger), new StringNBT("CivType"), new ByteNBT(this.civType.getId()), new StringNBT("Mega"), new ByteNBT((byte) (this.isMega ? 1 : 0))));
        return NBTHelper.mergeInto(super.serialize0(), map);
    }

    @Override
    public boolean childConditions(IConfigStruct config) {
        if (!(config instanceof ConfigStructCave)) {
            throw new IllegalStateException("Invalid Struct Config for Cave Piece " + config);
        }
        ConfigStructCave cave = (ConfigStructCave) config;
        //Test for mega building
        if (this.isMega && (!cave.hasMega() || cave.hasMegaGenerated())) {
            return false;
        }
        //Test for civilization type
        if (this.civType != cave.getCivType()) {
            return false;
        }
        //Test for danger
        if (this.danger == -1) {
            return true;
        }
        float chance = cave.getRandom().nextFloat();
        return !(chance > 1 - Math.abs(this.danger - cave.getDanger()));
    }

    @Override
    public void success(IConfigStruct config) {
        if (!(config instanceof ConfigStructCave)) {
            throw new IllegalStateException("Invalid Struct Config for Cave Piece " + config);
        }
        if (this.isMega) {
            ((ConfigStructCave) config).setMegaGenerated();
        }
    }
}
