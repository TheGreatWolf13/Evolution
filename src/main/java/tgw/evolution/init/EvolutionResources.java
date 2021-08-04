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
    public static final ResourceLocation GUI_CORPSE = Evolution.getResource("textures/gui/corpse.png");
    public static final ResourceLocation GUI_ICONS = Evolution.getResource("textures/gui/icons.png");
    public static final ResourceLocation GUI_INVENTORY = Evolution.getResource("textures/gui/inventory.png");
    public static final ResourceLocation GUI_KNAPPING = Evolution.getResource("textures/gui/knapping.png");
    public static final ResourceLocation GUI_STATS_ICONS = Evolution.getResource("textures/gui/stats_icons.png");
    public static final ResourceLocation GUI_TABS = Evolution.getResource("textures/gui/tabs.png");
    public static final ResourceLocation GUI_WIDGETS = Evolution.getResource("textures/gui/widgets.png");
    public static final ResourceLocation GUI_WINDOW = Evolution.getResource("textures/gui/window.png");
    public static final ResourceLocation GUI_WORLD_SELECTION = new ResourceLocation("textures/gui/world_selection.png");
    public static final ResourceLocation PIT_KILN = Evolution.getResource("textures/block/pit_kiln.png");
    public static final ResourceLocation[] PIT_LOGS = {Evolution.getResource("textures/block/pit_acacia.png"),
                                                       Evolution.getResource("textures/block/pit_aspen.png"),
                                                       Evolution.getResource("textures/block/pit_birch.png"),
                                                       Evolution.getResource("textures/block/pit_cedar.png"),
                                                       Evolution.getResource("textures/block/pit_ebony.png"),
                                                       Evolution.getResource("textures/block/pit_elm.png"),
                                                       Evolution.getResource("textures/block/pit_eucalyptus.png"),
                                                       Evolution.getResource("textures/block/pit_fir.png"),
                                                       Evolution.getResource("textures/block/pit_kapok.png"),
                                                       Evolution.getResource("textures/block/pit_mangrove.png"),
                                                       Evolution.getResource("textures/block/pit_maple.png"),
                                                       Evolution.getResource("textures/block/pit_oak.png"),
                                                       Evolution.getResource("textures/block/pit_old_oak.png"),
                                                       Evolution.getResource("textures/block/pit_palm.png"),
                                                       Evolution.getResource("textures/block/pit_pine.png"),
                                                       Evolution.getResource("textures/block/pit_redwood.png"),
                                                       Evolution.getResource("textures/block/pit_spruce.png"),
                                                       Evolution.getResource("textures/block/pit_willow.png")};
    public static final ResourceLocation[] KNAPPING = new ResourceLocation[RockVariant.values().length];
    public static final ResourceLocation MOLDING = Evolution.getResource("textures/block/molding_block.png");
    public static final ResourceLocation SHADER_DESATURATE_25 = Evolution.getResource("shaders/post/saturation25.json");
    public static final ResourceLocation SHADER_DESATURATE_50 = Evolution.getResource("shaders/post/saturation50.json");
    public static final ResourceLocation SHADER_DESATURATE_75 = Evolution.getResource("shaders/post/saturation75.json");
    public static final ResourceLocation FRESH_WATER = Evolution.getResource("block/fluid/fresh_water");
    public static final ResourceLocation EMPTY = Evolution.getResource("empty");
    public static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    public static final ResourceLocation MOON_PHASES = Evolution.getResource("textures/environment/moon_phases.png");
    public static final ResourceLocation MOONLIGHT = Evolution.getResource("textures/environment/moonlight_phases.png");
    public static final ResourceLocation SUN = Evolution.getResource("textures/environment/sun.png");
    public static final ResourceLocation SOLAR_ECLIPSE = Evolution.getResource("textures/environment/solar_eclipse.png");
    public static final ResourceLocation LUNAR_ECLIPSE = Evolution.getResource("textures/environment/lunar_eclipse.png");
    public static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");

    static {
        for (RockVariant variant : RockVariant.values()) {
            if (variant != RockVariant.CLAY && variant != RockVariant.PEAT) {
                //noinspection ObjectAllocationInLoop
                KNAPPING[variant.getId()] = Evolution.getResource("textures/block/knapping_" + variant.getName() + ".png");
            }
        }
    }

    private EvolutionResources() {
    }
}
