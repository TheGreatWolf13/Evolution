package tgw.evolution.mixin;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;

import java.util.Locale;

@Mixin(ModelResourceLocation.class)
public abstract class Mixin_CF_ModelResourceLocation extends ResourceLocation {

    @Mutable @Shadow @RestoreFinal @Final private String variant;

    @ModifyConstructor
    public Mixin_CF_ModelResourceLocation(String string, String string2, String string3) {
        super(string, string2);
        this.variant = string3.toLowerCase(Locale.ROOT);
    }

    @ModifyConstructor
    public Mixin_CF_ModelResourceLocation(String string) {
        super(string.substring(0, string.indexOf(35)));
        this.variant = string.substring(string.indexOf(35) + 1).toLowerCase(Locale.ROOT);
    }

    @ModifyConstructor
    public Mixin_CF_ModelResourceLocation(ResourceLocation resourceLocation, String string) {
        this(resourceLocation.getNamespace(), resourceLocation.getPath(), string);
    }

    @ModifyConstructor
    public Mixin_CF_ModelResourceLocation(String string, String string2) {
        super(string);
        this.variant = string2.toLowerCase(Locale.ROOT);
    }
}
