package tgw.evolution.util;

public enum NBTTypes {
    BYTE(1),
    SHORT(2),
    INT(3),
    LONG(4),
    FLOAT(5),
    DOUBLE(6),
    BYTE_ARRAY(7),
    STRING(8),
    LIST_NBT(9),
    COMPOUND_NBT(10),
    INT_ARRAY(11),
    LONG_ARRAY(12);

    private final int id;

    NBTTypes(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
