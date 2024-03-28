package tgw.evolution.mixin;

import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PresetFlatWorldScreen.class)
public abstract class MixinPresetFlatWorldScreen extends Screen {

    @Shadow @Final private static Logger LOGGER;

    public MixinPresetFlatWorldScreen(Component component) {
        super(component);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static @Nullable FlatLayerInfo getLayerInfoFromString(String string, int i) {
        String[] strings = string.split("\\*", 2);
        int count;
        if (strings.length == 2) {
            try {
                count = Math.max(Integer.parseInt(strings[0]), 0);
            }
            catch (NumberFormatException e) {
                LOGGER.error("Error while parsing flat world string => {}", e.getMessage());
                return null;
            }
        }
        else {
            count = 1;
        }
        String blockName = strings[strings.length - 1];
        Block block;
        try {
            block = (Block) Registry.BLOCK.getNullable(new ResourceLocation(blockName));
        }
        catch (Exception e) {
            LOGGER.error("Error while parsing flat world string => {}", e.getMessage());
            return null;
        }
        if (block == null) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", blockName);
            return null;
        }
        return new FlatLayerInfo(Math.min(i + count, DimensionType.Y_SIZE) - i, block);
    }
}
