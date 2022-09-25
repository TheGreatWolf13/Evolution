package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.client.tooltip.EvolutionTooltipDurability;
import tgw.evolution.client.tooltip.EvolutionTooltipMass;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartHead;

import java.util.List;

public class PartHead implements IPartHit<PartTypes.Head, ItemPartHead, PartHead> {

    public static final PartHead DUMMY = new PartHead();
    private MaterialInstance material = MaterialInstance.DUMMY;
    private int sharpAmount;
    private int spentDurability;
    private @Nullable CompoundTag tag;
    private PartTypes.Head type = PartTypes.Head.NULL;

    public static PartHead get(ItemStack stack) {
        return (PartHead) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
    }

    @Override
    public void appendText(List<Either<FormattedText, TooltipComponent>> tooltip, int num) {
        tooltip.add(Either.left(this.type.getComponent()));
        this.material.appendText(tooltip);
        if (this.canBeSharpened()) {
            tooltip.add(Either.left(EvolutionTexts.sharp(this.sharpAmount, this.material.getHardness())));
        }
        tooltip.add(Either.right(EvolutionTooltipMass.PARTS[num].mass(this.getMass())));
        tooltip.add(Either.right(EvolutionTooltipDurability.PARTS[num].durability(this.displayDurability(ItemStack.EMPTY))));
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
        this.type = PartTypes.Head.byId(nbt.getByte("Type"));
        this.material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        this.spentDurability = nbt.getInt("Durability");
        if (this.canBeSharpened()) {
            this.sharpAmount = nbt.getInt("SharpAmount");
        }
    }

    @Override
    public double getAttackDamageInternal(double preAttackDamage) {
        if (this.canBeSharpened()) {
            if (this.sharpAmount > this.material.getHardness()) {
                //Very Sharp
                return 1.15 * preAttackDamage;
            }
        }
        if (this.type == PartTypes.Head.HOE) {
            return 0.7 * preAttackDamage;
        }
        return preAttackDamage;
    }

    @Override
    public EvolutionDamage.Type getDamageType() {
        return switch (this.type) {
            case AXE -> this.sharpAmount > 0 ? EvolutionDamage.Type.SLASHING : EvolutionDamage.Type.CRUSHING;
            case HOE -> EvolutionDamage.Type.SLASHING;
            case SPEAR, PICKAXE -> EvolutionDamage.Type.PIERCING;
            case MACE, HAMMER, SHOVEL -> EvolutionDamage.Type.CRUSHING;
            case NULL -> EvolutionDamage.Type.GENERIC;
        };
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.head." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    public int getDurabilityDmg() {
        return this.spentDurability;
    }

    @Override
    public ReferenceSet<Material> getEffectiveMaterials() {
        return this.type.getEffectiveMaterials();
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
        return this.material.getElasticModulus() * this.material.getHardness() / 500.0f;
    }

    @Override
    public int getSharpAmount() {
        return this.canBeSharpened() ? this.sharpAmount : 0;
    }

    @Override
    public PartTypes.Head getType() {
        return this.type;
    }

    @Override
    public void init(PartTypes.Head type, ItemMaterial material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("Material " + material + " does not allow HeadType " + type);
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
    public boolean isSimilar(PartHead part) {
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
        if (this.tag == null) {
            this.tag = new CompoundTag();
        }
        this.tag.putByte("Type", this.type.getId());
        this.tag.putInt("Durability", this.spentDurability);
        this.tag.put("MaterialInstance", this.material.write());
        if (this.canBeSharpened()) {
            this.tag.putInt("SharpAmount", this.sharpAmount);
        }
        else {
            this.tag.remove("SharpAmount");
        }
        return this.tag;
    }

    @Override
    public void set(PartTypes.Head type, MaterialInstance material) {
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
