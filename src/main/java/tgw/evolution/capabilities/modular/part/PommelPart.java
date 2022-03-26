package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionDamage;

import java.util.List;

public class PommelPart implements IHitPart<PartTypes.Pommel> {

    public static final PommelPart DUMMY = new PommelPart();
    private MaterialInstance material = MaterialInstance.DUMMY;
    private int spentDurability;
    private PartTypes.Pommel type = PartTypes.Pommel.NULL;

    public static PommelPart get(ItemStack stack) {
        return (PommelPart) stack.getCapability(CapabilityModular.PART).orElse(DUMMY);
    }

    @Override
    public void appendText(List<Either<FormattedText, TooltipComponent>> tooltip, int num) {
        //TODO implementation

    }

    @Override
    public boolean canBeSharpened() {
        return this.type.canBeSharpened();
    }

    @Override
    public void damage(int amount) {
        this.spentDurability += amount;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.type = PartTypes.Pommel.byName(nbt.getString("Type"));
        this.material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        this.spentDurability = nbt.getInt("Durability");
    }

    @Override
    public double getAttackDamageInternal(double preAttackDamage) {
        //TODO implementation
        return preAttackDamage;
    }

    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.CRUSHING;
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.pommel." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    public int getDurabilityDmg() {
        return this.spentDurability;
    }

    @Override
    public ReferenceSet<Material> getEffectiveMaterials() {
        return ReferenceSet.of();
    }

    @Override
    public int getHarvestLevel() {
        return this.material.getHarvestLevel();
    }

    @Override
    public MaterialInstance getMaterial() {
        return this.material;
    }

    @Override
    public int getMaxDurability() {
        return Mth.ceil(this.material.getHardness() * this.material.getResistance() / 50.0);
    }

    @Override
    public float getMiningSpeed() {
        return 1.0f;
    }

    @Override
    public int getSharpAmount() {
        return 0;
    }

    @Override
    public PartTypes.Pommel getType() {
        return this.type;
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public void loseSharp(int amount) {
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getName());
        tag.put("MaterialInstance", this.material.write());
        tag.putInt("Durability", this.spentDurability);
        return tag;
    }

    @Override
    public void set(PartTypes.Pommel type, MaterialInstance material) {
        this.type = type;
        this.material = material;
    }

    @Override
    public void sharp() {
    }
}
