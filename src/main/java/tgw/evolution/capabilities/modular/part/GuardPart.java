package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;

import java.util.List;

public class GuardPart implements IPart<PartTypes.Guard> {

    public static final GuardPart DUMMY = new GuardPart();
    private MaterialInstance material = MaterialInstance.DUMMY;
    private int spentDurability;
    private PartTypes.Guard type = PartTypes.Guard.NULL;

    public static GuardPart get(ItemStack stack) {
        return (GuardPart) stack.getCapability(CapabilityModular.PART).orElse(DUMMY);
    }

    @Override
    public void appendText(List<Either<FormattedText, TooltipComponent>> tooltip, int num) {
        //TODO implementation

    }

    @Override
    public void damage(int amount) {
        this.spentDurability += amount;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.type = PartTypes.Guard.byName(nbt.getString("Type"));
        this.material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        this.spentDurability = nbt.getInt("Durability");
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.guard." + this.type.getName() + "." + this.material.getMaterial().getName();
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

    @Override
    public PartTypes.Guard getType() {
        return this.type;
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getName());
        tag.putInt("Durability", this.spentDurability);
        tag.put("MaterialInstance", this.material.write());
        return tag;
    }

    @Override
    public void set(PartTypes.Guard type, MaterialInstance material) {
        this.type = type;
        this.material = material;
    }
}
