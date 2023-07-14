package tgw.evolution.capabilities.modular.part;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IAttachmentType;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.modular.part.ItemPart;

public interface IPartHit<T extends IAttachmentType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> extends IPart<T, I, P> {

    boolean canBeSharpened();

    SoundEvent getBlockHitSound();

    EvolutionDamage.Type getDamageType();

    double getDmgMultiplierInternal();

    ReferenceSet<Material> getEffectiveMaterials();

    float getMiningSpeed();

    int getSharpAmount();

    default boolean isSharp() {
        return this.getSharpAmount() > 0;
    }

    void loseSharp(int amount);

    void sharp();
}
