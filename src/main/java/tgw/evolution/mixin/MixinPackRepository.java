package tgw.evolution.mixin;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.resources.ModdedPackSource;

import java.util.HashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public abstract class MixinPackRepository {

    @Mutable @Shadow @Final public Set<RepositorySource> sources;

    @Inject(method = "<init>(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)" +
                     "V", at = @At("RETURN"))
    public void construct(Pack.PackConstructor arg, RepositorySource[] resourcePackProviders, CallbackInfo info) {
        this.sources = new HashSet<>(this.sources);
        // Search resource pack providers to find any server-related pack provider.
        boolean shouldAddServerProvider = false;
        for (RepositorySource provider : this.sources) {
            if (provider instanceof FolderRepositorySource f && (f.packSource == PackSource.WORLD || f.packSource == PackSource.SERVER)) {
                shouldAddServerProvider = true;
                break;
            }
        }
        // On server, add the mod resource pack provider.
        if (shouldAddServerProvider) {
            this.sources.add(new ModdedPackSource(PackType.SERVER_DATA));
        }
    }
}
