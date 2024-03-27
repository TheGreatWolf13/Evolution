package tgw.evolution.blocks.tileentities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.util.collection.maps.B2OHashMap;
import tgw.evolution.util.collection.maps.B2OMap;
import tgw.evolution.util.math.MathHelper;

public enum EnumMolding {
    NULL(0, Shapes.empty(), MoldingPatterns.NULL, ItemStack.EMPTY),
    AXE(1, MathHelper.subtract(EvolutionShapes.MOLD_1, EvolutionShapes.AXE_THICK), MoldingPatterns.AXE, new ItemStack(EvolutionItems.MOLD_CLAY_AXE)),
    ;
//    PICKAXE(2,
//            MathHelper.subtract(EvolutionShapes.MOLD_1, EvolutionShapes.PICKAXE_THICK),
//            MoldingPatterns.PICKAXE,
//            new ItemStack(EvolutionItems.mold_clay_pickaxe.get()));

    public static final EnumMolding[] VALUES = values();
    private static final B2OMap<EnumMolding> REGISTRY;

    static {
        B2OMap<EnumMolding> map = new B2OHashMap<>();
        for (EnumMolding molding : VALUES) {
            map.put(molding.id, molding);
        }
        map.trim();
        REGISTRY = map.view();
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
