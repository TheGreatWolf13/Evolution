package tgw.evolution.mixin;

import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.capabilities.chunk.AtmStorage;
import tgw.evolution.capabilities.chunk.IntegrityStorage;
import tgw.evolution.capabilities.chunk.StabilityStorage;
import tgw.evolution.patches.PatchLevelChunkSection;

@Mixin(LevelChunkSection.class)
public abstract class MixinLevelChunkSection implements PatchLevelChunkSection {

    @Unique private AtmStorage atmStorage;
    @Unique private IntegrityStorage integrityStorage;
    @Unique private IntegrityStorage loadFactorStorage;
    @Unique private StabilityStorage stabilityStorage;

    @Override
    public AtmStorage getAtmStorage() {
        return this.atmStorage;
    }

    @Override
    public IntegrityStorage getIntegrityStorage() {
        return this.integrityStorage;
    }

    @Override
    public IntegrityStorage getLoadFactorStorage() {
        return this.loadFactorStorage;
    }

    @Override
    public StabilityStorage getStabilityStorage() {
        return this.stabilityStorage;
    }

    @Inject(method = "<init>(ILnet/minecraft/core/Registry;)V", at = @At("TAIL"))
    private void onInit(int sectionY, Registry biomeRegistry, CallbackInfo ci) {
        this.atmStorage = new AtmStorage();
        this.integrityStorage = new IntegrityStorage();
        this.loadFactorStorage = new IntegrityStorage();
        this.stabilityStorage = new StabilityStorage();
    }

    @Override
    public void setAtmStorage(AtmStorage atm) {
        this.atmStorage = atm;
    }

    @Override
    public void setIntegrityStorage(IntegrityStorage storage) {
        this.integrityStorage = storage;
    }

    @Override
    public void setLoadFactorStorage(IntegrityStorage storage) {
        this.loadFactorStorage = storage;
    }

    @Override
    public void setStabilityStorage(StabilityStorage stabilityStorage) {
        this.stabilityStorage = stabilityStorage;
    }
}
