package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.MetalVariant;
import tgw.evolution.util.Oxidation;

public class TEMetal extends TileEntity {

    /**
     * Bit 0: isExposed;<br>
     * Bit 1: isAirExposed;<br>
     * Bit 2: isWaterExposed;<br>
     */
    protected byte exposed;
    protected long lastTick = -1;
    protected long oxidationTicks;
    protected byte partialOxidation;

    public TEMetal() {
        super(EvolutionTEs.METAL.get());
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
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.lastTick = compound.getLong("LastTick");
        this.exposed = compound.getByte("Exposed");
        this.oxidationTicks = compound.getLong("OxidationTicks");
        this.partialOxidation = compound.getByte("PartialOxidation");
    }

    public void oxidationTick(MetalVariant metal, Oxidation oxidation) {
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
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        boolean exp = false;
        boolean water = false;
        boolean air = false;
        for (Direction dir : DirectionUtil.ALL) {
            if (exp && water && air) {
                break;
            }
            mutablePos.setWithOffset(this.worldPosition, dir);
            if (!water) {
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
                if (!BlockUtils.hasSolidSide(this.level, mutablePos, DirectionUtil.getOpposite(dir))) {
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
    public CompoundNBT save(CompoundNBT compound) {
        compound.putLong("LastTick", this.lastTick);
        compound.putLong("OxidationTicks", this.oxidationTicks);
        compound.putByte("Exposed", this.exposed);
        compound.putByte("PartialOxidation", this.partialOxidation);
        return super.save(compound);
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
