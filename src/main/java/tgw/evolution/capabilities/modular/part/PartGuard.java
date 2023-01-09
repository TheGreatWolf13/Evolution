package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.Material;
import tgw.evolution.items.modular.part.ItemPartGuard;

import java.util.List;

public class PartGuard implements IPart<PartTypes.Guard, ItemPartGuard, PartGuard> {

    public static final PartGuard DUMMY = new PartGuard();
    private MaterialInstance material = MaterialInstance.DUMMY;
    private int spentDurability;
    private PartTypes.Guard type = PartTypes.Guard.NULL;

    public static PartGuard get(ItemStack stack) {
        return (PartGuard) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
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
        this.type = PartTypes.Guard.byId(nbt.getByte("Type"));
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
    public MaterialInstance getMaterialInstance() {
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
    public void init(PartTypes.Guard type, Material material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("Material " + material + " does not allow GuardPart " + type);
        }
        this.set(type, new MaterialInstance(material));
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartGuard part) {
        if (this.type != part.type) {
            return false;
        }
        return this.material.isSimilar(part.material);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("Type", this.type.getId());
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
