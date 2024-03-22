package tgw.evolution.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(ClientLanguage.class)
public abstract class MixinClientLanguage extends Language {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static void appendFrom(List<Resource> list, Map<String, String> map) {
        for (int i = 0, len = list.size(); i < len; ++i) {
            Resource resource = list.get(i);
            try {
                InputStream inputStream = resource.getInputStream();
                try {
                    Objects.requireNonNull(map);
                    Language.loadFromJson(inputStream, map::put);
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
            catch (IOException e) {
                Evolution.warn("Failed to load translations from {}: {}", resource, e);
            }
        }
    }
}
