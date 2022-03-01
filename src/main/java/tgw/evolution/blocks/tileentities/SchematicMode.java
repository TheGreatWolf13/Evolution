package tgw.evolution.blocks.tileentities;

import net.minecraft.util.StringRepresentable;
import tgw.evolution.util.math.MathHelper;

public enum SchematicMode implements StringRepresentable {
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
