package tgw.evolution.init;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import tgw.evolution.Evolution;

public enum EvolutionArmorMaterials implements IArmorMaterial {
    generic("placeholder", 400, new int[]{8, 10, 9, 7}, 25, EvolutionItems.placeholder_item.get(), "entity.ender_dragon.growl", 0.0f);

    private static final int[] MAX_DAMAGE_ARRAY = {13, 15, 16, 11};
    private final int durability;
    private final int enchantability;
    private final String name;
    private final String equipSound;
    private final int[] damageReduction;
    private final Item repairItem;
    private final float toughness;

    EvolutionArmorMaterials(String name, int durability, int[] damageReductionAmount, int enchantability, Item repairItem, String equipSound, float toughness) {
        this.name = name;
        this.equipSound = equipSound;
        this.durability = durability;
        this.enchantability = enchantability;
        this.damageReduction = damageReductionAmount;
        this.repairItem = repairItem;
        this.toughness = toughness;
    }

    @Override
    public int getDurability(EquipmentSlotType slotIn) {
        return MAX_DAMAGE_ARRAY[slotIn.getIndex()] * this.durability;
    }

    @Override
    public int getDamageReductionAmount(EquipmentSlotType slotIn) {
        return this.damageReduction[slotIn.getIndex()];
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return new SoundEvent(new ResourceLocation(this.equipSound));
    }

    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(this.repairItem);
    }

    @Override
    public String getName() {
        return Evolution.MODID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }
}
