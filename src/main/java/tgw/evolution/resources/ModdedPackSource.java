package tgw.evolution.resources;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.function.Consumer;

public class ModdedPackSource implements RepositorySource {

    public static final PackSource RESOURCE_PACK_SOURCE = text -> new TranslatableComponent("pack.nameAndSource", text,
                                                                                            new TranslatableComponent("evolution.pack.source.mod"));
    public static final ModdedPackSource CLIENT_RESOURCE_PACK_PROVIDER = new ModdedPackSource(PackType.CLIENT_RESOURCES);
    private final Pack.PackConstructor factory;
    private final PackType type;

    public ModdedPackSource(PackType type) {
        this.type = type;
        this.factory = (name, text, bl, supplier, metadata, initialPosition, source) -> new Pack(name, text, bl, supplier, metadata, type,
                                                                                                 initialPosition, source);
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor factory) {
        OList<IModResourcePack> packs = new OArrayList<>();
        ModResourcePackUtil.appendModResourcePacks(packs, this.type, null);
        if (!packs.isEmpty()) {
            Pack resourcePackProfile = Pack.create("Mods",
                                                   true, () -> new ModResourcePack(this.type, packs), factory,
                                                   Pack.Position.TOP,
                                                   RESOURCE_PACK_SOURCE);
            if (resourcePackProfile != null) {
                consumer.accept(resourcePackProfile);
            }
        }
        ResourceManagerHelperImpl.registerBuiltinResourcePacks(this.type, consumer, factory);
    }

    /**
     * Registers the resource packs.
     *
     * @param consumer The resource pack profile consumer.
     */
    public void register(Consumer<Pack> consumer) {
        this.loadPacks(consumer, this.factory);
    }
}
