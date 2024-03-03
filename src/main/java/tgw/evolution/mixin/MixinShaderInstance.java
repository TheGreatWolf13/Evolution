package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(ShaderInstance.class)
public abstract class MixinShaderInstance implements Shader, AutoCloseable {

    @Shadow private static ShaderInstance lastAppliedShader;
    @Shadow private static int lastProgramId;
    @Shadow @Final private BlendMode blend;
    @Shadow private boolean dirty;
    @Shadow @Final private int programId;
    @Shadow @Final private List<Integer> samplerLocations;
    @Shadow @Final private Map<String, Object> samplerMap;
    @Shadow @Final private List<String> samplerNames;
    @Shadow @Final private List<Uniform> uniforms;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void apply() {
        RenderSystem.assertOnRenderThread();
        this.dirty = false;
        lastAppliedShader = (ShaderInstance) (Object) this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }
        int activeTexture = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.samplerLocations.size(); ++j) {
            String string = this.samplerNames.get(j);
            if (this.samplerMap.get(string) != null) {
                int k = Uniform.glGetUniformLocation(this.programId, string);
                Uniform.uploadInteger(k, j);
                RenderSystem.activeTexture('蓀' + j);
                RenderSystem.enableTexture();
                Object object = this.samplerMap.get(string);
                int l = -1;
                if (object instanceof RenderTarget renderTarget) {
                    l = renderTarget.getColorTextureId();
                }
                else if (object instanceof AbstractTexture texture) {
                    l = texture.getId();
                }
                else if (object instanceof Integer i) {
                    l = i;
                }
                if (l != -1) {
                    RenderSystem.bindTexture(l);
                }
            }
        }
        GlStateManager._activeTexture(activeTexture);
        for (int i = 0, len = this.uniforms.size(); i < len; ++i) {
            this.uniforms.get(i).upload();
        }
    }
}
