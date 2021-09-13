//package tgw.evolution.world.puzzle.pieces;
//
//import com.google.common.collect.ImmutableMap;
//import net.minecraft.nbt.ByteNBT;
//import net.minecraft.nbt.INBT;
//import net.minecraft.nbt.ShortNBT;
//import net.minecraft.nbt.StringNBT;
//import tgw.evolution.util.NBTHelper;
//import tgw.evolution.world.feature.structures.config.IConfigStruct;
//import tgw.evolution.world.puzzle.EnumPuzzleType;
//import tgw.evolution.world.puzzle.pieces.config.ConfigPuzzle;
//import tgw.evolution.world.puzzle.pieces.config.ForceType;
//
//public class ConfiguredPuzzlePiece extends SinglePuzzlePiece {
//
//    private final short desiredHeight;
//    private final ForceType forceType;
//    private final byte maxDeviation;
//    private final boolean underground;
//
//    public ConfiguredPuzzlePiece(String location, ConfigPuzzle<?> config) {
//        super(location);
//        this.underground = config.isUnderground();
//        this.forceType = config.getForceType();
//        this.desiredHeight = config.getDesiredHeight();
//        this.maxDeviation = config.getMaxDeviation();
//    }
//
//    public ConfiguredPuzzlePiece(INBT nbt) {
//        super(nbt);
//        this.underground = NBTHelper.asBoolean(nbt, "Und", false);
//        this.forceType = ForceType.byId(NBTHelper.asByte(nbt, "Force", 0));
//        this.desiredHeight = NBTHelper.asShort(nbt, "Height", -1);
//        this.maxDeviation = NBTHelper.asByte(nbt, "Deviation", 0);
//    }
//
//    /**
//     * Additional conditions for this piece to spawn.
//     *
//     * @return Whether or not this piece should spawn.
//     */
//    public boolean childConditions(IConfigStruct config) {
//        return true;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof ConfiguredPuzzlePiece)) {
//            return false;
//        }
//        if (!super.equals(o)) {
//            return false;
//        }
//        ConfiguredPuzzlePiece that = (ConfiguredPuzzlePiece) o;
//        return this.underground == that.underground &&
//               this.desiredHeight == that.desiredHeight &&
//               this.maxDeviation == that.maxDeviation &&
//               this.forceType == that.forceType;
//    }
//
//    public short getDesiredHeight() {
//        return this.desiredHeight;
//    }
//
//    public ForceType getForceType() {
//        return this.forceType;
//    }
//
//    public byte getMaxDeviation() {
//        return this.maxDeviation;
//    }
//
//    @Override
//    public EnumPuzzleType getType() {
//        return EnumPuzzleType.CONFIGURED;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = super.hashCode();
//        hash = 31 * hash + (this.underground ? 1 : 0);
//        hash = 31 * hash + this.forceType.hashCode();
//        hash = 31 * hash + this.desiredHeight;
//        hash = 31 * hash + this.maxDeviation;
//        return hash;
//    }
//
//    public boolean isUnderground() {
//        return this.underground;
//    }
//
//    @Override
//    public INBT serialize0() {
//        INBT map = NBTHelper.createMap(ImmutableMap.of(StringNBT.valueOf("Und"),
//                                                       ByteNBT.valueOf((byte) (this.underground ? 1 : 0)),
//                                                       StringNBT.valueOf("Force"),
//                                                       ByteNBT.valueOf(this.forceType.getId()),
//                                                       StringNBT.valueOf("Height"),
//                                                       ShortNBT.valueOf(this.desiredHeight),
//                                                       StringNBT.valueOf("Deviation"),
//                                                       ByteNBT.valueOf(this.maxDeviation)));
//        return NBTHelper.mergeInto(super.serialize0(), map);
//    }
//
//    /**
//     * Gets called when this piece is marked for spawn.
//     *
//     * @param config The config of the Structure spawning this piece.
//     */
//    public void success(IConfigStruct config) {
//    }
//}
