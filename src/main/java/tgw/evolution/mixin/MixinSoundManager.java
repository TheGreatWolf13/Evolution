package tgw.evolution.mixin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.sounds.SoundManager;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager extends SimplePreparableReloadListener<SoundManager.Preparations> implements IKeyedReloadListener {

    @Shadow @Final private static Gson GSON;
    @Shadow @Final private static TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE;

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.SOUNDS;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public SoundManager.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        SoundManager.Preparations preparations = new SoundManager.Preparations();
        profiler.startTick();
        for (String string : resourceManager.getNamespaces()) {
            profiler.push(string);
            try {
                List<Resource> list = resourceManager.getResources(new ResourceLocation(string, "sounds.json"));
                for (int i = 0, len = list.size(); i < len; ++i) {
                    Resource resource = list.get(i);
                    profiler.push(resource.getSourceName());
                    try {
                        InputStream inputStream = resource.getInputStream();
                        try {
                            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                            try {
                                profiler.push("parse");
                                Map<String, SoundEventRegistration> map = GsonHelper.fromJson(GSON, reader, SOUND_EVENT_REGISTRATION_TYPE);
                                profiler.popPush("register");
                                for (Map.Entry<String, SoundEventRegistration> entry : map.entrySet()) {
                                    preparations.handleRegistration(new ResourceLocation(string, entry.getKey()), entry.getValue(), resourceManager);
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
                        Evolution.warn("Invalid {} in resourcepack: '{}'", "sounds.json", resource.getSourceName(), e);
                    }
                    profiler.pop();
                }
            }
            catch (IOException ignored) {
            }
            profiler.pop();
        }
        profiler.endTick();
        return preparations;
    }
}
