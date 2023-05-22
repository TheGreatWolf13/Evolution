package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @Shadow
    private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelManager;" +
                                                                    "bakedRegistry:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onConstructor(ModelManager instance, Map<ResourceLocation, BakedModel> value) {
        this.bakedRegistry = new Object2ReferenceOpenHashMap<>();
    }
}
