package tgw.evolution.blocks.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;

public enum EnumMolding {
    NULL(0, VoxelShapes.empty(), MoldingPatterns.NULL, ItemStack.EMPTY),
    AXE(1,
        MathHelper.subtract(EvolutionHitBoxes.MOLD_1, EvolutionHitBoxes.AXE_THICK),
        MoldingPatterns.AXE,
        new ItemStack(EvolutionItems.mold_clay_axe.get())),
    ;
//    PICKAXE(2,
//            MathHelper.subtract(EvolutionHitBoxes.MOLD_1, EvolutionHitBoxes.PICKAXE_THICK),
//            MoldingPatterns.PICKAXE,
//            new ItemStack(EvolutionItems.mold_clay_pickaxe.get()));

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

    public static EnumMolding byId(int id) {
        for (EnumMolding molding : values()) {
            if (molding.id == id) {
                return molding;
            }
        }
        Evolution.LOGGER.warn("Could not find EnumMolding with id {}, replacing with NULL", id);
        return NULL;
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
