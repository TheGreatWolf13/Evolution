package tgw.evolution.mixin;

import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.capabilities.chunk.AtmStorage;
import tgw.evolution.patches.PatchLevelChunkSection;

@Mixin(LevelChunkSection.class)
public abstract class MixinLevelChunkSection implements PatchLevelChunkSection {

    @Unique private AtmStorage atmStorage;

    @Override
    public AtmStorage getAtmStorage() {
        return this.atmStorage;
    }

    @Inject(method = "<init>(ILnet/minecraft/core/Registry;)V", at = @At("TAIL"))
    private void onInit(int sectionY, Registry biomeRegistry, CallbackInfo ci) {
        this.atmStorage = new AtmStorage();
    }

    @Override
    public void setAtmStorage(AtmStorage atm) {
        this.atmStorage = atm;
    }
}
