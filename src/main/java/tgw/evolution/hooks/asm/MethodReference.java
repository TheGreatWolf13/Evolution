package tgw.evolution.hooks.asm;

import net.fabricmc.loader.api.FabricLoader;

public class MethodReference {

    final String desc;
    final String name;
    final @MethodOp int opcode;
    final String owner;

    public MethodReference(@MethodOp int opcode, ClassReference owner, String name, MDBI desc) {
        this.opcode = opcode;
        this.owner = owner.owner;
        this.desc = desc.desc();
        this.name = FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", owner.nameInt, name, desc.descInt());
        if (this.name.equals(name)) {
            CoreModLoader.LOGGER.warn("WARNING UNMATCHED METHOD NAME: {} ", name);
        }
    }

    public MethodReference(@MethodOp int opcode, ClassReference owner, String name, String desc) {
        this.opcode = opcode;
        this.owner = owner.owner;
        this.desc = desc;
        this.name = FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", owner.nameInt, name, desc);
        if (this.name.equals(name)) {
            CoreModLoader.LOGGER.warn("WARNING UNMATCHED METHOD NAME: {} ", name);
        }
    }

    protected MethodReference(@MethodOp int opcode, ClassReference owner, String name, String desc, boolean remapName) {
        this.opcode = opcode;
        this.owner = owner.owner;
        this.desc = desc;
        this.name = remapName ? FabricLoader.getInstance().getMappingResolver().mapMethodName("named", owner.nameInt, name, desc) : name;
        if (remapName && this.name.equals(name)) {
            CoreModLoader.LOGGER.warn("WARNING UNMATCHED METHOD NAME: {} ", name);
        }
    }
}
