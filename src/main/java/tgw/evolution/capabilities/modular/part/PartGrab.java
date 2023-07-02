package tgw.evolution.capabilities.modular.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.IGrabType;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.client.tooltip.TooltipDurability;
import tgw.evolution.client.tooltip.TooltipMass;
import tgw.evolution.items.modular.part.ItemPart;
import tgw.evolution.util.collection.EitherList;

public abstract class PartGrab<T extends IGrabType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> implements IPart<T, I, P> {

    protected MaterialInstance material = MaterialInstance.DUMMY;
    protected int spentDurability;
    protected @Nullable CompoundTag tag;
    protected T type;

    public PartGrab(T type) {
        this.type = type;
    }

    @Override
    public void appendText(EitherList<FormattedText, TooltipComponent> tooltip, int num) {
        tooltip.addLeft(this.type.getComponent());
        this.material.appendText(tooltip);
        tooltip.addRight(TooltipMass.part(num, this.getMass()));
        tooltip.addRight(TooltipDurability.part(num, this.displayDurability(ItemStack.EMPTY)));
    }

    @Override
    public void damage(int amount) {
        this.spentDurability += amount;
    }

    @Override
    public final void deserializeNBT(CompoundTag nbt) {
        this.type = this.getType(nbt.getByte("Type"));
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
    public MaterialInstance getMaterialInstance() {
        return this.material;
    }

    @Override
    public int getMaxDurability() {
        return Mth.ceil(this.material.getHardness() * this.material.getResistance() / 50.0);
    }

    protected abstract T getType(byte id);

    @Override
    public T getType() {
        return this.type;
    }

    @Override
    public final CompoundTag serializeNBT() {
        if (this.tag == null) {
            this.tag = new CompoundTag();
        }
        this.tag.putByte("Type", this.type.getId());
        this.tag.putInt("Durability", this.spentDurability);
        this.tag.put("MaterialInstance", this.material.write());
        return this.tag;
    }

    @Override
    public void set(T type, MaterialInstance material) {
        this.type = type;
        this.material = material;
    }
}
