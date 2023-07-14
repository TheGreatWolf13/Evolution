package tgw.evolution.hooks.asm;

import net.fabricmc.loader.api.FabricLoader;

public final class FieldReference {

    final String desc;
    final String name;
    final String owner;

    public FieldReference(ClassReference owner, String name, ClassReference desc) {
        this.owner = owner.owner;
        this.name = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary", owner.nameInt, name, desc.paramInt);
        this.desc = desc.param;
        if (this.name.equals(name)) {
            CoreModLoader.LOGGER.warn("WARNING UNMATCHED FIELD NAME: {} ", name);
        }
    }

    public FieldReference(ClassReference owner, String name, String desc) {
        this.owner = owner.owner;
        this.name = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary", owner.nameInt, name, desc);
        this.desc = desc;
        if (this.name.equals(name)) {
            CoreModLoader.LOGGER.warn("WARNING UNMATCHED FIELD NAME: {} ", name);
        }
    }
}
