package tgw.evolution.mixin;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;

@Mixin(ResourceLocation.class)
public abstract class Mixin_CF_ResourceLocation {

    @Mutable @Shadow @RestoreFinal @Final protected String namespace;
    @Mutable @Shadow @RestoreFinal @Final protected String path;

    @ModifyConstructor
    public Mixin_CF_ResourceLocation(String string) {
        int index = string.indexOf(58);
        String n = "minecraft";
        String p = string;
        if (index >= 0) {
            p = string.substring(index + 1);
            if (index >= 1) {
                n = string.substring(0, index);
            }
        }
        if (!isValidNamespace(n)) {
            throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + n + ":" + p);
        }
        if (!isValidPath(p)) {
            throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + n + ":" + p);
        }
        this.namespace = n;
        this.path = p;
    }

    @ModifyConstructor
    public Mixin_CF_ResourceLocation(String n, String p) {
        if (!isValidNamespace(n)) {
            throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + n + ":" + p);
        }
        if (!isValidPath(p)) {
            throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + n + ":" + p);
        }
        this.namespace = StringUtils.isEmpty(n) ? "minecraft" : n;
        this.path = p;
    }

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
