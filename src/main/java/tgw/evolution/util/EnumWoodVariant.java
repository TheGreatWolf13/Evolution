package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.items.ItemLog;

import static tgw.evolution.init.EvolutionBlocks.*;
import static tgw.evolution.init.EvolutionItems.*;

public enum EnumWoodVariant {
    ACACIA("acacia", log_acacia, LOG_PILE_ACACIA, 0, plank_acacia),
    ASPEN("aspen", log_aspen, LOG_PILE_ASPEN, 1, plank_aspen),
    BIRCH("birch", log_birch, LOG_PILE_BIRCH, 2, plank_birch),
    CEDAR("cedar", log_cedar, LOG_PILE_CEDAR, 3, plank_cedar),
    EBONY("ebony", log_ebony, LOG_PILE_EBONY, 4, plank_ebony),
    ELM("elm", log_elm, LOG_PILE_ELM, 5, plank_elm),
    EUCALYPTUS("eucalyptus", log_eucalyptus, LOG_PILE_EUCALYPTUS, 6, plank_eucalyptus),
    FIR("fir", log_fir, LOG_PILE_FIR, 7, plank_fir),
    KAPOK("kapok", log_kapok, LOG_PILE_KAPOK, 8, plank_kapok),
    MANGROVE("mangrove", log_mangrove, LOG_PILE_MANGROVE, 9, plank_mangrove),
    MAPLE("maple", log_maple, LOG_PILE_MAPLE, 10, plank_maple),
    OAK("oak", log_oak, LOG_PILE_OAK, 11, plank_oak),
    OLD_OAK("old_oak", log_old_oak, LOG_PILE_OLD_OAK, 12, plank_old_oak),
    PALM("palm", log_palm, LOG_PILE_PALM, 13, plank_palm),
    PINE("pine", log_pine, LOG_PILE_PINE, 14, plank_pine),
    REDWOOD("redwood", log_redwood, LOG_PILE_REDWOOD, 15, plank_redwood),
    SPRUCE("spruce", log_spruce, LOG_PILE_SPRUCE, 16, plank_spruce),
    WILLOW("willow", log_willow, LOG_PILE_WILLOW, 17, plank_willow);

    private final byte id;
    private final Item log;
    private final String name;
    private final Block pile;
    private final Item plank;

    EnumWoodVariant(String name, RegistryObject<ItemLog> log, RegistryObject<Block> pile, int id, RegistryObject<Item> plank) {
        this.name = name;
        this.log = log.get();
        this.pile = pile.get();
        this.plank = plank.get();
        this.id = (byte) id;
    }

    public static EnumWoodVariant byId(int id) {
        for (EnumWoodVariant variant : values()) {
            if (variant.id == id) {
                return variant;
            }
        }
        throw new IllegalStateException("Cannot find EnumWoodVariant with id " + id);
    }

    public byte getId() {
        return this.id;
    }

    public Item getLog() {
        return this.log;
    }

    public String getName() {
        return this.name;
    }

    public Block getPile() {
        return this.pile;
    }

    public Item getPlank() {
        return this.plank;
    }
}
