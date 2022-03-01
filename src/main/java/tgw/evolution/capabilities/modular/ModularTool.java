package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.GrabPart;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.ItemModular;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.math.MathHelper;

import java.util.List;

public class ModularTool implements IModularTool {

    private GrabPart<PartTypes.Handle> handle;
    private HeadPart head;

    @Override
    public void appendTooltip(List<Either<FormattedText, TooltipComponent>> tooltip) {
        if (!this.isInit()) {
            return;
        }
        this.head.appendText(tooltip, 0);
        tooltip.add(Either.left(EvolutionTexts.EMPTY));
        this.handle.appendText(tooltip, 1);
    }

    @Override
    public void damage(ItemModular.DamageCause cause) {
        if (!this.isInit()) {
            return;
        }
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
    public void deserializeNBT(CompoundTag nbt) {
        this.handle = GrabPart.read(nbt.getCompound("Handle"), PartTypes.Handle.NULL);
        this.head = HeadPart.read(nbt.getCompound("Head"));
    }

    @Override
    public double getAttackDamage() {
        if (!this.isInit()) {
            return 0;
        }
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
        return 0;
    }

    @Override
    public int getBackPriority() {
        if (!this.isInit()) {
            return -1;
        }
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
        if (!this.isInit()) {
            return EvolutionDamage.Type.GENERIC;
        }
        return this.head.getDamageType();
    }

    @Override
    public String getDescriptionId() {
        if (!this.isInit()) {
            return "null";
        }
        return "item.evolution." + this.head.getType().getName() + "." + this.head.getMaterial().getName();
    }

    @Override
    public ReferenceSet<Material> getEffectiveMaterials() {
        if (!this.isInit()) {
            return ReferenceSet.of();
        }
        return this.head.getEffectiveMaterials();
    }

    @Override
    public GrabPart<PartTypes.Handle> getHandle() {
        return this.handle;
    }

    @Override
    public ItemMaterial getHandleMaterial() {
        if (!this.isInit()) {
            return ItemMaterial.WOOD;
        }
        return this.handle.getMaterial().getMaterial();
    }

    @Override
    public PartTypes.Handle getHandleType() {
        if (!this.isInit()) {
            return PartTypes.Handle.NULL;
        }
        return this.handle.getType();
    }

    @Override
    public int getHarvestLevel() {
        if (!this.isInit()) {
            return HarvestLevel.HAND;
        }
        return this.head.getHarvestLevel();
    }

    @Override
    public HeadPart getHead() {
        return this.head;
    }

    @Override
    public ItemMaterial getHeadMaterial() {
        if (!this.isInit()) {
            return ItemMaterial.STONE_ANDESITE;
        }
        return this.head.getMaterial().getMaterial();
    }

    @Override
    public PartTypes.Head getHeadType() {
        if (!this.isInit()) {
            return PartTypes.Head.NULL;
        }
        return this.head.getType();
    }

    @Override
    public double getMass() {
        if (!this.isInit()) {
            return 0;
        }
        return this.head.getMass() + this.handle.getMass();
    }

    @Override
    public float getMiningSpeed() {
        if (!this.isInit()) {
            return 0;
        }
        return this.head.getMiningSpeed();
    }

    @Override
    public double getMoment() {
        if (!this.isInit()) {
            return 0;
        }
        int handleLength = this.handle.getType().getLength();
        double grabPoint = this.handle.getType().getGrabPoint();
        double handleArm = handleLength / 2.0 - grabPoint;
        double headArm = this.head.getType().getRelativeCenterOfMass(handleLength) - grabPoint;
        return handleArm * this.handle.getMass() + headArm * this.head.getMass();
    }

    @Override
    public double getReach() {
        //TODO implementation
        return 0;
    }

    @Override
    public int getTotalDurabilityDmg() {
        if (!this.isInit()) {
            return 0;
        }
        return this.head.getDurabilityDmg() + this.handle.getDurabilityDmg();
    }

    @Override
    public int getTotalMaxDurability() {
        if (!this.isInit()) {
            return 0;
        }
        return this.head.getMaxDurability() + this.handle.getMaxDurability();
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isInit() {
        return this.head != null && this.handle != null;
    }

    @Override
    public boolean isSharpened() {
        if (!this.isInit()) {
            return false;
        }
        return this.head.getSharpAmount() > 0;
    }

    @Override
    public boolean isTwoHanded() {
        if (!this.isInit()) {
            return false;
        }
        return this.head.getType().isTwoHanded() || this.handle.getType().isTwoHanded();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!this.isInit()) {
            return tag;
        }
        tag.put("Handle", this.handle.write());
        tag.put("Head", this.head.write());
        return tag;
    }

    @Override
    public void setDurabilityDmg(int damage) {

    }

    @Override
    public void setHandle(GrabPart<PartTypes.Handle> handle) {
        this.handle = handle;
    }

    @Override
    public void setHead(HeadPart head) {
        this.head = head;
    }

    @Override
    public void sharp() {
        if (!this.isInit()) {
            return;
        }
        this.head.sharp();
    }
}
