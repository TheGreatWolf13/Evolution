package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionDamage;

import java.util.List;

public class BladePart implements IHitPart<PartTypes.Blade> {
    private final MaterialInstance material;
    private final PartTypes.Blade type;
    private int sharpAmount;
    private int spentDurability;

    public BladePart(PartTypes.Blade type, MaterialInstance material) {
        this.material = material;
        this.type = type;
    }

    public static BladePart read(CompoundTag nbt) {
        PartTypes.Blade type = PartTypes.Blade.NULL.byName(nbt.getString("Type"));
        MaterialInstance material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        BladePart part = new BladePart(type, material);
        part.spentDurability = nbt.getInt("Durability");
        if (part.canBeSharpened()) {
            part.sharpAmount = nbt.getInt("SharpAmount");
        }
        return part;
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
    public double getAttackDamageInternal(double preAttackDamage) {
        //TODO implementation
        return preAttackDamage;
    }

    @Override
    public EvolutionDamage.Type getDamageType() {
        return this.sharpAmount > 0 ? EvolutionDamage.Type.SLASHING : EvolutionDamage.Type.CRUSHING;
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
        return this.sharpAmount;
    }

    @Override
    public PartTypes.Blade getType() {
        return this.type;
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
    public void sharp() {
        if (this.canBeSharpened()) {
            this.sharpAmount = Mth.ceil(1.5 * this.material.getHardness());
        }
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getName());
        tag.putInt("Durability", this.spentDurability);
        tag.put("MaterialInstance", this.material.write());
        if (this.canBeSharpened()) {
            tag.putInt("SharpAmount", this.sharpAmount);
        }
        return tag;
    }
}
