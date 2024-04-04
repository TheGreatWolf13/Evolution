package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import tgw.evolution.util.collection.lists.OList;

public interface PatchVertexFormat {

    default OList<VertexFormatElement> getElements_() {
        throw new AbstractMethodError();
    }
}
