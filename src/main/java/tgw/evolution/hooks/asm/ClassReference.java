package tgw.evolution.hooks.asm;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Contract;

public final class ClassReference {

    final String name;
    final String nameInt;
    final String owner;
    final String param;
    final String paramInt;

    public ClassReference(String name) {
        this.nameInt = name;
        this.name = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", name);
        this.owner = this.name.replace('.', '/');
        this.param = "L" + this.owner + ";";
        this.paramInt = "L" + name.replace('.', '/') + ";";
        if (this.name.equals(name)) {
            CoreModLoader.LOGGER.info("WARNING UNMATCHED NAME: {} -> {}", name,
                                      FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", name));
        }
    }

    @Contract(pure = true)
    public boolean matches(String name) {
        return this.name.equals(name);
    }
}
