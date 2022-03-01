package tgw.evolution.util.constants;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.UnregisteredFeatureException;

public enum WoodVariant {
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
        return switch (id) {
            case 0 -> ACACIA;
            case 1 -> ASPEN;
            case 2 -> BIRCH;
            case 3 -> CEDAR;
            case 4 -> EBONY;
            case 5 -> ELM;
            case 6 -> EUCALYPTUS;
            case 7 -> FIR;
            case 8 -> KAPOK;
            case 9 -> MANGROVE;
            case 10 -> MAPLE;
            case 11 -> OAK;
            case 12 -> OLD_OAK;
            case 13 -> PALM;
            case 14 -> PINE;
            case 15 -> REDWOOD;
            case 16 -> SPRUCE;
            case 17 -> WILLOW;
            default -> throw new UnregisteredFeatureException("Unregistered variant for id: " + id);
        };
    }

    public Block getChoppingBlock() {
        return switch (this) {
            case ACACIA -> EvolutionBlocks.CHOPPING_BLOCK_ACACIA.get();
            case ASPEN -> EvolutionBlocks.CHOPPING_BLOCK_ASPEN.get();
            case BIRCH -> EvolutionBlocks.CHOPPING_BLOCK_BIRCH.get();
            case CEDAR -> EvolutionBlocks.CHOPPING_BLOCK_CEDAR.get();
            case EBONY -> EvolutionBlocks.CHOPPING_BLOCK_EBONY.get();
            case ELM -> EvolutionBlocks.CHOPPING_BLOCK_ELM.get();
            case EUCALYPTUS -> EvolutionBlocks.CHOPPING_BLOCK_EUCALYPTUS.get();
            case FIR -> EvolutionBlocks.CHOPPING_BLOCK_FIR.get();
            case KAPOK -> EvolutionBlocks.CHOPPING_BLOCK_KAPOK.get();
            case MANGROVE -> EvolutionBlocks.CHOPPING_BLOCK_MANGROVE.get();
            case MAPLE -> EvolutionBlocks.CHOPPING_BLOCK_MAPLE.get();
            case OAK -> EvolutionBlocks.CHOPPING_BLOCK_OAK.get();
            case OLD_OAK -> EvolutionBlocks.CHOPPING_BLOCK_OLD_OAK.get();
            case PALM -> EvolutionBlocks.CHOPPING_BLOCK_PALM.get();
            case PINE -> EvolutionBlocks.CHOPPING_BLOCK_PINE.get();
            case REDWOOD -> EvolutionBlocks.CHOPPING_BLOCK_REDWOOD.get();
            case SPRUCE -> EvolutionBlocks.CHOPPING_BLOCK_SPRUCE.get();
            case WILLOW -> EvolutionBlocks.CHOPPING_BLOCK_WILLOW.get();
        };
    }

    public Item getFirewood() {
        return switch (this) {
            case ACACIA -> EvolutionItems.firewood_acacia.get();
            case ASPEN -> EvolutionItems.firewood_aspen.get();
            case BIRCH -> EvolutionItems.firewood_birch.get();
            case CEDAR -> EvolutionItems.firewood_cedar.get();
            case EBONY -> EvolutionItems.firewood_ebony.get();
            case ELM -> EvolutionItems.firewood_elm.get();
            case EUCALYPTUS -> EvolutionItems.firewood_eucalyptus.get();
            case FIR -> EvolutionItems.firewood_fir.get();
            case KAPOK -> EvolutionItems.firewood_kapok.get();
            case MANGROVE -> EvolutionItems.firewood_mangrove.get();
            case MAPLE -> EvolutionItems.firewood_maple.get();
            case OAK -> EvolutionItems.firewood_oak.get();
            case OLD_OAK -> EvolutionItems.firewood_old_oak.get();
            case PALM -> EvolutionItems.firewood_palm.get();
            case PINE -> EvolutionItems.firewood_pine.get();
            case REDWOOD -> EvolutionItems.firewood_redwood.get();
            case SPRUCE -> EvolutionItems.firewood_spruce.get();
            case WILLOW -> EvolutionItems.firewood_willow.get();
        };
    }

    public byte getId() {
        return this.id;
    }

    public Block getLeaves() {
        return switch (this) {
            case ACACIA -> EvolutionBlocks.LEAVES_ACACIA.get();
            case ASPEN -> EvolutionBlocks.LEAVES_ASPEN.get();
            case BIRCH -> EvolutionBlocks.LEAVES_BIRCH.get();
            case CEDAR -> EvolutionBlocks.LEAVES_CEDAR.get();
            case EBONY -> EvolutionBlocks.LEAVES_EBONY.get();
            case ELM -> EvolutionBlocks.LEAVES_ELM.get();
            case EUCALYPTUS -> EvolutionBlocks.LEAVES_EUCALYPTUS.get();
            case FIR -> EvolutionBlocks.LEAVES_FIR.get();
            case KAPOK -> EvolutionBlocks.LEAVES_KAPOK.get();
            case MANGROVE -> EvolutionBlocks.LEAVES_MANGROVE.get();
            case MAPLE -> EvolutionBlocks.LEAVES_MAPLE.get();
            case OAK -> EvolutionBlocks.LEAVES_OAK.get();
            case OLD_OAK -> EvolutionBlocks.LEAVES_OLD_OAK.get();
            case PALM -> EvolutionBlocks.LEAVES_PALM.get();
            case PINE -> EvolutionBlocks.LEAVES_PINE.get();
            case REDWOOD -> EvolutionBlocks.LEAVES_REDWOOD.get();
            case SPRUCE -> EvolutionBlocks.LEAVES_SPRUCE.get();
            case WILLOW -> EvolutionBlocks.LEAVES_WILLOW.get();
        };
    }

    public Block getLog() {
        return switch (this) {
            case ACACIA -> EvolutionBlocks.LOG_ACACIA.get();
            case ASPEN -> EvolutionBlocks.LOG_ASPEN.get();
            case BIRCH -> EvolutionBlocks.LOG_BIRCH.get();
            case CEDAR -> EvolutionBlocks.LOG_CEDAR.get();
            case EBONY -> EvolutionBlocks.LOG_EBONY.get();
            case ELM -> EvolutionBlocks.LOG_ELM.get();
            case EUCALYPTUS -> EvolutionBlocks.LOG_EUCALYPTUS.get();
            case FIR -> EvolutionBlocks.LOG_FIR.get();
            case KAPOK -> EvolutionBlocks.LOG_KAPOK.get();
            case MANGROVE -> EvolutionBlocks.LOG_MANGROVE.get();
            case MAPLE -> EvolutionBlocks.LOG_MAPLE.get();
            case OAK -> EvolutionBlocks.LOG_OAK.get();
            case OLD_OAK -> EvolutionBlocks.LOG_OLD_OAK.get();
            case PALM -> EvolutionBlocks.LOG_PALM.get();
            case PINE -> EvolutionBlocks.LOG_PINE.get();
            case REDWOOD -> EvolutionBlocks.LOG_REDWOOD.get();
            case SPRUCE -> EvolutionBlocks.LOG_SPRUCE.get();
            case WILLOW -> EvolutionBlocks.LOG_WILLOW.get();
        };
    }

    public Item getLogItem() {
        return switch (this) {
            case ACACIA -> EvolutionItems.log_acacia.get();
            case ASPEN -> EvolutionItems.log_aspen.get();
            case BIRCH -> EvolutionItems.log_birch.get();
            case CEDAR -> EvolutionItems.log_cedar.get();
            case EBONY -> EvolutionItems.log_ebony.get();
            case ELM -> EvolutionItems.log_elm.get();
            case EUCALYPTUS -> EvolutionItems.log_eucalyptus.get();
            case FIR -> EvolutionItems.log_fir.get();
            case KAPOK -> EvolutionItems.log_kapok.get();
            case MANGROVE -> EvolutionItems.log_mangrove.get();
            case MAPLE -> EvolutionItems.log_maple.get();
            case OAK -> EvolutionItems.log_oak.get();
            case OLD_OAK -> EvolutionItems.log_old_oak.get();
            case PALM -> EvolutionItems.log_palm.get();
            case PINE -> EvolutionItems.log_pine.get();
            case REDWOOD -> EvolutionItems.log_redwood.get();
            case SPRUCE -> EvolutionItems.log_spruce.get();
            case WILLOW -> EvolutionItems.log_willow.get();
        };
    }

    public int getMass() {
        return this.density;
    }

    public String getName() {
        return this.name;
    }

    public Item getPlank() {
        return switch (this) {
            case ACACIA -> EvolutionItems.plank_acacia.get();
            case ASPEN -> EvolutionItems.plank_aspen.get();
            case BIRCH -> EvolutionItems.plank_birch.get();
            case CEDAR -> EvolutionItems.plank_cedar.get();
            case EBONY -> EvolutionItems.plank_ebony.get();
            case ELM -> EvolutionItems.plank_elm.get();
            case EUCALYPTUS -> EvolutionItems.plank_eucalyptus.get();
            case FIR -> EvolutionItems.plank_fir.get();
            case KAPOK -> EvolutionItems.plank_kapok.get();
            case MANGROVE -> EvolutionItems.plank_mangrove.get();
            case MAPLE -> EvolutionItems.plank_maple.get();
            case OAK -> EvolutionItems.plank_oak.get();
            case OLD_OAK -> EvolutionItems.plank_old_oak.get();
            case PALM -> EvolutionItems.plank_palm.get();
            case PINE -> EvolutionItems.plank_pine.get();
            case REDWOOD -> EvolutionItems.plank_redwood.get();
            case SPRUCE -> EvolutionItems.plank_spruce.get();
            case WILLOW -> EvolutionItems.plank_willow.get();
        };
    }

    public Block getPlanks() {
        return switch (this) {
            case ACACIA -> EvolutionBlocks.PLANKS_ACACIA.get();
            case ASPEN -> EvolutionBlocks.PLANKS_ASPEN.get();
            case BIRCH -> EvolutionBlocks.PLANKS_BIRCH.get();
            case CEDAR -> EvolutionBlocks.PLANKS_CEDAR.get();
            case EBONY -> EvolutionBlocks.PLANKS_EBONY.get();
            case ELM -> EvolutionBlocks.PLANKS_ELM.get();
            case EUCALYPTUS -> EvolutionBlocks.PLANKS_EUCALYPTUS.get();
            case FIR -> EvolutionBlocks.PLANKS_FIR.get();
            case KAPOK -> EvolutionBlocks.PLANKS_KAPOK.get();
            case MANGROVE -> EvolutionBlocks.PLANKS_MANGROVE.get();
            case MAPLE -> EvolutionBlocks.PLANKS_MAPLE.get();
            case OAK -> EvolutionBlocks.PLANKS_OAK.get();
            case OLD_OAK -> EvolutionBlocks.PLANKS_OLD_OAK.get();
            case PALM -> EvolutionBlocks.PLANKS_PALM.get();
            case PINE -> EvolutionBlocks.PLANKS_PINE.get();
            case REDWOOD -> EvolutionBlocks.PLANKS_REDWOOD.get();
            case SPRUCE -> EvolutionBlocks.PLANKS_SPRUCE.get();
            case WILLOW -> EvolutionBlocks.PLANKS_WILLOW.get();
        };
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
