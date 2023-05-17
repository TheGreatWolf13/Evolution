package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public abstract class RenderType_CompositeRenderTypeMixin extends RenderType {

    public RenderType_CompositeRenderTypeMixin(String pName,
                                               VertexFormat pFormat,
                                               VertexFormat.Mode pMode,
                                               int pBufferSize,
                                               boolean pAffectsCrumbling,
                                               boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify
     */
    @Override
    @Overwrite
    public String toString() {
        return "CompositeRenderType[" + this.name + "]";
    }
}
