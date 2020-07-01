package tgw.evolution.world.puzzle.pieces;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.world.puzzle.EnumPuzzleType;

public class HeightPuzzlePiece extends UndergroundPuzzlePiece {

    private final short desiredHeight;
    private final byte maxDeviation;

    public HeightPuzzlePiece(String location, int desiredHeight, int maxDeviation) {
        super(location);
        this.desiredHeight = MathHelper.toShortExact(desiredHeight);
        this.maxDeviation = MathHelper.toByteExact(maxDeviation);
    }

    public HeightPuzzlePiece(INBT nbt) {
        super(nbt);
        this.desiredHeight = NBTHelper.asShort(nbt, "desiredHeight", 32);
        this.maxDeviation = NBTHelper.asByte(nbt, "maxDeviation", 0);
    }

    public short getDesiredHeight() {
        return this.desiredHeight;
    }

    public byte getMaxDeviation() {
        return this.maxDeviation;
    }

    @Override
    public INBT serialize0() {
        INBT map = NBTHelper.createMap(ImmutableMap.of(new StringNBT("desiredHeight"), new ShortNBT(this.desiredHeight), new StringNBT("maxDeviation"), new ByteNBT(this.maxDeviation)));
        return NBTHelper.mergeInto(super.serialize0(), map);
    }

    @Override
    public EnumPuzzleType getType() {
        return super.getType();
    }
}
