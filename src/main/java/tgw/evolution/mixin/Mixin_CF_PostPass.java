package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;

@Mixin(PostPass.class)
public abstract class Mixin_CF_PostPass implements AutoCloseable {

    @Unique private final OList<IntSupplier> auxAssets_;
    @Unique private final IList auxHeights_;
    @Unique private final OList<String> auxNames_;
    @Unique private final IList auxWidths_;
    @Mutable @Shadow @Final @RestoreFinal public RenderTarget inTarget;
    @Mutable @Shadow @Final @RestoreFinal public RenderTarget outTarget;
    @Shadow @Final @DeleteField private List<IntSupplier> auxAssets;
    @Shadow @Final @DeleteField private List<Integer> auxHeights;
    @Shadow @Final @DeleteField private List<String> auxNames;
    @Shadow @Final @DeleteField private List<Integer> auxWidths;
    @Mutable @Shadow @Final @RestoreFinal private EffectInstance effect;
    @Shadow private @Nullable Matrix4f shaderOrthoMatrix;

    @ModifyConstructor
    public Mixin_CF_PostPass(ResourceManager manager, String name, RenderTarget inTarget, RenderTarget outTarget) throws IOException {
        this.auxAssets_ = new OArrayList<>();
        this.auxNames_ = new OArrayList<>();
        this.auxWidths_ = new IArrayList();
        this.auxHeights_ = new IArrayList();
        this.shaderOrthoMatrix = null;
        this.effect = new EffectInstance(manager, name);
        this.inTarget = inTarget;
        this.outTarget = outTarget;
    }

    @Overwrite
    public void addAuxAsset(String name, IntSupplier asset, int width, int height) {
        this.auxNames_.add(name);
        this.auxAssets_.add(asset);
        this.auxWidths_.add(width);
        this.auxHeights_.add(height);
    }

    @Overwrite
    public void process(float partialTicks) {
        this.inTarget.unbindWrite();
        float outWidth = this.outTarget.width;
        float outHeight = this.outTarget.height;
        RenderSystem.viewport(0, 0, (int) outWidth, (int) outHeight);
        this.effect.setSampler("DiffuseSampler", this.inTarget::getColorTextureId);
        OList<IntSupplier> auxAssets_ = this.auxAssets_;
        for (int i = 0, len = auxAssets_.size(); i < len; ++i) {
            this.effect.setSampler(this.auxNames_.get(i), auxAssets_.get(i));
            this.effect.safeGetUniform(RenderHelper.auxAssetsNames(i)).set(this.auxWidths_.getInt(i), (float) this.auxHeights_.getInt(i));
        }
        assert this.shaderOrthoMatrix != null;
        this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
        this.effect.safeGetUniform("InSize").set(this.inTarget.width, (float) this.inTarget.height);
        this.effect.safeGetUniform("OutSize").set(outWidth, outHeight);
        this.effect.safeGetUniform("Time").set(partialTicks);
        Minecraft minecraft = Minecraft.getInstance();
        this.effect.safeGetUniform("ScreenSize").set(minecraft.getWindow().getWidth(), (float) minecraft.getWindow().getHeight());
        this.effect.apply();
        this.outTarget.clear(Minecraft.ON_OSX);
        this.outTarget.bindWrite(false);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.vertex(0, 0, 500).endVertex();
        builder.vertex(outWidth, 0, 500).endVertex();
        builder.vertex(outWidth, outHeight, 500).endVertex();
        builder.vertex(0, outHeight, 500).endVertex();
        builder.end();
        BufferUploader._endInternal(builder);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        this.effect.clear();
        this.outTarget.unbindWrite();
        this.inTarget.unbindRead();
        for (int i = 0, len = auxAssets_.size(); i < len; ++i) {
            if (auxAssets_.get(i) instanceof RenderTarget target) {
                target.unbindRead();
            }
        }
    }
}
