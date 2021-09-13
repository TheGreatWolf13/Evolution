package tgw.evolution.blocks.tileentities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

public class TEMolding extends TileEntity {

    public EnumMolding molding = EnumMolding.NULL;
    @Nullable
    private VoxelShape hitbox;
    private long[] parts = {Patterns.MATRIX_TRUE,
                            Patterns.MATRIX_FALSE,
                            Patterns.MATRIX_FALSE,
                            Patterns.MATRIX_FALSE,
                            Patterns.MATRIX_FALSE,
                            Patterns.MATRIX_FALSE,
                            Patterns.MATRIX_FALSE,
                            Patterns.MATRIX_FALSE};

    public TEMolding() {
        super(EvolutionTEs.MOLDING.get());
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
        VoxelShape shape = VoxelShapes.empty();
        if (state.getValue(EvolutionBStates.LAYERS_1_5) == 1) {
            shape = EvolutionHitBoxes.MOLD_TOTAL_BASE;
        }
//        for (int enc = 0; enc < this.matrices.length; enc++) {
//            if (this.matrices[enc] == null) {
//                this.hitbox = shape;
//                return;
//            }
//            for (int i = 0; i < this.matrices[enc].length; i++) {
//                for (int j = 0; j < this.matrices[enc][i].length; j++) {
//                    if (this.matrices[enc][i][j]) {
//                        shape = MathHelper.union(shape, EvolutionHitBoxes.MOLD_PART.withOffset(3 * i / 16.0f, 3 * enc / 16.0f, 3 * j / 16.0f));
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
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.molding = EnumMolding.byId(compound.getByte("Type"));
//        this.encoded[0] = compound.getInt("Part1");
//        this.encoded[1] = compound.getInt("Part2");
//        this.encoded[2] = compound.getInt("Part3");
//        this.encoded[3] = compound.getInt("Part4");
//        this.encoded[4] = compound.getInt("Part5");
//        this.deserializeToMatrices();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.hitbox = null;
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), packet.getTag());
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
//        this.serializeToInts();
//        compound.putInt("Part1", this.encoded[0]);
//        compound.putInt("Part2", this.encoded[1]);
//        compound.putInt("Part3", this.encoded[2]);
//        compound.putInt("Part4", this.encoded[3]);
//        compound.putInt("Part5", this.encoded[4]);
        compound.putByte("Type", this.molding.getId());
        return super.save(compound);
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
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }

    public void setType(EnumMolding molding) {
        this.molding = molding;
        this.sendRenderUpdate();
    }

    private void spawnDrops(RegistryObject<Item> item) {
        this.spawnDrops(item, 1);
    }

    private void spawnDrops(RegistryObject<Item> item, int count) {
        Block.popResource(this.level, this.worldPosition, new ItemStack(item.get(), count));
        this.level.removeBlock(this.worldPosition, false);
    }
}
