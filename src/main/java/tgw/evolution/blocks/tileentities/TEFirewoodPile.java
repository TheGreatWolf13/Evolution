package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.items.ItemFirewood;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.WoodVariant;

import java.util.Arrays;

public class TEFirewoodPile extends BlockEntity {

    private int currentIndex;
    private byte[] firewood = new byte[16];

    public TEFirewoodPile(BlockPos pos, BlockState state) {
        super(EvolutionTEs.FIREWOOD_PILE.get(), pos, state);
        Arrays.fill(this.firewood, (byte) -1);
    }

    public void addFirewood(ItemFirewood itemInHand) {
        this.firewood[this.currentIndex++] = itemInHand.getVariant().getId();
        this.sendRenderUpdate();
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

    public void dropAll(Level level, BlockPos pos) {
        while (this.currentIndex > 0) {
            Item item = this.removeLastFirewood();
            if (item != null) {
                //noinspection ObjectAllocationInLoop
                Block.popResource(level, pos, new ItemStack(item));
            }
        }
    }

    public byte[] getFirewood() {
        return this.firewood;
    }

    @Nullable
    public Item getFirewoodAt(int index) {
        byte id = this.firewood[index];
        if (id == -1) {
            return null;
        }
        return WoodVariant.byId(id).get(EvolutionItems.FIREWOODS);
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
    public void load(CompoundTag compound) {
        super.load(compound);
        this.firewood = compound.getByteArray("Firewood");
        int i;
        for (i = 0; i < 16; i++) {
            if (this.firewood[i] == -1) {
                break;
            }
        }
        this.currentIndex = i;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.handleUpdateTag(packet.getTag());
        assert this.level != null;
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }

    @Nullable
    public Item removeLastFirewood() {
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
        super.saveAdditional(compound);
        compound.putByteArray("Firewood", this.firewood);
    }

    public void sendRenderUpdate() {
        this.setChanged();
        assert this.level != null;
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }
}
