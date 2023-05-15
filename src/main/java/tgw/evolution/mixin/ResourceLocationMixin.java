package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceLocation.class)
public abstract class ResourceLocationMixin {

    @Shadow
    private static boolean isValidNamespace(String pNamespace) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean isValidPath(String pPath) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Prevent String[] allocation.
     */
    @Overwrite
    public static boolean isValidResourceLocation(String location) {
        int index = location.indexOf(':');
        String namespace = "minecraft";
        String path = location;
        if (index >= 0) {
            path = location.substring(index + 1);
            if (index >= 1) {
                namespace = location.substring(0, index);
            }
        }
        return isValidNamespace(namespace) && isValidPath(path);
    }

    /**
     * @author TheGreatWolf
     * @reason Prevent String[] allocation on decompose.
     */
    @Overwrite
    public static ResourceLocation of(String location, char separator) {
        int index = location.indexOf(separator);
        String namespace = "minecraft";
        String path = location;
        if (index >= 0) {
            path = location.substring(index + 1);
            if (index >= 1) {
                namespace = location.substring(0, index);
            }
        }
        return new ResourceLocation(namespace, path);
    }
}
