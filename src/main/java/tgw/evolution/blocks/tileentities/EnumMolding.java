package tgw.evolution.blocks.tileentities;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMaps;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.math.MathHelper;

public enum EnumMolding {
    NULL(0, Shapes.empty(), MoldingPatterns.NULL, ItemStack.EMPTY),
    AXE(1,
        MathHelper.subtract(EvolutionHitBoxes.MOLD_1, EvolutionHitBoxes.AXE_THICK),
        MoldingPatterns.AXE,
        new ItemStack(EvolutionItems.mold_clay_axe.get())),
    ;
//    PICKAXE(2,
//            MathHelper.subtract(EvolutionHitBoxes.MOLD_1, EvolutionHitBoxes.PICKAXE_THICK),
//            MoldingPatterns.PICKAXE,
//            new ItemStack(EvolutionItems.mold_clay_pickaxe.get()));

    public static final EnumMolding[] VALUES = values();
    private static final Byte2ReferenceMap<EnumMolding> REGISTRY;

    static {
        Byte2ReferenceOpenHashMap<EnumMolding> map = new Byte2ReferenceOpenHashMap<>();
        for (EnumMolding molding : VALUES) {
            map.put(molding.id, molding);
        }
        map.trim();
        REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
    }

    private final byte id;
    private final long[] pattern;
    private final VoxelShape shape;
    private final ItemStack stack;

    EnumMolding(int id, VoxelShape shape, long[] pattern, ItemStack stack) {
        this.id = MathHelper.toByteExact(id);
        this.shape = shape;
        this.pattern = pattern;
        this.stack = stack;
    }

    public static EnumMolding byId(byte id) {
        return REGISTRY.getOrDefault(id, NULL);
    }

    public byte getId() {
        return this.id;
    }

    public long[] getPattern() {
        return this.pattern;
    }

    public VoxelShape getShape() {
        return this.shape;
    }

    public ItemStack getStack() {
        return this.stack;
    }
}
