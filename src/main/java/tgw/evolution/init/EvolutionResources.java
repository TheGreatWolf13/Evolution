package tgw.evolution.init;

import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.util.RockVariant;
import tgw.evolution.util.WoodVariant;

public final class EvolutionResources {
    //Slot indices
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
    //Resource Locations
    //Blocks
    public static final ResourceLocation[] BLOCK_KNAPPING;
    public static final ResourceLocation[] BLOCK_LOG_SIDE;
    public static final ResourceLocation[] BLOCK_LOG_TOP;
    public static final ResourceLocation BLOCK_MOLDING = Evolution.getResource("textures/block/molding_block.png");
    public static final ResourceLocation BLOCK_PIT_KILN = Evolution.getResource("textures/block/pit_kiln.png");
    public static final ResourceLocation[] BLOCK_PIT_LOG;
    //Empty
    public static final ResourceLocation EMPTY = Evolution.getResource("empty");
    //Environment
    public static final ResourceLocation ENVIRONMENT_LUNAR_ECLIPSE = Evolution.getResource("textures/environment/lunar_eclipse.png");
    public static final ResourceLocation ENVIRONMENT_MOON = Evolution.getResource("textures/environment/moon.png");
    public static final ResourceLocation ENVIRONMENT_MOON_SHADOW = Evolution.getResource("textures/environment/moon_shadow.png");
    public static final ResourceLocation ENVIRONMENT_MOONLIGHT = Evolution.getResource("textures/environment/moonlight.png");
    public static final ResourceLocation ENVIRONMENT_SOLAR_ECLIPSE = Evolution.getResource("textures/environment/solar_eclipse.png");
    public static final ResourceLocation ENVIRONMENT_SUN = Evolution.getResource("textures/environment/sun.png");
    //Fluids
    public static final ResourceLocation FLUID_FRESH_WATER = Evolution.getResource("block/fluid/fresh_water");
    //GUI
    public static final ResourceLocation GUI_CORPSE = Evolution.getResource("textures/gui/corpse.png");
    public static final ResourceLocation GUI_CORPSE_DEATH = Evolution.getResource("textures/gui/corpse_death.png");
    public static final ResourceLocation GUI_DAMAGE_ICONS = Evolution.getResource("textures/gui/damage_icons.png");
    public static final ResourceLocation GUI_ICONS = Evolution.getResource("textures/gui/icons.png");
    public static final ResourceLocation GUI_INVENTORY = Evolution.getResource("textures/gui/inventory.png");
    public static final ResourceLocation GUI_KNAPPING = Evolution.getResource("textures/gui/knapping.png");
    public static final ResourceLocation GUI_MOLDING = Evolution.getResource("textures/gui/molding.png");
    public static final ResourceLocation GUI_RECIPE_BUTTON = new ResourceLocation("textures/gui/recipe_button.png");
    public static final ResourceLocation GUI_STATS_ICONS = Evolution.getResource("textures/gui/stats_icons.png");
    public static final ResourceLocation GUI_TABS = Evolution.getResource("textures/gui/tabs.png");
    public static final ResourceLocation GUI_WIDGETS = Evolution.getResource("textures/gui/widgets.png");
    public static final ResourceLocation GUI_WINDOW = Evolution.getResource("textures/gui/window.png");
    public static final ResourceLocation GUI_WORLD_SELECTION = new ResourceLocation("textures/gui/world_selection.png");
    //Shaders
    public static final ResourceLocation SHADER_DESATURATE_25 = Evolution.getResource("shaders/post/saturation25.json");
    public static final ResourceLocation SHADER_DESATURATE_50 = Evolution.getResource("shaders/post/saturation50.json");
    public static final ResourceLocation SHADER_DESATURATE_75 = Evolution.getResource("shaders/post/saturation75.json");
    public static final ResourceLocation SHADER_MOTION_BLUR = new ResourceLocation("shaders/post/phosphor.json");
    //Slots
    public static final ResourceLocation[] SLOT_ARMOR = {PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS,
                                                         PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS,
                                                         PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE,
                                                         PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
    public static final ResourceLocation[] SLOT_EXTENDED = {Evolution.getResource("item/slot_hat"),
                                                            Evolution.getResource("item/slot_body"),
                                                            Evolution.getResource("item/slot_legs"),
                                                            Evolution.getResource("item/slot_feet"),
                                                            Evolution.getResource("item/slot_cloak"),
                                                            Evolution.getResource("item/slot_mask"),
                                                            Evolution.getResource("item/slot_back"),
                                                            Evolution.getResource("item/slot_tactical")};

    static {
        BLOCK_KNAPPING = new ResourceLocation[RockVariant.VALUES.length];
        for (RockVariant variant : RockVariant.VALUES) {
            if (variant != RockVariant.CLAY && variant != RockVariant.PEAT) {
                //noinspection ObjectAllocationInLoop
                BLOCK_KNAPPING[variant.getId()] = Evolution.getResource("block/stone_" + variant.getName());
            }
        }
        BLOCK_LOG_SIDE = new ResourceLocation[WoodVariant.VALUES.length];
        for (WoodVariant variant : WoodVariant.VALUES) {
            //noinspection ObjectAllocationInLoop
            BLOCK_LOG_SIDE[variant.getId()] = Evolution.getResource("block/log_" + variant.getName());
        }
        BLOCK_LOG_TOP = new ResourceLocation[WoodVariant.VALUES.length];
        for (WoodVariant variant : WoodVariant.VALUES) {
            //noinspection ObjectAllocationInLoop
            BLOCK_LOG_TOP[variant.getId()] = Evolution.getResource("block/log_top_" + variant.getName());
        }
        BLOCK_PIT_LOG = new ResourceLocation[WoodVariant.VALUES.length];
        for (WoodVariant variant : WoodVariant.VALUES) {
            //noinspection ObjectAllocationInLoop
            BLOCK_PIT_LOG[variant.getId()] = Evolution.getResource("textures/block/pit_" + variant.getName() + ".png");
        }
    }

    private EvolutionResources() {
    }
}
