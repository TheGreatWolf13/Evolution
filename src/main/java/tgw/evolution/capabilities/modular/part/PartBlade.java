package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.ItemPartBlade;

import java.util.List;

public class PartBlade implements IPartHit<PartTypes.Blade, ItemPartBlade, PartBlade> {

    public static final PartBlade DUMMY = new PartBlade();
    private MaterialInstance material = MaterialInstance.DUMMY;
    private int sharpAmount;
    private int spentDurability;
    private PartTypes.Blade type = PartTypes.Blade.NULL;

    public static PartBlade get(ItemStack stack) {
        return (PartBlade) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
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
        this.type = PartTypes.Blade.byId(nbt.getByte("Type"));
        this.material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        this.spentDurability = nbt.getInt("Durability");
        if (this.canBeSharpened()) {
            this.sharpAmount = nbt.getInt("SharpAmount");
        }
    }

    @Override
    public EvolutionDamage.Type getDamageType() {
        return this.sharpAmount > 0 ? EvolutionDamage.Type.SLASHING : EvolutionDamage.Type.CRUSHING;
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.blade." + this.type.getName() + "." + this.material.getMaterial().getName();
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
    public ReferenceSet<net.minecraft.world.level.material.Material> getEffectiveMaterials() {
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
        return this.sharpAmount;
    }

    @Override
    public PartTypes.Blade getType() {
        return this.type;
    }

    @Override
    public void init(PartTypes.Blade type, EvolutionMaterials material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("EvolutionMaterials " + material + " does not allow BladeType " + type);
        }
        this.set(type, new MaterialInstance(material));
        this.sharp();
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartBlade part) {
        if (this.type != part.type) {
            return false;
        }
        return this.material.isSimilar(part.material);
    }

    @Override
    public void loseSharp(int amount) {
        if (this.canBeSharpened()) {
            this.sharpAmount -= amount;
            if (this.sharpAmount < 0) {
                this.sharpAmount = 0;
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("Type", this.type.getId());
        tag.putInt("Durability", this.spentDurability);
        tag.put("MaterialInstance", this.material.write());
        if (this.canBeSharpened()) {
            tag.putInt("SharpAmount", this.sharpAmount);
        }
        return tag;
    }

    @Override
    public void set(PartTypes.Blade type, MaterialInstance material) {
        this.type = type;
        this.material = material;
    }

    @Override
    public void sharp() {
        if (this.canBeSharpened()) {
            this.sharpAmount = Mth.ceil(1.5 * this.material.getHardness());
        }
    }
}
