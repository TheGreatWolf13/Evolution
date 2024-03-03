package tgw.evolution.mixin;

import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.patches.PatchClientboundLightUpdatePacketData;

import java.util.BitSet;
import java.util.List;

@Mixin(ClientboundLightUpdatePacketData.class)
public abstract class MixinClientboundLightUpdatePacketData implements PatchClientboundLightUpdatePacketData {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void prepareSectionData(ChunkPos pos, LevelLightEngine engine, LightLayer layer, int index, BitSet yMask, BitSet emptyYMask, List<byte[]> list) {
        byte[] dataLayer = engine.getLayerListener(layer).getDataLayerData_(pos.x, engine.getMinLightSection() + index, pos.z);
        if (dataLayer != null) {
            if (dataLayer.length == 0) {
                emptyYMask.set(index);
            }
            else {
                yMask.set(index);
                list.add(dataLayer);
            }
        }
    }
}
