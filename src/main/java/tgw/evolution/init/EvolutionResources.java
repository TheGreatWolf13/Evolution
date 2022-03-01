package tgw.evolution.init;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.antlr.runtime.misc.DoubleKeyMap;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.util.ArrayList;
import java.util.List;

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
    //      Blocks
    public static final ResourceLocation[] BLOCK_KNAPPING;
    public static final ResourceLocation[] BLOCK_LOG_SIDE;
    public static final ResourceLocation[] BLOCK_LOG_TOP;
    public static final ResourceLocation BLOCK_MOLDING = Evolution.getResource("textures/block/molding_block.png");
    public static final ResourceLocation BLOCK_PIT_KILN = Evolution.getResource("textures/block/pit_kiln.png");
    public static final ResourceLocation[] BLOCK_PIT_LOG;
    //      Empty
    public static final ResourceLocation EMPTY = Evolution.getResource("empty");
    //      Environment
    public static final ResourceLocation ENVIRONMENT_LUNAR_ECLIPSE = Evolution.getResource("textures/environment/lunar_eclipse.png");
    public static final ResourceLocation ENVIRONMENT_MOON = Evolution.getResource("textures/environment/moon.png");
    public static final ResourceLocation ENVIRONMENT_MOONLIGHT = Evolution.getResource("textures/environment/moonlight.png");
    public static final ResourceLocation ENVIRONMENT_SOLAR_ECLIPSE = Evolution.getResource("textures/environment/solar_eclipse.png");
    public static final ResourceLocation ENVIRONMENT_SUN = Evolution.getResource("textures/environment/sun.png");
    //      Fluids
    public static final ResourceLocation FLUID_FRESH_WATER = Evolution.getResource("block/fluid/fresh_water");
    //      GUI
    public static final ResourceLocation GUI_ICONS = Evolution.getResource("textures/gui/icons.png");
    public static final ResourceLocation GUI_INVENTORY = Evolution.getResource("textures/gui/inventory.png");
    public static final ResourceLocation GUI_TABS = Evolution.getResource("textures/gui/tabs.png");
    //      Shaders
    public static final ResourceLocation SHADER_DESATURATE_25 = Evolution.getResource("shaders/post/saturation25.json");
    public static final ResourceLocation SHADER_DESATURATE_50 = Evolution.getResource("shaders/post/saturation50.json");
    public static final ResourceLocation SHADER_DESATURATE_75 = Evolution.getResource("shaders/post/saturation75.json");
    public static final ResourceLocation SHADER_MOTION_BLUR = new ResourceLocation("shaders/post/phosphor.json");
    //      Slots
    public static final ResourceLocation[] SLOT_ARMOR = {InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
                                                         InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
                                                         InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
                                                         InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    public static final ResourceLocation[] SLOT_EXTENDED = {Evolution.getResource("item/slot_hat"),
                                                            Evolution.getResource("item/slot_body"),
                                                            Evolution.getResource("item/slot_legs"),
                                                            Evolution.getResource("item/slot_feet"),
                                                            Evolution.getResource("item/slot_cloak"),
                                                            Evolution.getResource("item/slot_mask"),
                                                            Evolution.getResource("item/slot_back"),
                                                            Evolution.getResource("item/slot_tactical")};
    //Models
    //      Modular
    public static final List<ModelResourceLocation> MODULAR_MODELS = new ArrayList<>();
    //          Blade
    public static final DoubleKeyMap<PartTypes.Blade, ItemMaterial, ModelResourceLocation> MODULAR_BLADES = new DoubleKeyMap<>();
    public static final DoubleKeyMap<PartTypes.Blade, ItemMaterial, ModelResourceLocation> MODULAR_BLADES_SHARP = new DoubleKeyMap<>();
    //          Guard
    public static final DoubleKeyMap<PartTypes.Guard, ItemMaterial, ModelResourceLocation> MODULAR_GUARDS = new DoubleKeyMap<>();
    //          Half Head
    public static final DoubleKeyMap<PartTypes.HalfHead, ItemMaterial, ModelResourceLocation> MODULAR_HALF_HEADS = new DoubleKeyMap<>();
    public static final DoubleKeyMap<PartTypes.HalfHead, ItemMaterial, ModelResourceLocation> MODULAR_HALF_HEADS_SHARP = new DoubleKeyMap<>();
    //          Handle
    public static final DoubleKeyMap<PartTypes.Handle, ItemMaterial, ModelResourceLocation> MODULAR_HANDLES = new DoubleKeyMap<>();
    //          Head
    public static final DoubleKeyMap<PartTypes.Head, ItemMaterial, ModelResourceLocation> MODULAR_HEADS = new DoubleKeyMap<>();
    public static final DoubleKeyMap<PartTypes.Head, ItemMaterial, ModelResourceLocation> MODULAR_HEADS_SHARP = new DoubleKeyMap<>();
    //          Hilt
    public static final DoubleKeyMap<PartTypes.Hilt, ItemMaterial, ModelResourceLocation> MODULAR_HILTS = new DoubleKeyMap<>();
    //          Pole
    public static final DoubleKeyMap<PartTypes.Pole, ItemMaterial, ModelResourceLocation> MODULAR_POLES = new DoubleKeyMap<>();
    //          Pommel
    public static final DoubleKeyMap<PartTypes.Pommel, ItemMaterial, ModelResourceLocation> MODULAR_POMMELS = new DoubleKeyMap<>();

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
        for (PartTypes.Blade blade : PartTypes.Blade.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(blade)) {
                    MODULAR_BLADES.put(blade, material, itemModel("modular/blade", blade, material, false));
                    if (blade.canBeSharpened()) {
                        MODULAR_BLADES_SHARP.put(blade, material, itemModel("modular/blade", blade, material, true));
                    }
                }
            }
        }
        for (PartTypes.Guard guard : PartTypes.Guard.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(guard)) {
                    MODULAR_GUARDS.put(guard, material, itemModel("modular/guard", guard, material, false));
                }
            }
        }
        for (PartTypes.HalfHead halfHead : PartTypes.HalfHead.VALUES)  {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(halfHead)) {
                    MODULAR_HALF_HEADS.put(halfHead, material, itemModel("modular/halfhead", halfHead, material, false));
                    if (halfHead.canBeSharpened()) {
                        MODULAR_HALF_HEADS_SHARP.put(halfHead, material, itemModel("modular/halfhead", halfHead, material, true));
                    }
                }
            }
        }
        for (PartTypes.Handle handle : PartTypes.Handle.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(handle)) {
                    MODULAR_HANDLES.put(handle, material, itemModel("modular/handle", handle, material, false));
                }
            }
        }
        for (PartTypes.Head head : PartTypes.Head.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(head)) {
                    MODULAR_HEADS.put(head, material, itemModel("modular/head", head, material, false));
                    if (head.canBeSharpened()) {
                        MODULAR_HEADS_SHARP.put(head, material, itemModel("modular/head", head, material, true));
                    }
                }
            }
        }
        for (PartTypes.Hilt hilt : PartTypes.Hilt.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(hilt)) {
                    MODULAR_HILTS.put(hilt, material, itemModel("modular/hilt", hilt, material, false));
                }
            }
        }
        for (PartTypes.Pole pole : PartTypes.Pole.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(pole)) {
                    MODULAR_POLES.put(pole, material, itemModel("modular/pole", pole, material, false));
                }
            }
        }
        for (PartTypes.Pommel pommel : PartTypes.Pommel.VALUES) {
            for (ItemMaterial material : ItemMaterial.VALUES) {
                if (material.isAllowedBy(pommel)) {
                    MODULAR_POMMELS.put(pommel, material, itemModel("modular/pommel", pommel, material, false));
                }
            }
        }
    }

    private static ModelResourceLocation itemModel(String path, IPartType<?> part, ItemMaterial material, boolean sharp) {
        String name = path + "/" + part.getName() + "_" + material.getName() + (sharp ? "_sharp" : "");
        ModelResourceLocation model = new ModelResourceLocation(Evolution.getResource(name), "inventory");
        MODULAR_MODELS.add(model);
        return model;
    }

    private EvolutionResources() {
    }
}
