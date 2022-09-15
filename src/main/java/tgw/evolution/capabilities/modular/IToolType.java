package tgw.evolution.capabilities.modular;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.items.modular.part.ItemPart;

public interface IToolType<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> extends IAttachmentType<T, I, P> {

    ReferenceSet<Material> getEffectiveMaterials();
}
