package tgw.evolution.blocks.tileentities;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.IStoneVariant;
import tgw.evolution.init.EvolutionTileEntities;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

public class TEKnapping extends TileEntity {

    public final boolean[][] matrix = {{true, true, true, true, true},
                                       {true, true, true, true, true},
                                       {true, true, true, true, true},
                                       {true, true, true, true, true},
                                       {true, true, true, true, true}};
    @Nullable
    public VoxelShape hitbox;
    public EnumKnapping type = EnumKnapping.NULL;
    private int encoded = 0x1FF_FFFF;

    public TEKnapping() {
        super(EvolutionTileEntities.TE_KNAPPING.get());
    }

    public void checkParts() {
        if (!this.world.isRemote()) {
            IStoneVariant block = (IStoneVariant) this.world.getBlockState(this.pos).getBlock();
            for (EnumKnapping knapping : EnumKnapping.values()) {
                if (MathHelper.matricesEqual(this.matrix, knapping.getPattern())) {
                    this.spawnDrops(block.getVariant().getKnappedStack(knapping));
                    return;
                }
            }
        }
    }

    private void deserializeToMatrix() {
        int temp = 0x100_0000;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                this.matrix[i][j] = (temp & this.encoded) != 0;
                temp >>= 1;
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 30, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        Evolution.LOGGER.debug("on data knapping plz work ;-;");
        this.hitbox = null;
        this.handleUpdateTag(packet.getNbtCompound());
    }

    @Override
    public void read(CompoundNBT compound) {
        this.encoded = compound.getInt("Parts");
        this.type = EnumKnapping.byId(compound.getByte("Type"));
        this.deserializeToMatrix();
        super.read(compound);
    }

    public void sendRenderUpdate() {
        super.markDirty();
        this.hitbox = null;
        this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), BlockFlags.RERENDER);
    }

    private void serializeToInt() {
        this.encoded = 0;
        int temp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                temp = (this.matrix[i][j] ? 1 : 0) << 24 - 5 * i - j;
                this.encoded |= temp;
            }
        }
    }

    public void setType(EnumKnapping type) {
        this.type = type;
        this.sendRenderUpdate();
    }

    private void spawnDrops(ItemStack stack) {
        Block.spawnAsEntity(this.world, this.pos, stack);
        this.world.removeBlock(this.pos, true);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        this.serializeToInt();
        compound.putInt("Parts", this.encoded);
        compound.putByte("Type", this.type.getId());
        return super.write(compound);
    }
}
