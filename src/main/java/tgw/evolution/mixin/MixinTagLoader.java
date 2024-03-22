package tgw.evolution.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.maps.O2OHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public abstract class MixinTagLoader {

    @Shadow @Final private static Gson GSON;
    @Shadow @Final private static int PATH_SUFFIX_LENGTH;
    @Shadow @Final private String directory;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Map<ResourceLocation, Tag.Builder> load(ResourceManager resourceManager) {
        Map<ResourceLocation, Tag.Builder> map = new O2OHashMap<>();
        for (ResourceLocation resourceLocation : resourceManager.listResources(this.directory, stringx -> stringx.endsWith(".json"))) {
            String string = resourceLocation.getPath();
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH));
            try {
                List<Resource> resources = resourceManager.getResources(resourceLocation);
                for (int i = 0, len = resources.size(); i < len; ++i) {
                    Resource resource = resources.get(i);
                    try {
                        InputStream inputStream = resource.getInputStream();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                            try {
                                JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                                if (jsonObject == null) {
                                    Evolution.error("Couldn't load tag list {} from {} in data pack {} as it is empty or null", resourceLocation2, resourceLocation, resource.getSourceName());
                                }
                                else {
                                    map.computeIfAbsent(resourceLocation2, resourceLocationx -> Tag.Builder.tag()).addFromJson(jsonObject, resource.getSourceName());
                                }
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
                    catch (RuntimeException | IOException e) {
                        Evolution.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation2, resourceLocation, resource.getSourceName(), e);
                    }
                    finally {
                        IOUtils.closeQuietly(resource);
                    }
                }
            }
            catch (IOException e) {
                Evolution.error("Couldn't read tag list {} from {}", resourceLocation2, resourceLocation, e);
            }
        }
        return map;
    }
}
