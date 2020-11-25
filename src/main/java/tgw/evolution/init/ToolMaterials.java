package tgw.evolution.init;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.util.HarvestLevel;

import static tgw.evolution.init.EvolutionItems.*;

public enum ToolMaterials implements IItemTier {
    //Stone
    STONE_ANDESITE("andesite", 13.2f, 4.4f, 45, HarvestLevel.STONE, stone_andesite, 2_565),
    STONE_BASALT("basalt", 11.6f, 4.4f, 45, HarvestLevel.STONE, stone_basalt, 2_768),
    STONE_CHALK("chalk", 8.0f, 1.6f, 30, HarvestLevel.STONE, stone_chalk, 2_499),
    STONE_CHERT("chert", 8.8f, 2.2f, 30, HarvestLevel.STONE, stone_chert, 2_564),
    STONE_CONGLOMERATE("conglomerate", 8.4f, 2.0f, 30, HarvestLevel.STONE, stone_conglomerate, 2_570),
    STONE_DACITE("dacite", 11.6f, 3.1f, 45, HarvestLevel.STONE, stone_dacite, 2_402),
    STONE_DIORITE("diorite", 10.0f, 3.6f, 50, HarvestLevel.STONE, stone_diorite, 2_797),
    STONE_DOLOMITE("dolomite", 8.8f, 3.4f, 30, HarvestLevel.STONE, stone_dolomite, 2_899),
    STONE_GABBRO("gabbro", 16.0f, 6.9f, 50, HarvestLevel.STONE, stone_gabbro, 2_884),
    STONE_GNEISS("gneiss", 8.8f, 3.1f, 65, HarvestLevel.STONE, stone_gneiss, 2_812),
    STONE_GRANITE("granite", 10.4f, 3.2f, 50, HarvestLevel.STONE, stone_granite, 2_640),
    STONE_LIMESTONE("limestone", 10.4f, 2.7f, 30, HarvestLevel.STONE, stone_limestone, 2_484),
    STONE_MARBLE("marble", 10.4f, 3.5f, 65, HarvestLevel.STONE, stone_marble, 2_716),
    STONE_PHYLLITE("phyllite", 9.6f, 2.6f, 65, HarvestLevel.STONE, stone_phyllite, 2_575),
    STONE_QUARTZITE("quartzite", 8.4f, 2.2f, 65, HarvestLevel.STONE, stone_quartzite, 2_612),
    STONE_RED_SANDSTONE("red_sandstone", 8.4f, 1.8f, 30, HarvestLevel.STONE, stone_red_sandstone, 2_475),
    STONE_SANDSTONE("sandstone", 8.4f, 1.7f, 30, HarvestLevel.STONE, stone_sandstone, 2_463),
    STONE_SCHIST("schist", 8.0f, 2.5f, 65, HarvestLevel.STONE, stone_schist, 2_732),
    STONE_SHALE("shale", 8.0f, 1.1f, 30, HarvestLevel.STONE, stone_shale, 2_335),
    STONE_SLATE("slate", 8.8f, 2.7f, 35, HarvestLevel.STONE, stone_slate, 2_691),
    //Metal
    COPPER("copper", 20.0f, 10.0f, 500, HarvestLevel.COPPER, ingot_copper, 8_920);

    private final float attackDamage;
    private final float miningSpeed;
    private final int durability;
    private final int harvestLevel;
    private final Item repairMaterial;
    private final String name;
    private final int density;

    ToolMaterials(String name,
                  float attackDamage,
                  float miningSpeed,
                  int durability,
                  int harvestLevel,
                  RegistryObject<Item> repairMaterial,
                  int density) {
        this.attackDamage = attackDamage;
        this.miningSpeed = miningSpeed;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
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

    public String getName() {
        return this.name;
    }

    public double getJavelinMass() {
        return this.density / 2_000.0;
    }

    public double getAxeMass() {
        return this.density / 1_500.0;
    }

    public double getPickaxeMass() {
        return this.density / 2_000.0;
    }

    public double getShovelMass() {
        return this.density / 2_000.0;
    }

    public double getHammerMass() {
        return this.density / 1_500.0;
    }
}
