package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTEs;

import org.jetbrains.annotations.Nullable;

public class TEPuzzle extends BlockEntity {

    private ResourceLocation attachmentType = EvolutionResources.EMPTY;
    private boolean checkBB = true;
    private String finalState = "minecraft:air";
    private ResourceLocation targetPool = EvolutionResources.EMPTY;

    public TEPuzzle(BlockPos pos, BlockState state) {
        super(EvolutionTEs.PUZZLE.get(), pos, state);
    }

    public ResourceLocation getAttachmentType() {
        return this.attachmentType;
    }

    public boolean getCheckBB() {
        return this.checkBB;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public ResourceLocation getTargetPool() {
        return this.targetPool;
    }

    @Override
    @Nullable
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
        this.attachmentType = new ResourceLocation(tag.getString("AttachmentType"));
        this.targetPool = new ResourceLocation(tag.getString("TargetPool"));
        this.finalState = tag.getString("FinalState");
        this.checkBB = tag.getBoolean("CheckBB");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag());
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("AttachmentType", this.attachmentType.toString());
        tag.putString("TargetPool", this.targetPool.toString());
        tag.putString("FinalState", this.finalState);
        tag.putBoolean("CheckBB", this.checkBB);
    }

    public void setAttachmentType(ResourceLocation attachmentType) {
        this.attachmentType = attachmentType;
    }

    public void setCheckBB(boolean checkBB) {
        this.checkBB = checkBB;
    }

    public void setFinalState(String finalState) {
        this.finalState = finalState;
    }

    public void setTargetPool(ResourceLocation targetPool) {
        this.targetPool = targetPool;
    }
}
