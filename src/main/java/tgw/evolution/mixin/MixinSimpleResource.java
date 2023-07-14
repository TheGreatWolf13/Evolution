package tgw.evolution.mixin;

import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.SimpleResource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.patches.PatchSimpleResource;

@Mixin(SimpleResource.class)
public abstract class MixinSimpleResource implements PatchSimpleResource {

    @Unique
    private @Nullable PackSource packSource;

    @Override
    public PackSource getModPackSource() {
        return this.packSource != null ? this.packSource : PackSource.DEFAULT;
    }

    @Override
    public void setPackSource(PackSource packSource) {
        this.packSource = packSource;
    }
}
