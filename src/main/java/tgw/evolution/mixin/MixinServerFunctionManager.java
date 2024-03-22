package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.collection.lists.OList;

@Mixin(ServerFunctionManager.class)
public abstract class MixinServerFunctionManager implements IKeyedReloadListener {

    @Unique private static final OList<ResourceLocation> DEPENDENCY = OList.of(ReloadListernerKeys.TAGS);

    @Override
    public OList<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.FUNCTIONS;
    }
}
