package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.capabilities.modular.MaterialInstance;

import java.util.List;

public class GuardPart implements IPart<PartTypes.Guard> {

    private final MaterialInstance material;
    private final PartTypes.Guard type;
    private int spentDurability;

    public GuardPart(PartTypes.Guard type, MaterialInstance material) {
        this.material = material;
        this.type = type;
    }

    public static GuardPart read(CompoundTag nbt) {
        PartTypes.Guard type = PartTypes.Guard.NULL.byName(nbt.getString("Type"));
        MaterialInstance material = MaterialInstance.read(nbt.getCompound("MaterialInstance"));
        GuardPart guard = new GuardPart(type, material);
        guard.spentDurability = nbt.getInt("Durability");
        return guard;
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
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getName());
        tag.putInt("Durability", this.spentDurability);
        tag.put("MaterialInstance", this.material.write());
        return tag;
    }
}
