package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InputConstants.Type.class)
public abstract class InputConstants_TypeMixin {

    @Shadow @Final public static InputConstants.Type MOUSE;
    @Shadow @Final String defaultPrefix;
    @Shadow @Final private Int2ObjectMap<InputConstants.Key> map;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public InputConstants.Key getOrCreate(int keyCode) {
        InputConstants.Key key = this.map.get(keyCode);
        if (key == null) {
            int i = keyCode;
            //noinspection ConstantConditions
            if ((Object) this == MOUSE) {
                i = keyCode + 1;
            }
            String s = this.defaultPrefix + "." + i;
            key = new InputConstants.Key(s, (InputConstants.Type) (Object) this, keyCode);
            this.map.put(keyCode, key);
        }
        return key;
    }
}
