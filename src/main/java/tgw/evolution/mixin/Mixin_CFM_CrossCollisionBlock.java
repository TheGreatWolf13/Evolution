package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OList;

@Mixin(CrossCollisionBlock.class)
public abstract class Mixin_CFM_CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Mutable @Shadow @Final @RestoreFinal protected VoxelShape[] collisionShapeByIndex;
    @Mutable @Shadow @Final @RestoreFinal protected VoxelShape[] shapeByIndex;
    @Mutable @Shadow @Final @RestoreFinal private Object2IntMap<BlockState> stateToIndex;

    @DummyConstructor
    public Mixin_CFM_CrossCollisionBlock(Properties properties, Object2IntMap<BlockState> stateToIndex) {
        super(properties);
        this.stateToIndex = stateToIndex;
    }

    @ModifyConstructor
    public Mixin_CFM_CrossCollisionBlock(float f, float g, float h, float i, float j, BlockBehaviour.Properties properties) {
        super(properties);
        this.stateToIndex = new Object2IntOpenHashMap<>();
        this.collisionShapeByIndex = this.makeShapes(f, g, j, 0.0F, j);
        this.shapeByIndex = this.makeShapes(f, g, h, 0.0F, i);
        OList<BlockState> possibleStates = this.stateDefinition.getPossibleStates_();
        for (int k = 0, len = possibleStates.size(); k < len; ++k) {
            this.getAABBIndex(possibleStates.get(k));
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.collisionShapeByIndex[this.getAABBIndex(state)];
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.shapeByIndex[this.getAABBIndex(state)];
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        return !state.getValue(WATERLOGGED);
    }

    @Shadow
    protected abstract int getAABBIndex(BlockState blockState);

    @Shadow
    protected abstract VoxelShape[] makeShapes(float f, float g, float h, float i, float j);
}
