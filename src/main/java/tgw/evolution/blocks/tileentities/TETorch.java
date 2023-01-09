package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionTEs;

public class TETorch extends BlockEntity {

    private long timePlaced;

    public TETorch(BlockPos pos, BlockState state) {
        super(EvolutionTEs.TORCH.get(), pos, state);
    }

    public long getTimePlaced() {
        return this.timePlaced;
    }

    @Nullable
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
        this.timePlaced = tag.getLong("TimePlaced");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("TimePlaced", this.timePlaced);
    }

    public void setPlaceTime() {
        assert this.level != null;
        this.setTimePlaced(this.level.getDayTime());
    }

    public void setTimePlaced(long timePlaced) {
        this.timePlaced = timePlaced;
        this.setChanged();
    }

    @Override
    public String toString() {
        return "TETorch{timePlaced=" + this.timePlaced + '}';
    }
}
