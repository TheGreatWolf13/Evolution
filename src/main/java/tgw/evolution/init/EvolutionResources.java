package tgw.evolution.init;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.util.collection.BiEnumMap;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import static tgw.evolution.capabilities.modular.part.PartTypes.*;

public final class EvolutionResources {
    //Icons coordinates
    public static final int ICON_HEARTS = 0;
    public static final int ICON_HEARTS_HARDCORE = 18;
    public static final int ICON_HUNGER = 36;
    public static final int ICON_THIRST = 45;
    public static final int ICON_STAMINA = 54;
    public static final int ICON_TEMPERATURE = 63;
    public static final int ICON_17_17 = 218;
    public static final int ICON_12_12 = 235;
    public static final int ICON_9_9 = 247;
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
    public static final ResourceLocation ENVIRONMENT_MOON_SHADOW = Evolution.getResource("textures/environment/moon_shadow.png");
    public static final ResourceLocation ENVIRONMENT_MOON_SHADOW_ECLIPSE = Evolution.getResource("textures/environment/moon_shadow_eclipse.png");
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
    public static final ResourceLocation[] SLOT_ARMOR;
    public static final ResourceLocation[] SLOT_EXTENDED;
    public static final ResourceLocation SLOT_OFFHAND = Evolution.getResource("item/slot_offhand");
    //Models
    //      Modular
    public static final RList<ModelResourceLocation> MODULAR_MODELS = new RArrayList<>();
    public static final ModelResourceLocation TOOL_SWEEP = new ModelResourceLocation(Evolution.getResource("modular_tool_sweep"), "inventory");
    public static final ModelResourceLocation TOOL_THROWING = new ModelResourceLocation(Evolution.getResource("modular_tool_throwing"), "inventory");
    //          Blade
    public static final BiEnumMap<Blade, Material, ModelResourceLocation> MODULAR_BLADES = new BiEnumMap<>(Blade.class, Material.class);
    public static final BiEnumMap<Blade, Material, ModelResourceLocation> MODULAR_BLADES_SHARP = new BiEnumMap<>(Blade.class, Material.class);
    //          Guard
    public static final BiEnumMap<Guard, Material, ModelResourceLocation> MODULAR_GUARDS = new BiEnumMap<>(Guard.class, Material.class);
    //          Half Head
    public static final BiEnumMap<HalfHead, Material, ModelResourceLocation> MODULAR_HALF_HEADS = new BiEnumMap<>(HalfHead.class,
                                                                                                                  Material.class);
    public static final BiEnumMap<HalfHead, Material, ModelResourceLocation> MODULAR_HALF_HEADS_SHARP = new BiEnumMap<>(HalfHead.class,
                                                                                                                        Material.class);
    //          Handle
    public static final BiEnumMap<Handle, Material, ModelResourceLocation> MODULAR_HANDLES = new BiEnumMap<>(Handle.class, Material.class);
    //          Head
    public static final BiEnumMap<Head, Material, ModelResourceLocation> MODULAR_HEADS = new BiEnumMap<>(Head.class, Material.class);
    public static final BiEnumMap<Head, Material, ModelResourceLocation> MODULAR_HEADS_SHARP = new BiEnumMap<>(Head.class, Material.class);
    //          Hilt
    public static final BiEnumMap<Hilt, Material, ModelResourceLocation> MODULAR_HILTS = new BiEnumMap<>(Hilt.class, Material.class);
    //          Pole
    public static final BiEnumMap<Pole, Material, ModelResourceLocation> MODULAR_POLES = new BiEnumMap<>(Pole.class, Material.class);
    //          Pommel
    public static final BiEnumMap<Pommel, Material, ModelResourceLocation> MODULAR_POMMELS = new BiEnumMap<>(Pommel.class, Material.class);

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
        for (Blade blade : Blade.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(blade)) {
                    MODULAR_BLADES.put(blade, material, itemModel("modular/blade", blade, material, false));
                    if (blade.canBeSharpened()) {
                        MODULAR_BLADES_SHARP.put(blade, material, itemModel("modular/blade", blade, material, true));
                    }
                }
            }
        }
        for (Guard guard : Guard.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(guard)) {
                    MODULAR_GUARDS.put(guard, material, itemModel("modular/guard", guard, material, false));
                }
            }
        }
        for (HalfHead halfHead : HalfHead.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(halfHead)) {
                    MODULAR_HALF_HEADS.put(halfHead, material, itemModel("modular/halfhead", halfHead, material, false));
                    if (halfHead.canBeSharpened()) {
                        MODULAR_HALF_HEADS_SHARP.put(halfHead, material, itemModel("modular/halfhead", halfHead, material, true));
                    }
                }
            }
        }
        for (Handle handle : Handle.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(handle)) {
                    MODULAR_HANDLES.put(handle, material, itemModel("modular/handle", handle, material, false));
                }
            }
        }
        for (Head head : Head.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(head)) {
                    MODULAR_HEADS.put(head, material, itemModel("modular/head", head, material, false));
                    if (head.canBeSharpened()) {
                        MODULAR_HEADS_SHARP.put(head, material, itemModel("modular/head", head, material, true));
                    }
                }
            }
        }
        for (Hilt hilt : Hilt.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(hilt)) {
                    MODULAR_HILTS.put(hilt, material, itemModel("modular/hilt", hilt, material, false));
                }
            }
        }
        for (Pole pole : Pole.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(pole)) {
                    MODULAR_POLES.put(pole, material, itemModel("modular/pole", pole, material, false));
                }
            }
        }
        for (Pommel pommel : Pommel.VALUES) {
            for (Material material : Material.VALUES) {
                if (material.isAllowedBy(pommel)) {
                    MODULAR_POMMELS.put(pommel, material, itemModel("modular/pommel", pommel, material, false));
                }
            }
        }
        MODULAR_MODELS.add(TOOL_SWEEP);
        MODULAR_MODELS.add(TOOL_THROWING);
        SLOT_EXTENDED = new ResourceLocation[AdditionalSlotType.VALUES.length];
        for (int i = 0, l = AdditionalSlotType.VALUES.length; i < l; i++) {
            AdditionalSlotType slotType = AdditionalSlotType.VALUES[i];
            //noinspection ObjectAllocationInLoop
            SLOT_EXTENDED[slotType.getSlotId()] = Evolution.getResource("item/slot_" + slotType.getName());
        }
        SLOT_ARMOR = new ResourceLocation[4];
        for (int i = 0; i < 4; i++) {
            String slot = switch (i) {
                case 0 -> "armor_feet";
                case 1 -> "armor_legs";
                case 2 -> "armor_chest";
                case 3 -> "armor_head";
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };
            //noinspection ObjectAllocationInLoop
            SLOT_ARMOR[i] = Evolution.getResource("item/slot_" + slot);
        }
    }

    private EvolutionResources() {
    }

    private static ModelResourceLocation itemModel(String path, IPartType<?, ?, ?> part, Material material, boolean sharp) {
        String name = path + "/" + part.getName() + "_" + material.getName() + (sharp ? "_sharp" : "");
        ModelResourceLocation model = new ModelResourceLocation(Evolution.getResource(name), "inventory");
        MODULAR_MODELS.add(model);
        return model;
    }
}
