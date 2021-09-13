package tgw.evolution.blocks.tileentities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import tgw.evolution.blocks.IRockVariant;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.BlockFlags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TEKnapping extends TileEntity {

    @Nullable
    public VoxelShape hitbox;
    @Nonnull
    public KnappingRecipe type = KnappingRecipe.NULL;
    private long parts = Patterns.MATRIX_TRUE;

    public TEKnapping() {
        super(EvolutionTEs.KNAPPING.get());
    }

    public void checkParts(PlayerEntity player) {
        if (!this.level.isClientSide) {
            IRockVariant block = (IRockVariant) this.level.getBlockState(this.worldPosition).getBlock();
            if (this.parts == this.type.getPattern()) {
                this.spawnDrops(block.getVariant().getKnappedStack(this.type));
                player.awardStat(EvolutionStats.TIMES_KNAPPING);
            }
        }
    }

    public void clearPart(int i, int j) {
        this.parts &= ~(1L << (7 - j) * 8 + 7 - i);
    }

    public boolean getPart(int i, int j) {
        return (this.parts >> (7 - j) * 8 + 7 - i & 1) != 0;
    }

    public long getParts() {
        return this.parts;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 30, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.parts = compound.getLong("Parts");
        this.type = KnappingRecipe.byId(compound.getByte("Type"));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.hitbox = null;
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), packet.getTag());
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putLong("Parts", this.parts);
        compound.putByte("Type", this.type.getId());
        return super.save(compound);
    }

    public void sendRenderUpdate() {
        this.setChanged();
        this.hitbox = null;
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }

    public void setType(@Nonnull KnappingRecipe type) {
        this.type = type;
        this.sendRenderUpdate();
    }

    private void spawnDrops(ItemStack stack) {
        Block.popResource(this.level, this.worldPosition, stack);
        this.level.removeBlock(this.worldPosition, true);
    }
}
