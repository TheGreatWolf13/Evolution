package tgw.evolution.init;

import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.util.RockVariant;

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
    public static final ResourceLocation GUI_ICONS = Evolution.location("textures/gui/icons.png");
    public static final ResourceLocation GUI_INVENTORY = Evolution.location("textures/gui/inventory.png");
    public static final ResourceLocation GUI_TABS = Evolution.location("textures/gui/tabs.png");
    public static final ResourceLocation GUI_WIDGETS = Evolution.location("textures/gui/widgets.png");
    public static final ResourceLocation GUI_WINDOW = Evolution.location("textures/gui/window.png");
    public static final ResourceLocation PIT_KILN = Evolution.location("textures/block/pit_kiln.png");
    public static final ResourceLocation[] PIT_LOGS = {Evolution.location("textures/block/pit_acacia.png"),
                                                       Evolution.location("textures/block/pit_aspen.png"),
                                                       Evolution.location("textures/block/pit_birch.png"),
                                                       Evolution.location("textures/block/pit_cedar.png"),
                                                       Evolution.location("textures/block/pit_ebony.png"),
                                                       Evolution.location("textures/block/pit_elm.png"),
                                                       Evolution.location("textures/block/pit_eucalyptus.png"),
                                                       Evolution.location("textures/block/pit_fir.png"),
                                                       Evolution.location("textures/block/pit_kapok.png"),
                                                       Evolution.location("textures/block/pit_mangrove.png"),
                                                       Evolution.location("textures/block/pit_maple.png"),
                                                       Evolution.location("textures/block/pit_oak.png"),
                                                       Evolution.location("textures/block/pit_old_oak.png"),
                                                       Evolution.location("textures/block/pit_palm.png"),
                                                       Evolution.location("textures/block/pit_pine.png"),
                                                       Evolution.location("textures/block/pit_redwood.png"),
                                                       Evolution.location("textures/block/pit_spruce.png"),
                                                       Evolution.location("textures/block/pit_willow.png")};
    public static final ResourceLocation[] KNAPPING = new ResourceLocation[RockVariant.values().length];
    public static final ResourceLocation MOLDING = Evolution.location("textures/block/molding_block.png");
    public static final ResourceLocation SHADER_DESATURATE_25 = Evolution.location("shaders/post/saturation25.json");
    public static final ResourceLocation SHADER_DESATURATE_50 = Evolution.location("shaders/post/saturation50.json");
    public static final ResourceLocation SHADER_DESATURATE_75 = Evolution.location("shaders/post/saturation75.json");
    public static final ResourceLocation FRESH_WATER = Evolution.location("block/fluid/fresh_water");
    public static final ResourceLocation EMPTY = Evolution.location("empty");
    public static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    public static final ResourceLocation MOON_PHASES = Evolution.location("textures/environment/moon_phases.png");
    public static final ResourceLocation MOONLIGHT = Evolution.location("textures/environment/moonlight_phases.png");
    public static final ResourceLocation SUN = Evolution.location("textures/environment/sun.png");
    public static final ResourceLocation SOLAR_ECLIPSE = Evolution.location("textures/environment/solar_eclipse.png");
    public static final ResourceLocation LUNAR_ECLIPSE = Evolution.location("textures/environment/lunar_eclipse.png");

    static {
        for (RockVariant variant : RockVariant.values()) {
            if (variant != RockVariant.CLAY && variant != RockVariant.PEAT) {
                //noinspection ObjectAllocationInLoop
                KNAPPING[variant.getId()] = Evolution.location("textures/block/knapping_" + variant.getName() + ".png");
            }
        }
    }

    private EvolutionResources() {
    }
}
