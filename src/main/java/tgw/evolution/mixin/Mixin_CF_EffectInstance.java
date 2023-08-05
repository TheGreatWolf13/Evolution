package tgw.evolution.mixin;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.shaders.*;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.O2OHashMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

@Mixin(EffectInstance.class)
public abstract class Mixin_CF_EffectInstance {

    @Mutable @Shadow @Final @RestoreFinal private @Nullable List<String> attributeNames;
    @Mutable @Shadow @Final @RestoreFinal private @Nullable List<Integer> attributes;
    @Mutable @Shadow @Final @RestoreFinal private BlendMode blend;
    @Mutable @Shadow @Final @RestoreFinal private EffectProgram fragmentProgram;
    @Mutable @Shadow @Final @RestoreFinal private String name;
    @Mutable @Shadow @Final @RestoreFinal private int programId;
    @Mutable @Shadow @Final @RestoreFinal private List<Integer> samplerLocations;
    @Mutable @Shadow @Final @RestoreFinal private Map<String, IntSupplier> samplerMap;
    @Mutable @Shadow @Final @RestoreFinal private List<String> samplerNames;
    @Mutable @Shadow @Final @RestoreFinal private List<Integer> uniformLocations;
    @Mutable @Shadow @Final @RestoreFinal private Map<String, Uniform> uniformMap;
    @Mutable @Shadow @Final @RestoreFinal private List<Uniform> uniforms;
    @Mutable @Shadow @Final @RestoreFinal private EffectProgram vertexProgram;

    @DummyConstructor
    public Mixin_CF_EffectInstance(@Nullable List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    @ModifyConstructor
    public Mixin_CF_EffectInstance(ResourceManager resourceManager, String string) throws IOException {
        this.samplerMap = new O2OHashMap<>();
        this.samplerNames = new OArrayList<>();
        this.samplerLocations = new OArrayList<>();
        this.uniforms = new OArrayList<>();
        this.uniformLocations = new OArrayList<>();
        this.uniformMap = new O2OHashMap<>();
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
                JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "attributes", null);
                Iterator var31;
                if (jsonArray2 != null) {
                    int j = 0;
                    this.attributes = Lists.newArrayListWithCapacity(jsonArray2.size());
                    this.attributeNames = Lists.newArrayListWithCapacity(jsonArray2.size());
                    for (var31 = jsonArray2.iterator(); var31.hasNext(); ++j) {
                        JsonElement jsonElement2 = (JsonElement) var31.next();
                        try {
                            this.attributeNames.add(GsonHelper.convertToString(jsonElement2, "attribute"));
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
                    this.attributes = null;
                    this.attributeNames = null;
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
                ProgramManager.linkShader((Shader) this);
                this.updateLocations();
                if (this.attributeNames != null) {
                    var31 = this.attributeNames.iterator();
                    while (var31.hasNext()) {
                        String string4 = (String) var31.next();
                        int l = Uniform.glGetAttribLocation(this.programId, string4);
                        this.attributes.add(l);
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

    @Shadow
    public abstract void markDirty();

    @Shadow
    protected abstract void parseSamplerNode(JsonElement jsonElement);

    @Shadow
    protected abstract void parseUniformNode(JsonElement jsonElement) throws ChainedJsonException;

    @Overwrite
    public void setSampler(String string, IntSupplier intSupplier) {
        this.samplerMap.put(string, intSupplier);
        this.markDirty();
    }

    @Shadow
    protected abstract void updateLocations();
}
