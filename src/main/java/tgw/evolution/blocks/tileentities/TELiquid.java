package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionTEs;

public class TELiquid extends BlockEntity {

    /**
     * In units of 10 mL. 1 block contains 1_000 L, a layer contains 125 L.
     */
    private int missingLiquid;

    public TELiquid(BlockPos pos, BlockState state) {
        super(EvolutionTEs.LIQUID.get(), pos, state);
    }

    public int getMissingLiquid() {
        return this.missingLiquid;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.setMissingLiquid(tag.getInt("MissingLiquid"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("MissingLiquid", this.missingLiquid);
    }

    public void setMissingLiquid(int missingLiquid) {
        this.missingLiquid = missingLiquid;
        this.setChanged();
    }

    @Override
    public String toString() {
        return "TELiquid{" + "missingLiquid=" + this.missingLiquid + '}';
    }
}
