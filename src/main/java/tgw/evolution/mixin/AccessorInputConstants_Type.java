package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BiFunction;

@Mixin(InputConstants.Type.class)
public interface AccessorInputConstants_Type {

    @Mutable
    @Accessor
    void setDisplayTextSupplier(BiFunction<Integer, String, Component> displayTextSupplier);
}
