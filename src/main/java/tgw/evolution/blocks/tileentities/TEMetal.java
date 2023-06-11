package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.constants.MetalVariant;
import tgw.evolution.util.constants.Oxidation;
import tgw.evolution.util.math.DirectionUtil;

public class TEMetal extends BlockEntity {

    /**
     * Bit 0: isExposed;<br>
     * Bit 1: isAirExposed;<br>
     * Bit 2: isWaterExposed;<br>
     */
    protected byte exposed;
    protected long lastTick = -1;
    protected long oxidationTicks;
    protected byte partialOxidation;

    public TEMetal(BlockPos pos, BlockState state) {
        super(EvolutionTEs.METAL.get(), pos, state);
    }

    public boolean isAirExposed() {
        return (this.exposed & 2) != 0;
    }

    public boolean isExposed() {
        return (this.exposed & 1) != 0;
    }

    public boolean isWaterExposed() {
        return (this.exposed & 4) != 0;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.lastTick = tag.getLong("LastTick");
        this.exposed = tag.getByte("Exposed");
        this.oxidationTicks = tag.getLong("OxidationTicks");
        this.partialOxidation = tag.getByte("PartialOxidation");
    }

    public void oxidationTick(MetalVariant metal, Oxidation oxidation) {
        assert this.level != null;
        long currentTick = this.level.getGameTime();
        long passedTicks = currentTick - this.lastTick;
        this.lastTick = currentTick;
        boolean wasExposed = this.isExposed();
        boolean wasAirExp = this.isAirExposed();
        boolean wasWaterExp = this.isWaterExposed();
        this.refreshExposed();
        if (passedTicks > 0 && oxidation != Oxidation.OXIDIZED) {
            if (wasExposed) {
                passedTicks += this.partialOxidation;
                this.partialOxidation = 0;
                if (wasAirExp) {
                    this.oxidationTicks += passedTicks;
                }
                else if (wasWaterExp) {
                    this.oxidationTicks += passedTicks / 5;
                    this.partialOxidation = (byte) (passedTicks % 5);
                }
                if (this.shouldOxidize(metal, oxidation)) {
                    this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, 0);
                }
            }
        }
    }

    public void refreshExposed() {
        this.exposed = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        boolean exp = false;
        boolean water = false;
        boolean air = false;
        for (Direction dir : DirectionUtil.ALL) {
            if (exp && water && air) {
                break;
            }
            mutablePos.setWithOffset(this.worldPosition, dir);
            if (!water) {
                assert this.level != null;
                FluidState fluid = this.level.getFluidState(mutablePos);
                if (fluid.is(FluidTags.WATER)) {
                    water = true;
                    exp = true;
                    this.setWaterExposed(true);
                    this.setExposed(true);
                    continue;
                }
            }
            if (!air) {
                if (!BlockUtils.hasSolidSide(this.level, mutablePos, dir.getOpposite())) {
                    FluidState fluid = this.level.getFluidState(mutablePos);
                    if (fluid.isEmpty()) {
                        air = true;
                        exp = true;
                        this.setAirExposed(true);
                        this.setExposed(true);
                    }
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("LastTick", this.lastTick);
        tag.putLong("OxidationTicks", this.oxidationTicks);
        tag.putByte("Exposed", this.exposed);
        tag.putByte("PartialOxidation", this.partialOxidation);
    }

    public void setAirExposed(boolean airExposed) {
        if (airExposed) {
            this.exposed |= 2;
        }
        else {
            this.exposed &= ~2;
        }
    }

    public void setExposed(boolean exposed) {
        if (exposed) {
            this.exposed |= 1;
        }
        else {
            this.exposed &= ~1;
        }
    }

    public void setWaterExposed(boolean waterExposed) {
        if (waterExposed) {
            this.exposed |= 4;
        }
        else {
            this.exposed &= ~4;
        }
    }

    public boolean shouldOxidize(MetalVariant metal, Oxidation oxidation) {
        return this.oxidationTicks >= oxidation.getTimeForNextStage(metal);
    }

    public void updateFromOld(TEMetal currentTile) {
        this.partialOxidation = currentTile.partialOxidation;
        this.oxidationTicks = currentTile.oxidationTicks;
    }
}
