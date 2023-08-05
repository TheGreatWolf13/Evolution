package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import tgw.evolution.util.math.MathHelper;

public class TEKnapping extends BlockEntity {

    public KnappingRecipe type = KnappingRecipe.NULL;
    private @Nullable VoxelShape hitbox;
    private long parts = Patterns.MATRIX_TRUE;

    public TEKnapping(BlockPos pos, BlockState state) {
        super(EvolutionTEs.KNAPPING, pos, state);
    }

    private VoxelShape calculateHitbox() {
        return MathHelper.generateShapeFromPattern(this.getParts());
    }

    public void checkParts(Player player) {
        if (this.level != null && !this.level.isClientSide) {
            IRockVariant block = (IRockVariant) this.level.getBlockState(this.worldPosition).getBlock();
            if (this.parts == this.type.getPattern()) {
                this.spawnDrops(block.rockVariant().getKnappedStack(this.type));
                player.awardStat(EvolutionStats.TIMES_KNAPPING);
            }
        }
    }

    public void clearPart(int i, int j) {
        this.parts &= ~(1L << 8 * j + i);
    }

    public VoxelShape getOrMakeHitbox() {
        if (this.hitbox == null) {
            this.hitbox = this.calculateHitbox();
        }
        return this.hitbox;
    }

    public boolean getPart(int i, int j) {
        return (this.parts & 1L << 8 * j + i) != 0;
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
        this.hitbox = null; //Remove cache, since it might have changed
        super.load(tag);
        this.parts = tag.getLong("Parts");
        this.type = KnappingRecipe.byId(tag.getByte("Type"));
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
                                    BlockFlags.RENDER_MAINTHREAD);
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
