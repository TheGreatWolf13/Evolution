package tgw.evolution.resources;

import net.minecraft.SharedConstants;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ModResourcePack extends GroupResourcePack {

    public ModResourcePack(PackType type, OList<IModResourcePack> packs) {
        super(type, packs);
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> metaReader) throws IOException {
        try {
            try (InputStream inputStream = this.getRootResource("pack.mcmeta")) {
                assert inputStream != null;
                return AbstractPackResources.getMetadataFromStream(metaReader, inputStream);
            }
        }
        catch (FileNotFoundException | RuntimeException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "Mods";
    }

    @Override
    public @Nullable InputStream getRootResource(String fileName) throws IOException {
        if ("pack.mcmeta".equals(fileName)) {
            String description = "Mod resources.";
            String pack = String.format(
                    "{\"pack\":{\"pack_format\":" + this.type.getVersion(SharedConstants.getCurrentVersion()) + ",\"description\":\"%s\"}}",
                    description);
            return IOUtils.toInputStream(pack, Charsets.UTF_8);
        }
        throw new FileNotFoundException("\"" + fileName + "\" in mod resource pack");
    }
}
