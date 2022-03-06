package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @Shadow
    private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructor(TextureManager textureManager, BlockColors blockColors, int maxMipmapLevels, CallbackInfo ci) {
        this.bakedRegistry = new Object2ReferenceOpenHashMap<>();
    }
}
