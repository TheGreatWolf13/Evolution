package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;

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
        switch (id) {
            case 0:
                return ACACIA;
            case 1:
                return ASPEN;
            case 2:
                return BIRCH;
            case 3:
                return CEDAR;
            case 4:
                return EBONY;
            case 5:
                return ELM;
            case 6:
                return EUCALYPTUS;
            case 7:
                return FIR;
            case 8:
                return KAPOK;
            case 9:
                return MANGROVE;
            case 10:
                return MAPLE;
            case 11:
                return OAK;
            case 12:
                return OLD_OAK;
            case 13:
                return PALM;
            case 14:
                return PINE;
            case 15:
                return REDWOOD;
            case 16:
                return SPRUCE;
            case 17:
                return WILLOW;
        }
        throw new IllegalStateException("Unknown id: " + id);
    }

    public byte getId() {
        return this.id;
    }

    public Item getLogItem() {
        switch (this) {
            case ACACIA:
                return EvolutionItems.log_acacia.get();
            case ASPEN:
                return EvolutionItems.log_aspen.get();
            case BIRCH:
                return EvolutionItems.log_birch.get();
            case CEDAR:
                return EvolutionItems.log_cedar.get();
            case EBONY:
                return EvolutionItems.log_ebony.get();
            case ELM:
                return EvolutionItems.log_elm.get();
            case EUCALYPTUS:
                return EvolutionItems.log_eucalyptus.get();
            case FIR:
                return EvolutionItems.log_fir.get();
            case KAPOK:
                return EvolutionItems.log_kapok.get();
            case MANGROVE:
                return EvolutionItems.log_mangrove.get();
            case MAPLE:
                return EvolutionItems.log_maple.get();
            case OAK:
                return EvolutionItems.log_oak.get();
            case OLD_OAK:
                return EvolutionItems.log_old_oak.get();
            case PALM:
                return EvolutionItems.log_palm.get();
            case PINE:
                return EvolutionItems.log_pine.get();
            case REDWOOD:
                return EvolutionItems.log_redwood.get();
            case SPRUCE:
                return EvolutionItems.log_spruce.get();
            case WILLOW:
                return EvolutionItems.log_willow.get();
        }
        throw new IllegalStateException("Unknown variant: " + this);
    }

    public int getMass() {
        return this.density;
    }

    public String getName() {
        return this.name;
    }

    public Block getPile() {
        switch (this) {
            case ACACIA:
                return EvolutionBlocks.LOG_PILE_ACACIA.get();
            case ASPEN:
                return EvolutionBlocks.LOG_PILE_ASPEN.get();
            case BIRCH:
                return EvolutionBlocks.LOG_PILE_BIRCH.get();
            case CEDAR:
                return EvolutionBlocks.LOG_PILE_CEDAR.get();
            case EBONY:
                return EvolutionBlocks.LOG_PILE_EBONY.get();
            case ELM:
                return EvolutionBlocks.LOG_PILE_ELM.get();
            case EUCALYPTUS:
                return EvolutionBlocks.LOG_PILE_EUCALYPTUS.get();
            case FIR:
                return EvolutionBlocks.LOG_PILE_FIR.get();
            case KAPOK:
                return EvolutionBlocks.LOG_PILE_KAPOK.get();
            case MANGROVE:
                return EvolutionBlocks.LOG_PILE_MANGROVE.get();
            case MAPLE:
                return EvolutionBlocks.LOG_PILE_MAPLE.get();
            case OAK:
                return EvolutionBlocks.LOG_PILE_OAK.get();
            case OLD_OAK:
                return EvolutionBlocks.LOG_PILE_OLD_OAK.get();
            case PALM:
                return EvolutionBlocks.LOG_PILE_PALM.get();
            case PINE:
                return EvolutionBlocks.LOG_PILE_PINE.get();
            case REDWOOD:
                return EvolutionBlocks.LOG_PILE_REDWOOD.get();
            case SPRUCE:
                return EvolutionBlocks.LOG_PILE_SPRUCE.get();
            case WILLOW:
                return EvolutionBlocks.LOG_PILE_WILLOW.get();
        }
        throw new IllegalStateException("Unknown variant: " + this);
    }

    public Item getPlank() {
        switch (this) {
            case ACACIA:
                return EvolutionItems.plank_acacia.get();
            case ASPEN:
                return EvolutionItems.plank_aspen.get();
            case BIRCH:
                return EvolutionItems.plank_birch.get();
            case CEDAR:
                return EvolutionItems.plank_cedar.get();
            case EBONY:
                return EvolutionItems.plank_ebony.get();
            case ELM:
                return EvolutionItems.plank_elm.get();
            case EUCALYPTUS:
                return EvolutionItems.plank_eucalyptus.get();
            case FIR:
                return EvolutionItems.plank_fir.get();
            case KAPOK:
                return EvolutionItems.plank_kapok.get();
            case MANGROVE:
                return EvolutionItems.plank_mangrove.get();
            case MAPLE:
                return EvolutionItems.plank_maple.get();
            case OAK:
                return EvolutionItems.plank_oak.get();
            case OLD_OAK:
                return EvolutionItems.plank_old_oak.get();
            case PALM:
                return EvolutionItems.plank_palm.get();
            case PINE:
                return EvolutionItems.plank_pine.get();
            case REDWOOD:
                return EvolutionItems.plank_redwood.get();
            case SPRUCE:
                return EvolutionItems.plank_spruce.get();
            case WILLOW:
                return EvolutionItems.plank_willow.get();
        }
        throw new IllegalStateException("Unknown variant: " + this);
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
