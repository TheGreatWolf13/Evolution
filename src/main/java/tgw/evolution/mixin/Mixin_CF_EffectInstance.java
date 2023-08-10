package tgw.evolution.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

@Mixin(EffectInstance.class)
public abstract class Mixin_CF_EffectInstance implements Effect, AutoCloseable {

    @Shadow @Final private static Logger LOGGER;
    @Shadow private static int lastProgramId;
    @Shadow private static @Nullable EffectInstance lastAppliedEffect;
    @Shadow @Final @DeleteField private List<String> attributeNames;
    @Unique private @Nullable OList<String> attributeNames_;
    @Shadow @Final @DeleteField private List<Integer> attributes;
    @Unique private @Nullable IList attributes_;
    @Mutable @Shadow @Final @RestoreFinal private BlendMode blend;
    @Shadow private boolean dirty;
    @Mutable @Shadow @Final @RestoreFinal private EffectProgram fragmentProgram;
    @Mutable @Shadow @Final @RestoreFinal private String name;
    @Mutable @Shadow @Final @RestoreFinal private int programId;
    @Shadow @Final @DeleteField private List<Integer> samplerLocations;
    @Unique private IList samplerLocations_;
    @Shadow @Final @DeleteField private Map<String, IntSupplier> samplerMap;
    @Unique private O2OMap<String, IntSupplier> samplerMap_;
    @Shadow @Final @DeleteField private List<String> samplerNames;
    @Unique private OList<String> samplerNames_;
    @Shadow @Final @DeleteField private List<Integer> uniformLocations;
    @Unique private IList uniformLocations_;
    @Shadow @Final @DeleteField private Map<String, Uniform> uniformMap;
    @Unique private O2OMap<String, Uniform> uniformMap_;
    @Shadow @Final @DeleteField private List<Uniform> uniforms;
    @Unique private OList<Uniform> uniforms_;
    @Mutable @Shadow @Final @RestoreFinal private EffectProgram vertexProgram;

    @DummyConstructor
    public Mixin_CF_EffectInstance(String name) {
        this.name = name;
    }

    @ModifyConstructor
    public Mixin_CF_EffectInstance(ResourceManager resourceManager, String string) throws IOException {
        this.samplerMap_ = new O2OHashMap<>();
        this.samplerNames_ = new OArrayList<>();
        this.samplerLocations_ = new IArrayList();
        this.uniforms_ = new OArrayList<>();
        this.uniformLocations_ = new IArrayList();
        this.uniformMap_ = new O2OHashMap<>();
        int index = string.indexOf(':');
        String namespace = "minecraft";
        String path = string;
        if (index != -1) {
            namespace = string.substring(0, index);
            path = string.substring(index + 1);
        }
        ResourceLocation resourceLocation = new ResourceLocation(namespace, "shaders/program/" + path + ".json");
        this.name = string;
        Resource resource = null;
        try {
            String string3;
            try {
                resource = resourceManager.getResource(resourceLocation);
                JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                String string2 = GsonHelper.getAsString(jsonObject, "vertex");
                string3 = GsonHelper.getAsString(jsonObject, "fragment");
                JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "samplers", null);
                if (jsonArray != null) {
                    int i = 0;
                    for (Iterator<JsonElement> var10 = jsonArray.iterator(); var10.hasNext(); ++i) {
                        JsonElement jsonElement = var10.next();
                        try {
                            this.parseSamplerNode(jsonElement);
                        }
                        catch (Exception var24) {
                            ChainedJsonException chainedJsonException = ChainedJsonException.forException(var24);
                            //noinspection ObjectAllocationInLoop
                            chainedJsonException.prependJsonKey("samplers[" + i + "]");
                            throw chainedJsonException;
                        }
                    }
                }
                JsonArray attributes = GsonHelper.getAsJsonArray(jsonObject, "attributes", null);
                if (attributes != null) {
                    int j = 0;
                    this.attributes_ = new IArrayList(attributes.size());
                    this.attributeNames_ = new OArrayList<>(attributes.size());
                    for (Iterator<JsonElement> it = attributes.iterator(); it.hasNext(); ++j) {
                        JsonElement jsonElement2 = it.next();
                        try {
                            this.attributeNames_.add(GsonHelper.convertToString(jsonElement2, "attribute"));
                        }
                        catch (Exception var23) {
                            ChainedJsonException chainedJsonException2 = ChainedJsonException.forException(var23);
                            //noinspection ObjectAllocationInLoop
                            chainedJsonException2.prependJsonKey("attributes[" + j + "]");
                            throw chainedJsonException2;
                        }
                    }
                }
                else {
                    this.attributes_ = null;
                    this.attributeNames_ = null;
                }
                JsonArray jsonArray3 = GsonHelper.getAsJsonArray(jsonObject, "uniforms", null);
                if (jsonArray3 != null) {
                    int k = 0;
                    for (Iterator<JsonElement> var33 = jsonArray3.iterator(); var33.hasNext(); ++k) {
                        JsonElement jsonElement3 = var33.next();
                        try {
                            this.parseUniformNode(jsonElement3);
                        }
                        catch (Exception var22) {
                            ChainedJsonException chainedJsonException3 = ChainedJsonException.forException(var22);
                            //noinspection ObjectAllocationInLoop
                            chainedJsonException3.prependJsonKey("uniforms[" + k + "]");
                            throw chainedJsonException3;
                        }
                    }
                }
                this.blend = parseBlendNode(GsonHelper.getAsJsonObject(jsonObject, "blend", null));
                this.vertexProgram = getOrCreate(resourceManager, Program.Type.VERTEX, string2);
                this.fragmentProgram = getOrCreate(resourceManager, Program.Type.FRAGMENT, string3);
                this.programId = ProgramManager.createProgram();
                ProgramManager.linkShader(this);
                this.updateLocations();
                OList<String> attributeNames = this.attributeNames_;
                if (attributeNames != null) {
                    for (int i = 0, len = attributeNames.size(); i < len; ++i) {
                        int location = Uniform.glGetAttribLocation(this.programId, attributeNames.get(i));
                        this.attributes_.add(location);
                    }
                }
            }
            catch (Exception var25) {
                if (resource != null) {
                    string3 = " (" + resource.getSourceName() + ")";
                }
                else {
                    string3 = "";
                }
                ChainedJsonException chainedJsonException4 = ChainedJsonException.forException(var25);
                String var10001 = resourceLocation.getPath();
                chainedJsonException4.setFilenameAndFlush(var10001 + string3);
                throw chainedJsonException4;
            }
        }
        finally {
            IOUtils.closeQuietly(resource);
        }
        this.markDirty();
    }

    @Shadow
    public static EffectProgram getOrCreate(ResourceManager resourceManager, Program.Type type, String string) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static BlendMode parseBlendNode(@Nullable JsonObject jsonObject) {
        throw new AbstractMethodError();
    }

    @Overwrite
    public void apply() {
        RenderSystem.assertOnGameThread();
        this.dirty = false;
        lastAppliedEffect = (EffectInstance) (Object) this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }
        IList samplerLocations = this.samplerLocations_;
        for (int i = 0, len = samplerLocations.size(); i < len; ++i) {
            String string = this.samplerNames_.get(i);
            IntSupplier intSupplier = this.samplerMap_.get(string);
            if (intSupplier != null) {
                RenderSystem.activeTexture('蓀' + i);
                RenderSystem.enableTexture();
                int j = intSupplier.getAsInt();
                if (j != -1) {
                    RenderSystem.bindTexture(j);
                    Uniform.uploadInteger(samplerLocations.getInt(i), i);
                }
            }
        }
        OList<Uniform> uniforms = this.uniforms_;
        for (int i = 0, len = uniforms.size(); i < len; ++i) {
            uniforms.get(i).upload();
        }
    }

    @Overwrite
    public void clear() {
        RenderSystem.assertOnRenderThread();
        ProgramManager.glUseProgram(0);
        lastProgramId = -1;
        lastAppliedEffect = null;
        for (int i = 0, len = this.samplerLocations_.size(); i < len; ++i) {
            if (this.samplerMap_.get(this.samplerNames_.get(i)) != null) {
                GlStateManager._activeTexture('蓀' + i);
                GlStateManager._disableTexture();
                GlStateManager._bindTexture(0);
            }
        }
    }

    @Override
    @Overwrite
    public void close() {
        OList<Uniform> uniforms = this.uniforms_;
        for (int i = 0, len = uniforms.size(); i < len; ++i) {
            uniforms.get(i).close();
        }
        ProgramManager.releaseProgram(this);
    }

    @Overwrite
    public @Nullable Uniform getUniform(String string) {
        RenderSystem.assertOnRenderThread();
        return this.uniformMap_.get(string);
    }

    @Override
    @Shadow
    public abstract void markDirty();

    @Overwrite
    private void parseSamplerNode(JsonElement jsonElement) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "sampler");
        String string = GsonHelper.getAsString(jsonObject, "name");
        if (!GsonHelper.isStringValue(jsonObject, "file")) {
            this.samplerMap_.put(string, null);
            this.samplerNames_.add(string);
        }
        else {
            this.samplerNames_.add(string);
        }
    }

    @Overwrite
    private void parseUniformNode(JsonElement jsonElement) throws ChainedJsonException {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "uniform");
        String string = GsonHelper.getAsString(jsonObject, "name");
        int i = Uniform.getTypeFromString(GsonHelper.getAsString(jsonObject, "type"));
        int j = GsonHelper.getAsInt(jsonObject, "count");
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
        if (jsonArray.size() != j && jsonArray.size() > 1) {
            throw new ChainedJsonException("Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
        }
        int k = 0;
        float[] fs = new float[Math.max(j, 16)];
        for (Iterator<JsonElement> var9 = jsonArray.iterator(); var9.hasNext(); ++k) {
            JsonElement jsonElement2 = var9.next();
            try {
                fs[k] = GsonHelper.convertToFloat(jsonElement2, "value");
            }
            catch (Exception var13) {
                ChainedJsonException chainedJsonException = ChainedJsonException.forException(var13);
                //noinspection ObjectAllocationInLoop
                chainedJsonException.prependJsonKey("values[" + k + "]");
                throw chainedJsonException;
            }
        }
        if (j > 1 && jsonArray.size() == 1) {
            while (k < j) {
                fs[k] = fs[0];
                ++k;
            }
        }
        int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
        Uniform uniform = new Uniform(string, i + l, j, this);
        if (i <= 3) {
            uniform.setSafe((int) fs[0], (int) fs[1], (int) fs[2], (int) fs[3]);
        }
        else if (i <= 7) {
            uniform.setSafe(fs[0], fs[1], fs[2], fs[3]);
        }
        else {
            uniform.set(fs);
        }
        this.uniforms_.add(uniform);
    }

    @Overwrite
    public void setSampler(String string, IntSupplier intSupplier) {
        this.samplerMap_.put(string, intSupplier);
        this.markDirty();
    }

    @Overwrite
    private void updateLocations() {
        RenderSystem.assertOnRenderThread();
        IList intList = new IArrayList();
        OList<String> samplerNames = this.samplerNames_;
        for (int i = 0, len = samplerNames.size(); i < len; ++i) {
            String name = samplerNames.get(i);
            int location = Uniform.glGetUniformLocation(this.programId, name);
            if (location == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, name);
                this.samplerMap_.remove(name);
                intList.add(i);
            }
            else {
                this.samplerLocations_.add(location);
            }
        }
        for (int i = intList.size() - 1; i >= 0; --i) {
            this.samplerNames_.remove(intList.getInt(i));
        }
        OList<Uniform> uniforms = this.uniforms_;
        for (int i = 0, len = uniforms.size(); i < len; ++i) {
            Uniform uniform = uniforms.get(i);
            String uniformName = uniform.getName();
            int location = Uniform.glGetUniformLocation(this.programId, uniformName);
            if (location == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, uniformName);
            }
            else {
                this.uniformLocations_.add(location);
                uniform.setLocation(location);
                this.uniformMap_.put(uniformName, uniform);
            }
        }
    }
}
