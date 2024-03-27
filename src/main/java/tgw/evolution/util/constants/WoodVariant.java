package tgw.evolution.util.constants;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.init.IVariant;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.B2OHashMap;
import tgw.evolution.util.collection.maps.B2OMap;

import java.util.Map;

public enum WoodVariant implements IVariant<WoodVariant> {
    ACACIA(0, "acacia", 750, 14_412_500),
    ASPEN(1, "aspen", 420, 6_650_000),
    BIRCH(2, "birch", 640, 12_233_333),
    CEDAR(3, "cedar", 530, 6_916_667),
    EBONY(4, "ebony", 1_220, 16_250_000),
    ELM(5, "elm", 570, 11_600_000),
    EUCALYPTUS(6, "eucalyptus", 490, 10_500_000),
    FIR(7, "fir", 633, 7_214_286),
    KAPOK(8, "kapok", 230, 8_500_000),
    MANGROVE(9, "mangrove", 485, 5_750_000),
    MAPLE(10, "maple", 685, 12_800_000),
    OAK(11, "oak", 750, 13_612_500),
    OLD_OAK(12, "old_oak", 830, 12_836_400),
    PALM(13, "palm", 500, 5_000_000),
    PINE(14, "pine", 840, 8_753_333),
    REDWOOD(15, "redwood", 480, 7_050_000),
    SPRUCE(16, "spruce", 440, 8_100_000),
    WILLOW(17, "willow", 500, 7_850_000);

    public static final WoodVariant[] VALUES = values();
    private static final B2OMap<WoodVariant> REGISTRY;
    private static final OList<Map<WoodVariant, ? extends Block>> BLOCKS = new OArrayList<>();

    static {
        B2OMap<WoodVariant> map = new B2OHashMap<>();
        for (WoodVariant variant : VALUES) {
            if (map.put(variant.id, variant) != null) {
                throw new IllegalStateException("WoodVariant " + variant + " has duplicate id: " + variant.id);
            }
        }
        map.trim();
        REGISTRY = map.view();
    }

    private final double density;
    private final byte id;
    private final String name;
    private final int shearStrength;

    WoodVariant(int id, String name, double density, int shearStrength) {
        this.id = (byte) id;
        this.name = name;
        this.shearStrength = shearStrength;
        this.density = density;
    }

    public static WoodVariant byId(byte id) {
        WoodVariant variant = REGISTRY.get(id);
        if (variant == null) {
            throw new UnregisteredFeatureException("Unregistered variant for id: " + id);
        }
        return variant;
    }

    @Override
    public @UnmodifiableView OList<Map<WoodVariant, ? extends Block>> getBlocks() {
        return BLOCKS.view();
    }

    public byte getId() {
        return this.id;
    }

    public double getMass() {
        return this.density;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public int getShearStrength() {
        return this.shearStrength;
    }

    @Override
    public void registerBlocks(Map<WoodVariant, ? extends Block> blocks) {
        BLOCKS.add(blocks);
    }
}
