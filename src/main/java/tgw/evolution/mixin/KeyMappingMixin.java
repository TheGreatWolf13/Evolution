package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IKeyMappingPatch;

import java.util.List;
import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMappingPatch {

    private static Map<InputConstants.Key, List<KeyMapping>>[] maps;
    @Shadow private int clickCount;

    private static void initMaps() {
        //noinspection ConstantConditions
        if (maps == null) {
            maps = new Map[4];
            int i = 0;
            for (Map<InputConstants.Key, List<KeyMapping>> value : KeyBindingMapAccessor.getMap().values()) {
                maps[i++] = value;
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason This method allocates a list to them iterate through it, instead of just iterating through the map...
     */
    @Overwrite
    public static void set(InputConstants.Key key, boolean held) {
        initMaps();
        for (Map<InputConstants.Key, List<KeyMapping>> map : maps) {
            List<KeyMapping> bindings = map.get(key);
            if (bindings != null) {
                for (int i = 0, len = bindings.size(); i < len; i++) {
                    KeyMapping mapping = bindings.get(i);
                    if (mapping != null) {
                        mapping.setDown(held);
                    }
                }
            }
        }
    }

    @Override
    public boolean consumeAllClicks() {
        boolean consumeClick = this.consumeClick();
        this.clickCount = 0;
        return consumeClick;
    }

    @Shadow
    public abstract boolean consumeClick();
}
