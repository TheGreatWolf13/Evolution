package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.HandlePart;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.math.MathHelper;

import java.util.List;

public class ModularTool implements IModularTool {

    private final HandlePart handle = new HandlePart();
    private final HeadPart head = new HeadPart();
    private CompoundTag tag;

    @Override
    public void appendTooltip(List<Either<FormattedText, TooltipComponent>> tooltip) {
        this.head.appendText(tooltip, 0);
        tooltip.add(Either.left(EvolutionTexts.EMPTY));
        this.handle.appendText(tooltip, 1);
    }

    private void damage(ItemModular.DamageCause cause) {
        switch (cause) {
            case BREAK_BAD_BLOCK -> {
                this.head.damage(1);
                this.head.loseSharp(2);
                if (MathHelper.RANDOM.nextFloat() < 0.15f) {
                    this.handle.damage(1);
                }
            }
            case BREAK_BLOCK -> {
                this.head.damage(1);
                this.head.loseSharp(1);
                if (MathHelper.RANDOM.nextFloat() < 0.1f) {
                    this.handle.damage(1);
                }
            }
            case HIT_ENTITY -> {
                switch (this.head.getType()) {
                    case AXE, MACE, SPEAR -> {
                        this.head.damage(1);
                        this.head.loseSharp(1);
                    }
                    default -> {
                        this.head.damage(2);
                        this.head.loseSharp(1);
                    }
                }
                if (MathHelper.RANDOM.nextFloat() < 0.1f) {
                    this.handle.damage(1);
                }
            }
        }
    }

    @Override
    public void damage(ItemModular.DamageCause cause, @HarvestLevel int harvestLevel) {
        int toolLevel = this.getHarvestLevel();
        int delta = toolLevel - harvestLevel;
        if (delta == 0) {
            this.damage(cause);
            return;
        }
        if (delta > 0) {
            if (!(MathHelper.RANDOM.nextFloat() < delta * 0.1f)) {
                this.damage(cause);
            }
            return;
        }
        this.damage(cause);
        if (MathHelper.RANDOM.nextFloat() < delta * -0.1f) {
            this.damage(cause);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.handle.deserializeNBT(nbt.getCompound("Handle"));
        this.head.deserializeNBT(nbt.getCompound("Head"));
    }

    @Override
    public double getAttackDamage() {
        double damage = switch (this.getDamageType()) {
            case PIERCING -> this.head.getMaterial().getElasticModulus() / 3.5;
            case CRUSHING -> this.getMoment() * 12.5; //TODO divide by area
            case SLASHING -> 0.87 * (0.65 * this.head.getMaterial().getElasticModulus() / 3.5 + 0.35 * this.getMoment() * 12.5);
            default -> 0;
        };
        return this.head.getAttackDamageInternal(damage);
    }

    @Override
    public double getAttackSpeed() {
        //TODO implementation
        return 1;
    }

    @Override
    public int getBackPriority() {
        return switch (this.head.getType()) {
            case NULL -> -1;
            case AXE -> 2;
            case HAMMER, PICKAXE -> 3;
            case HOE -> 5;
            case MACE, SPEAR -> 1;
            case SHOVEL -> 4;
        };
    }

    @Override
    public EvolutionDamage.Type getDamageType() {
        return this.head.getDamageType();
    }

    @Override
    public String getDescriptionId() {
        if (this.head.getType() == PartTypes.Head.SPEAR) {
            if (!this.isTwoHanded()) {
                return "item.evolution.javelin." + this.head.getMaterial().getName();
            }
        }
        return "item.evolution." + this.head.getType().getName() + "." + this.head.getMaterial().getName();
    }

    @Override
    public ReferenceSet<Material> getEffectiveMaterials() {
        return this.head.getEffectiveMaterials();
    }

    @Override
    public HandlePart getHandle() {
        return this.handle;
    }

    @Override
    public int getHarvestLevel() {
        return this.head.getHarvestLevel();
    }

    @Override
    public HeadPart getHead() {
        return this.head;
    }

    @Override
    public double getMass() {
        return this.head.getMass() + this.handle.getMass();
    }

    @Override
    public float getMiningSpeed() {
        return this.head.getMiningSpeed();
    }

    @Override
    public double getMoment() {
        int handleLength = this.handle.getType().getLength();
        double grabPoint = this.handle.getType().getGrabPoint();
        double handleArm = handleLength / 2.0 - grabPoint;
        double headArm = this.head.getType().getRelativeCenterOfMass(handleLength) - grabPoint;
        return handleArm * this.handle.getMass() + headArm * this.head.getMass();
    }

    @Override
    public int getTotalDurabilityDmg() {
        return this.head.getDurabilityDmg() + this.handle.getDurabilityDmg();
    }

    @Override
    public int getTotalMaxDurability() {
        return this.head.getMaxDurability() + this.handle.getMaxDurability();
    }

    @Override
    public boolean isAxe() {
        return this.head.getType() == PartTypes.Head.AXE;
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSharpened() {
        return this.head.getSharpAmount() > 0;
    }

    @Override
    public boolean isTwoHanded() {
        return this.head.getType().isTwoHanded() || this.handle.getType().isTwoHanded();
    }

    @Override
    public CompoundTag serializeNBT() {
        if (this.tag == null) {
            this.tag = new CompoundTag();
        }
        this.tag.put("Handle", this.handle.serializeNBT());
        this.tag.put("Head", this.head.serializeNBT());
        return this.tag;
    }

    @Override
    public void setHandle(PartTypes.Handle handleType, MaterialInstance material) {
        this.handle.set(handleType, material);
    }

    @Override
    public void setHead(PartTypes.Head headType, MaterialInstance material) {
        this.head.set(headType, material);
    }

    @Override
    public void sharp() {
        this.head.sharp();
    }
}
