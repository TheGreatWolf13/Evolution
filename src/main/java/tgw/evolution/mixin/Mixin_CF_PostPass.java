package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.Vec3f;
import tgw.evolution.util.math.VectorUtil;
import tgw.evolution.util.physics.EarthHelper;

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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void addAuxAsset(String name, IntSupplier asset, int width, int height) {
        this.auxNames_.add(name);
        this.auxAssets_.add(asset);
        this.auxWidths_.add(width);
        this.auxHeights_.add(height);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void process(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;
        this.inTarget.unbindWrite();
        float outWidth = this.outTarget.width;
        float outHeight = this.outTarget.height;
        RenderSystem.viewport(0, 0, (int) outWidth, (int) outHeight);
        this.effect.setSampler("DiffuseSampler", this.inTarget::getColorTextureId);
        OList<IntSupplier> auxAssets_ = this.auxAssets_;
        for (int i = 0, len = auxAssets_.size(); i < len; ++i) {
            this.effect.setSampler(this.auxNames_.get(i), auxAssets_.get(i));
            Uniform uniform = this.effect.getUniform(RenderHelper.auxAssetsNames(i));
            if (uniform != null) {
                uniform.set((float) this.auxWidths_.getInt(i), this.auxHeights_.getInt(i));
            }
        }
        assert this.shaderOrthoMatrix != null;
        Uniform PROJ_MAT = this.effect.getUniform("ProjMat");
        if (PROJ_MAT != null) {
            PROJ_MAT.set(this.shaderOrthoMatrix);
        }
        Uniform IN_SIZE = this.effect.getUniform("InSize");
        if (IN_SIZE != null) {
            IN_SIZE.set((float) this.inTarget.width, this.inTarget.height);
        }
        Uniform OUT_SIZE = this.effect.getUniform("OutSize");
        if (OUT_SIZE != null) {
            OUT_SIZE.set(outWidth, outHeight);
        }
        Uniform TIME = this.effect.getUniform("Time");
        if (TIME != null) {
            TIME.set(partialTicks);
        }
        Uniform REAL_PROJ_MAT = this.effect.getUniform("RealProjMat");
        if (REAL_PROJ_MAT != null) {
            REAL_PROJ_MAT.set(ClientEvents.retrieveProjMatrix());
        }
        Uniform MODEL_VIEW = this.effect.getUniform("ModelViewM");
        if (MODEL_VIEW != null) {
            MODEL_VIEW.set(ClientEvents.retrieveModelViewMatrix());
        }
        Uniform SUN_DIR = this.effect.getUniform("SunDir");
        if (SUN_DIR != null) {
            Vec3f sunDir = EarthHelper.getSunDir();
            float norm = VectorUtil.norm(sunDir.x, sunDir.y, sunDir.z);
            SUN_DIR.set(sunDir.x * norm, sunDir.y * norm, sunDir.z * norm);
        }
        Uniform MOON_DIR = this.effect.getUniform("MoonDir");
        if (MOON_DIR != null) {
            Vec3f moonDir = EarthHelper.getMoonDir();
            float norm = VectorUtil.norm(moonDir.x, moonDir.y, moonDir.z);
            MOON_DIR.set(moonDir.x * norm, moonDir.y * norm, moonDir.z * norm);
        }
        Uniform FOV = this.effect.getUniform("Fov");
        if (FOV != null) {
            FOV.set(ClientEvents.retrieveFov());
        }
        Uniform FOG_COLOR = this.effect.getUniform("FogColor");
        if (FOG_COLOR != null) {
            FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        Uniform FOG_END = this.effect.getUniform("FogEnd");
        if (FOG_END != null) {
            FOG_END.set(1 / RenderSystem.getShaderFogEnd());
        }
        Uniform RAIN = this.effect.getUniform("Rain");
        if (RAIN != null) {
            RAIN.set(mc.level.getRainLevel(partialTicks));
        }
        Uniform DIM = this.effect.getUniform("Dim");
        if (DIM != null) {
            DimensionType dimensionType = mc.level.dimensionType();
            if (dimensionType.hasSkyLight()) {
                DIM.set(1.0f);
            }
            else if (dimensionType.createDragonFight()) {
                DIM.set(2.0f);
            }
            else if (dimensionType.ultraWarm()) {
                DIM.set(3.0f);
            }
            else {
                DIM.set(0.0f);
            }
        }
        Uniform UNDERWATER = this.effect.getUniform("UnderWater");
        if (UNDERWATER != null) {
            UNDERWATER.set(mc.gameRenderer.getMainCamera().getFluidInCamera() == FogType.WATER ? 1.0f : 0.0f);
        }
        Uniform FAR = this.effect.getUniform("Far");
        if (FAR != null) {
            FAR.set(mc.options.renderDistance * 16.0f);
        }
        Uniform CAVE = this.effect.getUniform("Cave");
        if (CAVE != null) {
            CAVE.set(1 - mc.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue_(mc.gameRenderer.getMainCamera().getBlockPosition().asLong()) / 15.0f);
        }
        Uniform SCREEN_SIZE = this.effect.getUniform("ScreenSize");
        if (SCREEN_SIZE != null) {
            SCREEN_SIZE.set((float) mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }
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
