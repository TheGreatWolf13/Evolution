package tgw.evolution.util.constants;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMaps;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.IVariant;
import tgw.evolution.util.UnregisteredFeatureException;

public enum WoodVariant implements IVariant {
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
    private static final Byte2ReferenceMap<WoodVariant> REGISTRY;

    static {
        Byte2ReferenceMap<WoodVariant> map = new Byte2ReferenceOpenHashMap<>();
        for (WoodVariant variant : VALUES) {
            map.put(variant.id, variant);
        }
        REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
    }

    private final int density;
    private final byte id;
    private final String name;
    private final int shearStrength;

    WoodVariant(int id, String name, int density, int shearStrength) {
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

    public Block getChoppingBlock() {
        return EvolutionBlocks.ALL_CHOPPING_BLOCK.get(this).get();
    }

    public Item getFirewood() {
        return EvolutionItems.ALL_FIREWOOD.get(this).get();
    }

    public byte getId() {
        return this.id;
    }

    public Block getLeaves() {
        return EvolutionBlocks.ALL_LEAVES.get(this).get();
    }

    public Block getLog() {
        return EvolutionBlocks.ALL_LOG.get(this).get();
    }

    public Item getLogItem() {
        return EvolutionItems.ALL_LOG.get(this).get();
    }

    public int getMass() {
        return this.density;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Item getPlank() {
        return EvolutionItems.ALL_PLANK.get(this).get();
    }

    public Block getPlanks() {
        return EvolutionBlocks.ALL_PLANKS.get(this).get();
    }

    public Block getSapling() {
        return EvolutionBlocks.ALL_SAPLING.get(this).get();
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
