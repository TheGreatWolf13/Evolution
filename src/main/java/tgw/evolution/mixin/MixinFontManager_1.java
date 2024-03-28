package tgw.evolution.mixin;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$1")
public abstract class MixinFontManager_1 extends SimplePreparableReloadListener<Map<ResourceLocation, List<GlyphProvider>>> implements IKeyedReloadListener {

    @Shadow(aliases = "this$0") @Final FontManager field_18216;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void apply(Map<ResourceLocation, List<GlyphProvider>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("closing");
        this.field_18216.fontSets.values().forEach(FontSet::close);
        this.field_18216.fontSets.clear();
        profilerFiller.popPush("reloading");
        O2OMap<ResourceLocation, List<GlyphProvider>> map_ = (O2OMap<ResourceLocation, List<GlyphProvider>>) map;
        for (long it = map_.beginIteration(); map_.hasNextIteration(it); it = map_.nextEntry(it)) {
            ResourceLocation resourceLocation = map_.getIterationKey(it);
            List<GlyphProvider> list = map_.getIterationValue(it);
            FontSet fontSet = new FontSet(this.field_18216.textureManager, resourceLocation);
            fontSet.reload(Lists.reverse(list));
            this.field_18216.fontSets.put(resourceLocation, fontSet);
        }
        profilerFiller.pop();
        profilerFiller.endTick();
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.FONTS;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Map<ResourceLocation, List<GlyphProvider>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<ResourceLocation, List<GlyphProvider>> map = new O2OHashMap<>();
        IntSet intSet = new IntOpenHashSet();
        for (ResourceLocation resourceLocation : resourceManager.listResources("font", s -> s.endsWith(".json"))) {
            String string = resourceLocation.getPath();
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string.substring("font/".length(), string.length() - ".json".length()));
            List<GlyphProvider> list = map.get(resourceLocation2);
            if (list == null) {
                list = new OArrayList<>();
                //noinspection resource
                list.add(new AllMissingGlyphProvider());
                map.put(resourceLocation2, list);
            }
            profiler.push(resourceLocation2.toString());
            try {
                List<Resource> resources = resourceManager.getResources(resourceLocation);
                for (int i = 0, len = resources.size(); i < len; ++i) {
                    Resource resource = resources.get(i);
                    profiler.push(resource.getSourceName());
                    try {
                        InputStream inputStream = resource.getInputStream();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                            try {
                                profiler.push("reading");
                                JsonArray jsonArray = GsonHelper.getAsJsonArray(GsonHelper.fromJson(gson, reader, JsonObject.class), "providers");
                                profiler.popPush("parsing");
                                for (int j = jsonArray.size() - 1; j >= 0; --j) {
                                    JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonArray.get(j), "providers[" + j + "]");
                                    try {
                                        String string2 = GsonHelper.getAsString(jsonObject, "type");
                                        GlyphProviderBuilderType glyphProviderBuilderType = GlyphProviderBuilderType.byName(string2);
                                        profiler.push(string2);
                                        GlyphProvider glyphProvider = glyphProviderBuilderType.create(jsonObject).create(resourceManager);
                                        if (glyphProvider != null) {
                                            list.add(glyphProvider);
                                        }
                                        profiler.pop();
                                    }
                                    catch (RuntimeException e) {
                                        Evolution.warn("Unable to read definition '{}' in {} in resourcepack: '{}': {}", resourceLocation2, "fonts.json", resource.getSourceName(), e.getMessage());
                                    }
                                }
                                profiler.pop();
                            }
                            catch (Throwable e) {
                                try {
                                    reader.close();
                                }
                                catch (Throwable t) {
                                    e.addSuppressed(t);
                                }
                                throw e;
                            }
                            reader.close();
                        }
                        catch (Throwable e) {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                }
                                catch (Throwable t) {
                                    e.addSuppressed(t);
                                }
                            }
                            throw e;
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                    catch (RuntimeException e) {
                        Evolution.warn("Unable to load font '{}' in {} in resourcepack: '{}': {}", resourceLocation2, "fonts.json", resource.getSourceName(), e.getMessage());
                    }
                    profiler.pop();
                }
            }
            catch (IOException e) {
                Evolution.warn("Unable to load font '{}' in {}: {}", resourceLocation2, "fonts.json", e.getMessage());
            }
            profiler.push("caching");
            intSet.clear();
            for (int i = 0, len = list.size(); i < len; ++i) {
                intSet.addAll(list.get(i).getSupportedGlyphs());
            }
            for (IntIterator it = intSet.iterator(); it.hasNext(); ) {
                int i = it.nextInt();
                if (i != 32) {
                    for (int j = list.size() - 1; j >= 0; j--) {
                        GlyphProvider glyphProvider = list.get(j);
                        if (glyphProvider.getGlyph(i) != null) {
                            break;
                        }
                    }
                }
            }
            profiler.pop();
            profiler.pop();
        }
        profiler.endTick();
        return map;
    }
}
