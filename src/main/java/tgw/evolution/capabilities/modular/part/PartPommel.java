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
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartPommel;

import java.util.List;

public class PartPommel implements IPartHit<PartTypes.Pommel, ItemPartPommel, PartPommel> {

    public static final PartPommel DUMMY = new PartPommel();
    private MaterialInstance material = MaterialInstance.DUMMY;
    private int spentDurability;
    private PartTypes.Pommel type = PartTypes.Pommel.NULL;

    public static PartPommel get(ItemStack stack) {
        return (PartPommel) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
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
        this.type = PartTypes.Pommel.byId(nbt.getByte("Type"));
        this.material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        this.spentDurability = nbt.getInt("Durability");
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
    public double getDmgMultiplierInternal() {
        //TODO implementation
        return 1.0;
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
    public MaterialInstance getMaterialInstance() {
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
    public void init(PartTypes.Pommel type, ItemMaterial material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("Material " + material + " does not allow PommelType " + type);
        }
        this.set(type, new MaterialInstance(material));
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartPommel part) {
        if (this.type != part.type) {
            return false;
        }
        return this.material.isSimilar(part.material);
    }

    @Override
    public void loseSharp(int amount) {
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("Type", this.type.getId());
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
