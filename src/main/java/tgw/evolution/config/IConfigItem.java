package tgw.evolution.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface IConfigItem {

    Component desc();

    void discardDirty();

    boolean isDefault();

    boolean isDirty();

    Component name();

    @Nullable ConfigFolder parent();

    void restore();

    void save();

    Type type();

    enum Type {
        FOLDER,
        BOOLEAN,
        INTEGER,
        ENUM
    }
}
