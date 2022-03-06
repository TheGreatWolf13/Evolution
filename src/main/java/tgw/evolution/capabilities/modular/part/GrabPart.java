package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.capabilities.modular.IGrabType;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.client.tooltip.EvolutionTooltipDurability;
import tgw.evolution.client.tooltip.EvolutionTooltipMass;

import java.util.List;

public abstract class GrabPart<T extends IGrabType<T>> implements IPart<T> {

    protected MaterialInstance material = MaterialInstance.DUMMY;
    protected int spentDurability;
    protected T type;

    @Override
    public void appendText(List<Either<FormattedText, TooltipComponent>> tooltip, int num) {
        tooltip.add(Either.left(this.type.getComponent()));
        this.material.appendText(tooltip);
        tooltip.add(Either.right(EvolutionTooltipMass.PARTS[num].mass(this.getMass())));
        tooltip.add(Either.right(EvolutionTooltipDurability.PARTS[num].durability(this.displayDurability(null))));
    }

    @Override
    public void damage(int amount) {
        this.spentDurability += amount;
    }

    @Override
    public final void deserializeNBT(CompoundTag nbt) {
        this.type = this.getType(nbt.getString("Type"));
        this.material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        this.spentDurability = nbt.getInt("Durability");
    }

    @Override
    public int getDurabilityDmg() {
        return this.spentDurability;
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

    protected abstract T getType(String type);

    @Override
    public T getType() {
        return this.type;
    }

    @Override
    public final CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getName());
        tag.putInt("Durability", this.spentDurability);
        tag.put("MaterialInstance", this.material.write());
        return tag;
    }

    @Override
    public void set(T type, MaterialInstance material) {
        this.type = type;
        this.material = material;
    }
}
