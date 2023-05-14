package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.MathHelper;

import java.util.function.Supplier;

public class TEMolding extends BlockEntity {

    private final long[] parts = {Patterns.MATRIX_TRUE,
                                  Patterns.MATRIX_FALSE,
                                  Patterns.MATRIX_FALSE,
                                  Patterns.MATRIX_FALSE,
                                  Patterns.MATRIX_FALSE,
                                  Patterns.MATRIX_FALSE,
                                  Patterns.MATRIX_FALSE,
                                  Patterns.MATRIX_FALSE};
    public EnumMolding molding = EnumMolding.NULL;
    @Nullable
    private VoxelShape hitbox;

    public TEMolding(BlockPos pos, BlockState state) {
        super(EvolutionTEs.MOLDING.get(), pos, state);
    }

    public void addLayer(int layer) {
        this.parts[layer] = Patterns.MATRIX_TRUE;
        this.sendRenderUpdate();
    }

    public int check() {
        for (int i = 0; i < this.parts.length; i++) {
            if (this.parts[i] == Patterns.MATRIX_FALSE) {
                MathHelper.resetTensor(this.parts, i == 0 ? 1 : i);
                return i;
            }
        }
        return -1;
    }

    public void checkPatterns() {
//        if (!this.world.isRemote()) {
//            int layers = this.world.getBlockState(this.pos).get(EvolutionBStates.LAYERS_1_5);
//            if (layers == 1) {
//                if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.AXE)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_axe);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SHOVEL)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_shovel);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.GUARD)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_guard);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.HAMMER)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_hammer);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.HOE)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_hoe);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SAW)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_saw);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.KNIFE)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_knife);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.PICKAXE)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_pickaxe);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SWORD)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_sword);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.PROSPECTING)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_prospecting);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SPEAR)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_spear);
//                }
//                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.INGOT)) {
//                    this.spawnDrops(EvolutionItems.mold_clay_ingot);
//                }
//                //                else if (MathHelper.matricesEqual(this.matrices[0], MoldingPatterns.PLATE)) {
//                //                    this.spawnDrops(EvolutionItems.mold_clay_plate);
//                //                }
//            }
//            else if (layers == 2) {
//                if (MoldingPatterns.comparePatternsTwoLayer(this.matrices, MoldingPatterns.BRICK)) {
//                    this.spawnDrops(EvolutionItems.brick_clay, 2);
//                }
//            }
//            else if (layers == 5) {
//                if (MathHelper.tensorsEquals(this.matrices, MoldingPatterns.CRUCIBLE)) {
//                    this.spawnDrops(EvolutionItems.crucible_clay);
//                }
//            }
//        }
    }

    public void computeHitbox(BlockState state) {
        VoxelShape shape = Shapes.empty();
        if (state.getValue(EvolutionBStates.LAYERS_1_5) == 1) {
            shape = EvolutionShapes.MOLD_TOTAL_BASE;
        }
//        for (int enc = 0; enc < this.matrices.length; enc++) {
//            if (this.matrices[enc] == null) {
//                this.hitbox = shape;
//                return;
//            }
//            for (int i = 0; i < this.matrices[enc].length; i++) {
//                for (int j = 0; j < this.matrices[enc][i].length; j++) {
//                    if (this.matrices[enc][i][j]) {
//                        shape = MathHelper.union(shape, EvolutionShapes.MOLD_PART.withOffset(3 * i / 16.0f, 3 * enc / 16.0f, 3 * j / 16.0f));
//                    }
//                }
//            }
//        }
        this.hitbox = shape;
    }

//    private void deserializeToMatrices() {
//        int temp = 0x100_0000;
//        for (int enc = 0; enc < this.encoded.length; enc++) {
//            if (this.encoded[enc] == -1) {
//                MathHelper.resetArray(this.encoded, enc);
//                MathHelper.resetTensor(this.matrices, enc);
//                return;
//            }
//            temp = 0x100_0000;
//            if (this.matrices[enc] == null) {
//                //noinspection ObjectAllocationInLoop
//                this.matrices[enc] = new boolean[5][5];
//            }
//            for (int i = 0; i < 5; i++) {
//                for (int j = 0; j < 5; j++) {
//                    this.matrices[enc][i][j] = (temp & this.encoded[enc]) != 0;
//                    temp >>= 1;
//                }
//            }
//        }
//    }

    public VoxelShape getHitbox(BlockState state) {
        if (this.hitbox == null) {
            this.computeHitbox(state);
        }
        return this.hitbox;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.molding = EnumMolding.byId(tag.getByte("Type"));
//        this.encoded[0] = tag.getInt("Part1");
//        this.encoded[1] = tag.getInt("Part2");
//        this.encoded[2] = tag.getInt("Part3");
//        this.encoded[3] = tag.getInt("Part4");
//        this.encoded[4] = tag.getInt("Part5");
//        this.deserializeToMatrices();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.hitbox = null;
        this.handleUpdateTag(packet.getTag());
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
//        this.serializeToInts();
//        tag.putInt("Part1", this.encoded[0]);
//        tag.putInt("Part2", this.encoded[1]);
//        tag.putInt("Part3", this.encoded[2]);
//        tag.putInt("Part4", this.encoded[3]);
//        tag.putInt("Part5", this.encoded[4]);
        tag.putByte("Type", this.molding.getId());
    }

//    private void serializeToInts() {
//        int temp = 0;
//        for (int enc = 0; enc < this.encoded.length; enc++) {
//            if (this.matrices[enc] == null) {
//                MathHelper.resetTensor(this.matrices, enc);
//                MathHelper.resetArray(this.encoded, enc);
//                return;
//            }
//            this.encoded[enc] = 0;
//            for (int i = 0; i < 5; i++) {
//                for (int j = 0; j < 5; j++) {
//                    temp = (this.matrices[enc][i][j] ? 1 : 0) << 24 - 5 * i - j;
//                    this.encoded[enc] |= temp;
//                }
//            }
//        }
//    }

    public void sendRenderUpdate() {
        this.setChanged();
        this.hitbox = null;
        assert this.level != null;
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }

    public void setType(EnumMolding molding) {
        this.molding = molding;
        this.sendRenderUpdate();
    }

    private void spawnDrops(Supplier<Item> item) {
        this.spawnDrops(item, 1);
    }

    private void spawnDrops(Supplier<Item> item, int count) {
        assert this.level != null;
        Block.popResource(this.level, this.worldPosition, new ItemStack(item.get(), count));
        this.level.removeBlock(this.worldPosition, false);
    }
}
