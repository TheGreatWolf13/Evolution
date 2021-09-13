package tgw.evolution.blocks.tileentities;

import net.minecraft.util.IStringSerializable;
import tgw.evolution.util.MathHelper;

public enum SchematicMode implements IStringSerializable {
    SAVE(0, "save"),
    LOAD(1, "load"),
    CORNER(2, "corner");

    private final byte id;
    private final String name;

    SchematicMode(int id, String name) {
        this.name = name;
        this.id = MathHelper.toByteExact(id);
    }

    public static SchematicMode byId(int id) {
        for (SchematicMode mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        throw new IllegalStateException("Unknown SchematicMode " + id);
    }

    public byte getId() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
