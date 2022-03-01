package tgw.evolution.capabilities.modular;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.IPartType;

public interface IToolType<T extends IPartType<T>> extends IAttachmentType<T> {

    ReferenceSet<Material> getEffectiveMaterials();
}
