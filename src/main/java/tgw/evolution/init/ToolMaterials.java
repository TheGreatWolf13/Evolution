package tgw.evolution.init;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.util.HarvestLevel;

import static tgw.evolution.init.EvolutionItems.*;

public enum ToolMaterials implements IItemTier {
    //Stone
    STONE_ANDESITE("andesite", 3.3f, 4.4f, 45, HarvestLevel.STONE, stone_andesite, -0.1F, 2565),
    STONE_BASALT("basalt", 2.9f, 4.4f, 45, HarvestLevel.STONE, stone_basalt, 0.3F, 2768),
    STONE_CHALK("chalk", 2.0f, 1.6f, 30, HarvestLevel.STONE, stone_chalk, -0.2F, 2499),
    STONE_CHERT("chert", 2.2f, 2.2f, 30, HarvestLevel.STONE, stone_chert, -0.1F, 2564),
    STONE_CONGLOMERATE("conglomerate", 2.1f, 2.0f, 30, HarvestLevel.STONE, stone_conglomerate, -0.1F, 2570),
    STONE_DACITE("dacite", 2.9f, 3.1f, 45, HarvestLevel.STONE, stone_dacite, -0.4F, 2402),
    STONE_DIORITE("diorite", 2.5f, 3.6f, 50, HarvestLevel.STONE, stone_diorite, 0.3F, 2797),
    STONE_DOLOMITE("dolomite", 2.2f, 3.4f, 30, HarvestLevel.STONE, stone_dolomite, 0.5F, 2899),
    STONE_GABBRO("gabbro", 4.0f, 6.9f, 50, HarvestLevel.STONE, stone_gabbro, 0.5F, 2884),
    STONE_GNEISS("gneiss", 2.2f, 3.1f, 65, HarvestLevel.STONE, stone_gneiss, 0.3F, 2812),
    STONE_GRANITE("granite", 2.6f, 3.2f, 50, HarvestLevel.STONE, stone_granite, 0.0F, 2640),
    STONE_LIMESTONE("limestone", 2.6f, 2.7f, 30, HarvestLevel.STONE, stone_limestone, -0.2F, 2484),
    STONE_MARBLE("marble", 2.6f, 3.5f, 65, HarvestLevel.STONE, stone_marble, 0.2F, 2716),
    STONE_PHYLLITE("phyllite", 2.4f, 2.6f, 65, HarvestLevel.STONE, stone_phyllite, -0.1F, 2575),
    STONE_QUARTZITE("quartzite", 2.1f, 2.2f, 65, HarvestLevel.STONE, stone_quartzite, 0.0F, 2612),
    STONE_RED_SANDSTONE("red_sandstone", 2.1f, 1.8f, 30, HarvestLevel.STONE, stone_red_sandstone, -0.3F, 2475),
    STONE_SANDSTONE("sandstone", 2.1f, 1.7f, 30, HarvestLevel.STONE, stone_sandstone, -0.3F, 2463),
    STONE_SCHIST("schist", 2.0f, 2.5f, 65, HarvestLevel.STONE, stone_schist, 0.2F, 2732),
    STONE_SHALE("shale", 2.0f, 1.1f, 30, HarvestLevel.STONE, stone_shale, -0.5F, 2335),
    STONE_SLATE("slate", 2.2f, 2.7f, 35, HarvestLevel.STONE, stone_slate, 0.1F, 2691),
    //Metal
    COPPER("copper", 5.0f, 10.0f, 500, HarvestLevel.COPPER, ingot_copper, 0.1f, 8920);

    private final float attackDamage;
    private final float miningSpeed;
    private final int durability;
    private final int harvestLevel;
    private final Item repairMaterial;
    private final float attackSpeedMod;
    private final String name;
    private final int density;

    ToolMaterials(String name, float attackDamage, float miningSpeed, int durability, int harvestLevel, RegistryObject<Item> repairMaterial, float attackSpeedMod, int density) {
        this.attackDamage = attackDamage;
        this.miningSpeed = miningSpeed;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
        this.attackSpeedMod = attackSpeedMod;
        this.repairMaterial = repairMaterial.get();
        this.name = name;
        this.density = density;
    }

    @Override
    public int getMaxUses() {
        return this.durability;
    }

    @Override
    public float getEfficiency() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(this.repairMaterial);
    }

    public float getAttackSpeedMod() {
        return this.attackSpeedMod;
    }

    public String getName() {
        return this.name;
    }

    public double getJavelinMass() {
        return this.density / 2000.0;
    }

    public double getAxeMass() {
        return this.density / 1500.0;
    }

    public double getPickaxeMass() {
        return this.density / 2000.0;
    }

    public double getShovelMass() {
        return this.density / 2000.0;
    }

    public double getHammerMass() {
        return this.density / 1500.0;
    }
}
