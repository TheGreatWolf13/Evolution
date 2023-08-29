package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.items.ItemFirewood;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.MathHelper;

import java.util.Arrays;

public class TEFirewoodPile extends BlockEntity {

    private int currentIndex;
    private byte[] firewood = new byte[16];

    public TEFirewoodPile(BlockPos pos, BlockState state) {
        super(EvolutionTEs.FIREWOOD_PILE, pos, state);
        Arrays.fill(this.firewood, (byte) -1);
    }

    public void addFirewood(ItemFirewood itemInHand) {
        this.firewood[this.currentIndex++] = itemInHand.getVariant().getId();
        TEUtils.sendRenderUpdate(this);
    }

    public double calculateMass() {
        double mass = 0;
        for (int i = 0; i < 16; i++) {
            byte id = this.firewood[i];
            if (id == -1) {
                return mass;
            }
            mass += WoodVariant.byId(id).getMass() / 16.0;
        }
        return mass;
    }

    public void dropAll(LevelAccessor level, int x, int y, int z) {
        while (this.currentIndex > 0) {
            Item item = this.removeLastFirewood();
            if (item != null) {
                //noinspection ObjectAllocationInLoop
                BlockUtils.popResource(level, x, y, z, new ItemStack(item));
            }
        }
    }

    public byte[] getFirewood() {
        return this.firewood;
    }

    public @Nullable Item getFirewoodAt(int index) {
        byte id = this.firewood[index];
        if (id == -1) {
            return null;
        }
        return WoodVariant.byId(id).get(EvolutionItems.FIREWOODS);
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.firewood = compound.getByteArray("Firewood");
        this.currentIndex = MathHelper.indexOfOrLength(this.firewood, (byte) -1);
    }

    public @Nullable Item removeLastFirewood() {
        if (this.currentIndex == 0) {
            return null;
        }
        byte id = this.firewood[--this.currentIndex];
        this.firewood[this.currentIndex] = -1;
        if (id == -1) {
            return null;
        }
        return WoodVariant.byId(id).get(EvolutionItems.FIREWOODS);
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.putByteArray("Firewood", this.firewood);
    }
}
