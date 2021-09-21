package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.items.ItemFirewood;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;
import java.util.Arrays;

public class TEFirewoodPile extends TileEntity {

    private int currentIndex;
    private byte[] firewood = new byte[16];

    public TEFirewoodPile() {
        super(EvolutionTEs.FIREWOOD_PILE.get());
        Arrays.fill(this.firewood, (byte) -1);
    }

    public void addFirewood(ItemFirewood itemInHand) {
        this.firewood[this.currentIndex++] = itemInHand.getVariant().getId();
        this.sendRenderUpdate();
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
        return WoodVariant.byId(id).getFirewood();
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 30, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.handleUpdateTag(this.level.getBlockState(this.worldPosition), packet.getTag());
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
        return WoodVariant.byId(id).getFirewood();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putByteArray("Firewood", this.firewood);
        return super.save(compound);
    }

    public void sendRenderUpdate() {
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition,
                                    this.level.getBlockState(this.worldPosition),
                                    this.level.getBlockState(this.worldPosition),
                                    BlockFlags.RERENDER);
    }
}
