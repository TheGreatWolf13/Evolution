package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import org.jetbrains.annotations.Nullable;

@Mixin(RegistryObject.class)
public abstract class RegistryObjectMixin<T> {

    @Shadow
    @Final
    private ResourceLocation name;
    @Shadow
    @Nullable
    private T value;

    /**
     * @author TheGreatWolf
     * @reason Prevent lambda and string allocation when the value is not {@code null}.
     */
    @Overwrite
    @NotNull
    public T get() {
        T ret = this.value;
        if (ret == null) {
            throw new NullPointerException("Registry Object not present: " + this.name);
        }
        return ret;
    }
}
