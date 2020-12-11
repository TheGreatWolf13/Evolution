package tgw.evolution.init;

import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;

public final class EvolutionResources {
    public static final String[] SLOT_ARMOR = {"item/empty_armor_slot_boots",
                                               "item/empty_armor_slot_leggings",
                                               "item/empty_armor_slot_chestplate",
                                               "item/empty_armor_slot_helmet"};
    public static final String SLOT_SHIELD = "item/empty_armor_slot_shield";
    public static final String[] SLOT_EXTENDED = {"evolution:item/slot_hat",
                                                  "evolution:item/slot_body",
                                                  "evolution:item/slot_legs",
                                                  "evolution:item/slot_feet",
                                                  "evolution:item/slot_cloak",
                                                  "evolution:item/slot_mask",
                                                  "evolution:item/slot_back",
                                                  "evolution:item/slot_tactical"};
    public static final int BOOTS = 0;
    public static final int LEGGINGS = 1;
    public static final int CHESTPLATE = 2;
    public static final int HELMET = 3;
    public static final int HAT = 0;
    public static final int BODY = 1;
    public static final int LEGS = 2;
    public static final int FEET = 3;
    public static final int CLOAK = 4;
    public static final int MASK = 5;
    public static final int BACK = 6;
    public static final int TACTICAL = 7;
    public static final ResourceLocation BUTTON_RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_button.png");
    public static final ResourceLocation GUI_CORPSE = Evolution.location("textures/gui/corpse.png");
    public static final ResourceLocation GUI_INVENTORY = Evolution.location("textures/gui/inventory.png");

    private EvolutionResources() {
    }
}
