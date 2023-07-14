package tgw.evolution.resources;

import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.packs.PackResources;

public interface IModResourcePack extends PackResources {

    ModMetadata getModMetadata();
}
