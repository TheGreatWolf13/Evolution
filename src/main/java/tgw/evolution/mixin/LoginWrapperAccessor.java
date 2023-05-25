package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.LoginWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoginWrapper.class)
public interface LoginWrapperAccessor {

    @Accessor(remap = false, value = "WRAPPER")
    static ResourceLocation getWrapper() {
        throw new AbstractMethodError();
    }
}
