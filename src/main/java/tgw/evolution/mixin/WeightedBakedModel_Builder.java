package tgw.evolution.mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.random.WeightedEntry;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.RArrayList;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(WeightedBakedModel.Builder.class)
public abstract class WeightedBakedModel_Builder {

    @Mutable
    @Shadow
    @Final
    private List<WeightedEntry.Wrapper<BakedModel>> list;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private @Nullable ArrayList onInit() {
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/WeightedBakedModel$Builder;" +
                                                                    "list:Ljava/util/List;", opcode = Opcodes.PUTFIELD))
    private void onInit(WeightedBakedModel.Builder instance, List<WeightedEntry.Wrapper<BakedModel>> value) {
        this.list = new RArrayList<>();
    }
}
