package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTileEntities;

import javax.annotation.Nullable;

public class TEPuzzle extends TileEntity {

    private ResourceLocation attachmentType = EvolutionResources.EMPTY;
    private boolean checkBB = true;
    private String finalState = "minecraft:air";
    private ResourceLocation targetPool = EvolutionResources.EMPTY;

    public TEPuzzle() {
        super(EvolutionTileEntities.TE_PUZZLE.get());
    }

    public ResourceLocation getAttachmentType() {
        return this.attachmentType;
    }

    public void setAttachmentType(ResourceLocation attachmentType) {
        this.attachmentType = attachmentType;
    }

    public boolean getCheckBB() {
        return this.checkBB;
    }

    public void setCheckBB(boolean checkBB) {
        this.checkBB = checkBB;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public void setFinalState(String finalState) {
        this.finalState = finalState;
    }

    public ResourceLocation getTargetPool() {
        return this.targetPool;
    }

    public void setTargetPool(ResourceLocation targetPool) {
        this.targetPool = targetPool;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 12, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.attachmentType = new ResourceLocation(compound.getString("AttachementType"));
        this.targetPool = new ResourceLocation(compound.getString("TargetPool"));
        this.finalState = compound.getString("FinalState");
        this.checkBB = compound.getBoolean("CheckBB");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putString("AttachementType", this.attachmentType.toString());
        compound.putString("TargetPool", this.targetPool.toString());
        compound.putString("FinalState", this.finalState);
        compound.putBoolean("CheckBB", this.checkBB);
        return compound;
    }
}
