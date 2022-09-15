package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.OArrayList;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

@Mixin(NonNullList.class)
public abstract class NonNullListMixin<E> extends AbstractList<E> {

    @ModifyArg(method = "create", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;<init>(Ljava/util/List;Ljava/lang/Object;)V"), index = 0)
    private static List modifyCreate(List pList) {
        return new OArrayList();
    }

    @Nullable
    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private static ArrayList proxyCreate() {
        return null;
    }
}
