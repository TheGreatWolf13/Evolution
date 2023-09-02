package tgw.evolution.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

import java.util.Comparator;

public final class ConfigFolder implements IConfigItem {

    private static final Comparator<IConfigItem> COMPARATOR = (o1, o2) -> {
        if (o1.type() == Type.FOLDER) {
            if (o2.type() == Type.FOLDER) {
                return MathHelper.compare(o1.name().getString(), o2.name().getString());
            }
            return -1;
        }
        if (o2.type() == Type.FOLDER) {
            return 1;
        }
        int priority = o1.priority().compareTo(o2.priority());
        if (priority == 0) {
            return MathHelper.compare(o1.name().getString(), o2.name().getString());
        }
        return priority;
    };
    private final TranslatableComponent desc;
    private final OList<IConfigItem> list = new OArrayList<>();
    private final TranslatableComponent name;
    private final @Nullable ConfigFolder parent;
    private final boolean root;

    public ConfigFolder(@Nullable ConfigFolder parent, String name) {
        if (parent == null || parent.isRoot()) {
            this.name = new TranslatableComponent("evolution.config." + name);
            this.desc = new TranslatableComponent("evolution.config." + name + ".desc");
        }
        else {
            String key = parent.name.getKey() + "." + name;
            this.name = new TranslatableComponent(key);
            this.desc = new TranslatableComponent(key + ".desc");
        }
        if (parent != null) {
            this.root = false;
            parent.add(this);
            this.parent = parent;
        }
        else {
            this.root = true;
            this.parent = null;
        }
    }

    public void add(IConfigItem t) {
        this.list.add(t);
    }

    @Override
    public Component desc() {
        return this.desc;
    }

    @Override
    public void discardDirty() {
    }

    @Override
    public boolean isDefault() {
        OList<IConfigItem> list = this.list;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (!list.get(i).isDefault()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    public boolean isRoot() {
        return this.root;
    }

    public @UnmodifiableView OList<IConfigItem> items() {
        return this.list.view();
    }

    @Override
    public Component name() {
        return this.name;
    }

    public String nameKey() {
        if (this.isRoot()) {
            String key = this.name.getKey();
            return key.substring(0, key.lastIndexOf('.'));
        }
        return this.name.getKey();
    }

    @Override
    public @Nullable ConfigFolder parent() {
        return this.parent;
    }

    @Override
    public Priority priority() {
        return Priority.NORMAL;
    }

    @Override
    public void restore() {
        OList<IConfigItem> list = this.list;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).restore();
        }
    }

    @Override
    public void save() {
    }

    public void sort() {
        this.list.sort(COMPARATOR);
    }

    @Override
    public Type type() {
        return Type.FOLDER;
    }
}
