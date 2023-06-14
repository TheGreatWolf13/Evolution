package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mixin(KeyBindingMap.class)
public interface KeyBindingMapAccessor {

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Accessor(remap = false)
    static EnumMap<KeyModifier, Map<InputConstants.Key, List<KeyMapping>>> getMap() {
        throw new AbstractMethodError();
    }
}
