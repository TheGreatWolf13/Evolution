package tgw.evolution.patches;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import tgw.evolution.util.collection.lists.OList;

import java.util.Map;

public interface PatchSimpleBakedModel {

    default void set(OList<BakedQuad> unculled, Map<Direction, OList<BakedQuad>> culled) {
        throw new AbstractMethodError();
    }
}
