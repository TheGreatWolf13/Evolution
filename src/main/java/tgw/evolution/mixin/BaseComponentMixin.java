package tgw.evolution.mixin;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/BaseComponent;siblings:Ljava/util/List;", opcode =
            Opcodes.PUTFIELD))
    private void onInit(BaseComponent instance, List<Component> value) {
        this.siblings = new RArrayList<>();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private @Nullable ArrayList onInit() {
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
