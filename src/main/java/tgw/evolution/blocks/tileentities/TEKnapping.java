package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IRockVariant;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.constants.BlockFlags;

public class TEKnapping extends BlockEntity {

    @Nullable
    public VoxelShape hitbox;
    public KnappingRecipe type = KnappingRecipe.NULL;
    private long parts = Patterns.MATRIX_TRUE;

    public TEKnapping(BlockPos pos, BlockState state) {
        super(EvolutionTEs.KNAPPING.get(), pos, state);
    }

    public void checkParts(Player player) {
        if (this.level != null && !this.level.isClientSide) {
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
        this.parts = tag.getLong("Parts");
        this.type = KnappingRecipe.byId(tag.getByte("Type"));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.hitbox = null;
        this.handleUpdateTag(packet.getTag());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Parts", this.parts);
        tag.putByte("Type", this.type.getId());
    }

    public void sendRenderUpdate() {
        this.setChanged();
        this.hitbox = null;
        assert this.level != null;
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }

    public void setType(KnappingRecipe type) {
        this.type = type;
        this.sendRenderUpdate();
    }

    private void spawnDrops(ItemStack stack) {
        assert this.level != null;
        Block.popResource(this.level, this.worldPosition, stack);
        this.level.removeBlock(this.worldPosition, true);
    }
}
