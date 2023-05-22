package tgw.evolution.mixin;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Sheets.class)
public abstract class SheetsMixin {

    @Shadow
    @Final
    public static Map<WoodType, Material> SIGN_MATERIALS;

    @Shadow
    @Final
    public static ResourceLocation SIGN_SHEET;

    /**
     * @author TheGreatWolf
     * @reason Instantiating a RenderMaterial every time a sign tries to grab a texture identifier causes a significant performance impact as no
     * RenderLayer will ever be cached for the sprite. Minecraft already maintains a WoodType -> RenderMaterial cache but for some reason doesn't
     * use it.
     */
    @Overwrite
    private static Material createSignMaterial(WoodType type) {
        //noinspection ConstantConditions
        if (SIGN_MATERIALS != null) {
            Material sprite = SIGN_MATERIALS.get(type);
            //noinspection ConstantConditions,VariableNotUsedInsideIf
            if (type != null) {
                return sprite;
            }
        }
        ResourceLocation location = new ResourceLocation(type.name());
        return new Material(SIGN_SHEET, new ResourceLocation(location.getNamespace(), "entity/signs/" + location.getPath()));
    }
}
