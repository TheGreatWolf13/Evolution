package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSCollision;
import tgw.evolution.util.math.Units;

import org.jetbrains.annotations.Nullable;

public class BlockGlass extends BlockGeneric implements ICollisionBlock {

    public BlockGlass() {
        super(Properties.of(Material.GLASS).strength(0.3f).sound(SoundType.GLASS).noOcclusion());
    }

    @Override
    public boolean collision(Level level, BlockPos pos, Entity entity, double speed, double mass, @Nullable Direction.Axis axis) {
        speed = Units.toSISpeed(speed);
        double kineticEnergy = speed * speed * mass / 2;
        double area = 0;
        if (axis == null) {
            area = entity.getBbWidth() * entity.getBbHeight();
        }
        else {
            switch (axis) {
                case X, Z -> area = entity.getBbWidth() * entity.getBbHeight();
                case Y -> area = entity.getBbWidth() * entity.getBbWidth();
            }
        }
        double energyDensity = kineticEnergy / area;
        if (energyDensity >= 5_000) {
            BlockUtils.destroyBlock(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            this.collision(level, pos, entity, entity.getDeltaMovement().y, living.getAttributeValue(EvolutionAttributes.MASS.get()),
                           Direction.Axis.Y);
        }
        else if (level.isClientSide && entity.equals(Evolution.PROXY.getClientPlayer())) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSCollision(pos, entity.getDeltaMovement().y, Direction.Axis.Y));
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6f;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public float getSlowdownSide(BlockState state) {
        return 0;
    }

    @Override
    public float getSlowdownTop(BlockState state) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }
}
