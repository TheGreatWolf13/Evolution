package tgw.evolution.blocks.tileentities;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTileEntities;
import tgw.evolution.util.MathHelper;

public class TEMolding extends TileEntity {

    private final int[] encoded = {0x1FFFFFF, -1, -1, -1, -1};
    public boolean[][][] matrices = {{{true, true, true, true, true},
                                      {true, true, true, true, true},
                                      {true, true, true, true, true},
                                      {true, true, true, true, true},
                                      {true, true, true, true, true}}, null, null, null, null};
    public VoxelShape hitbox;

    public TEMolding() {
        super(EvolutionTileEntities.TE_MOLDING.get());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        this.serializeToInts();
        compound.putInt("Part1", this.encoded[0]);
        compound.putInt("Part2", this.encoded[1]);
        compound.putInt("Part3", this.encoded[2]);
        compound.putInt("Part4", this.encoded[3]);
        compound.putInt("Part5", this.encoded[4]);
        return super.write(compound);
    }

    private void serializeToInts() {
        int temp = 0;
        for (int enc = 0; enc < this.encoded.length; enc++) {
            if (this.matrices[enc] == null) {
                MathHelper.resetTensor(this.matrices, enc);
                MathHelper.resetArray(this.encoded, enc);
                return;
            }
            this.encoded[enc] = 0;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    temp = (this.matrices[enc][i][j] ? 1 : 0) << 24 - 5 * i - j;
                    this.encoded[enc] |= temp;
                }
            }
        }
    }

    public void sendRenderUpdate() {
        super.markDirty();
        this.hitbox = null;
        this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), Constants.BlockFlags.RERENDER_MAIN_THREAD);
    }

    @Override
    public void read(CompoundNBT compound) {
        this.encoded[0] = compound.getInt("Part1");
        this.encoded[1] = compound.getInt("Part2");
        this.encoded[2] = compound.getInt("Part3");
        this.encoded[3] = compound.getInt("Part4");
        this.encoded[4] = compound.getInt("Part5");
        this.deserializeToMatrices();
        super.read(compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.hitbox = null;
        this.handleUpdateTag(packet.getNbtCompound());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    private void deserializeToMatrices() {
        int temp = 0x1000000;
        for (int enc = 0; enc < this.encoded.length; enc++) {
            if (this.encoded[enc] == -1) {
                MathHelper.resetArray(this.encoded, enc);
                MathHelper.resetTensor(this.matrices, enc);
                return;
            }
            temp = 0x1000000;
            if (this.matrices[enc] == null) {
                //noinspection ObjectAllocationInLoop
                this.matrices[enc] = new boolean[5][5];
            }
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    this.matrices[enc][i][j] = (temp & this.encoded[enc]) != 0;
                    temp >>= 1;
                }
            }
        }
    }

    public void addLayer(int layer) {
        this.encoded[layer] = 0x1FFFFFF;
        if (this.matrices[layer] == null) {
            this.matrices[layer] = new boolean[5][5];
        }
        MathHelper.fillBooleanMatrix(this.matrices[layer]);
        this.sendRenderUpdate();
    }

    public int check() {
        for (int i = 0; i < this.matrices.length; i++) {
            if (this.matrices[i] == null || MathHelper.matricesEqual(this.matrices[i], KnappingPatterns.NULL)) {
                MathHelper.resetTensor(this.matrices, i == 0 ? 1 : i);
                return i;
            }
        }
        return -1;
    }

    public void checkPatterns() {
        if (!this.world.isRemote()) {
            int layers = this.world.getBlockState(this.pos).get(BlockMolding.LAYERS);
            if (layers == 1) {
                if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.AXE)) {
                    this.spawnDrops(EvolutionItems.mold_clay_axe);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SHOVEL)) {
                    this.spawnDrops(EvolutionItems.mold_clay_shovel);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.GUARD)) {
                    this.spawnDrops(EvolutionItems.mold_clay_guard);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.HAMMER)) {
                    this.spawnDrops(EvolutionItems.mold_clay_hammer);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.HOE)) {
                    this.spawnDrops(EvolutionItems.mold_clay_hoe);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SAW)) {
                    this.spawnDrops(EvolutionItems.mold_clay_saw);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.KNIFE)) {
                    this.spawnDrops(EvolutionItems.mold_clay_knife);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.PICKAXE)) {
                    this.spawnDrops(EvolutionItems.mold_clay_pickaxe);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SWORD)) {
                    this.spawnDrops(EvolutionItems.mold_clay_sword);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.PROSPECTING)) {
                    this.spawnDrops(EvolutionItems.mold_clay_prospecting);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.SPEAR)) {
                    this.spawnDrops(EvolutionItems.mold_clay_spear);
                }
                else if (MoldingPatterns.comparePatternsOneLayer(this.matrices[0], MoldingPatterns.INGOT)) {
                    this.spawnDrops(EvolutionItems.mold_clay_ingot);
                }
                else if (MathHelper.matricesEqual(this.matrices[0], MoldingPatterns.PLATE)) {
                    this.spawnDrops(EvolutionItems.mold_clay_plate);
                }
            }
            else if (layers == 2) {
                if (MoldingPatterns.comparePatternsTwoLayer(this.matrices, MoldingPatterns.BRICK)) {
                    this.spawnDrops(EvolutionItems.brick_clay, 2);
                }
            }
            else if (layers == 5) {
                if (MathHelper.tensorsEquals(this.matrices, MoldingPatterns.CRUCIBLE)) {
                    this.spawnDrops(EvolutionItems.crucible_clay);
                }
            }
        }
    }

    private void spawnDrops(RegistryObject<Item> item) {
        this.spawnDrops(item, 1);
    }

    private void spawnDrops(RegistryObject<Item> item, int count) {
        Block.spawnAsEntity(this.world, this.pos, new ItemStack(item.get(), count));
        this.world.removeBlock(this.pos, false);
    }
}
