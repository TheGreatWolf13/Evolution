package tgw.evolution.mixin;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IComponentPatch;
import tgw.evolution.util.collection.RArrayList;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(BaseComponent.class)
public abstract class BaseComponentMixin implements IComponentPatch {

    @Mutable
    @Shadow
    @Final
    protected List<Component> siblings;

    @Shadow
    @javax.annotation.Nullable
    private Language decomposedWith;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.siblings = new RArrayList<>();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private @Nullable ArrayList proxyInit() {
        return null;
    }

    @Override
    public void resetCache() {
        this.decomposedWith = null;
        for (int i = 0, l = this.siblings.size(); i < l; i++) {
            ((IComponentPatch) this.siblings.get(i)).resetCache();
        }
    }
}
