package tgw.evolution.capabilities.modular.part;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IAttachmentType;
import tgw.evolution.init.EvolutionDamage;

public interface IHitPart<T extends IAttachmentType<T>> extends IPart<T> {

    boolean canBeSharpened();

    double getAttackDamageInternal(double preAttackDamage);

    EvolutionDamage.Type getDamageType();

    ReferenceSet<Material> getEffectiveMaterials();

    float getMiningSpeed();

    int getSharpAmount();

    void loseSharp(int amount);

    void sharp();
}
