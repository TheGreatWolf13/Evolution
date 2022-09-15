package tgw.evolution.client.models.item.part;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.items.modular.part.ItemPart;

public abstract class ItemOverridesPart<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>,
        M extends BakedModelFinalPart<T, I, P>>
        extends ItemOverrides {

    protected final M finalModel;
    private final P part;

    public ItemOverridesPart(M finalModel, P part) {
        this.finalModel = finalModel;
        this.part = part;
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        P part = (P) stack.getCapability(CapabilityModular.PART).orElse(this.part);
        this.setModelData(part);
        return this.finalModel;
    }

    protected abstract void setModelData(P part);
}
