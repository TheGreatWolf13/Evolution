package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.events.ItemEvents;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.items.ItemLog;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.util.constants.WoodVariant;

public class TEChopping extends BlockEntity {

    private byte breakProgress;
    private byte id = -1;

    public TEChopping(BlockPos pos, BlockState state) {
        super(EvolutionTEs.CHOPPING.get(), pos, state);
    }

    public void breakLog(Player player) {
        if (this.level != null && !this.level.isClientSide) {
            Item firewood = WoodVariant.byId(this.id).getFirewood();
            ItemStack stack = new ItemStack(firewood, 16);
            BlockUtils.dropItemStack(this.level, this.worldPosition, stack, 0.5);
            Block block = WoodVariant.byId(this.id).getLog();
            ItemEvents.damageItem(player.getMainHandItem(), player, ItemModular.DamageCause.BREAK_BLOCK, EquipmentSlot.MAINHAND,
                                  ((IBlockPatch) block).getHarvestLevel(block.defaultBlockState(), this.level, null));
            player.awardStat(Stats.ITEM_CRAFTED.get(firewood), 16);
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    public void dropLog() {
        if (this.level != null && this.hasLog() && !this.level.isClientSide) {
            BlockUtils.dropItemStack(this.level, this.worldPosition, this.getItemStack());
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    public byte getBreakProgress() {
        return this.breakProgress;
    }

    public ItemStack getItemStack() {
        return new ItemStack(WoodVariant.byId(this.id).getLogItem());
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public boolean hasLog() {
        return this.id != -1;
    }

    public int increaseBreakProgress() {
        this.breakProgress++;
        TEUtils.sendRenderUpdate(this);
        return this.breakProgress;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.id = compound.getByte("Wood");
        this.breakProgress = compound.getByte("Break");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.handleUpdateTag(packet.getTag());
        TEUtils.sendRenderUpdate(this);
    }

    public void removeStack(Player player) {
        ItemStack stack = this.getItemStack();
        assert this.level != null;
        if (!this.level.isClientSide && !player.getInventory().add(stack)) {
            BlockUtils.dropItemStack(this.level, this.worldPosition, stack);
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
        player.level.playSound(player, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1.0f);
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putByte("Wood", this.id);
        compound.putByte("Break", this.breakProgress);
    }

    public void setStack(Player player, InteractionHand hand) {
        this.id = ((ItemLog) player.getItemInHand(hand).getItem()).variant.getId();
        TEUtils.sendRenderUpdate(this);
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }
        player.level.playSound(player, player, SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    @Override
    public String toString() {
        return "TEChopping{" +
               "breakProgress=" +
               this.breakProgress +
               ", id=" +
               this.id +
               '}';
    }
}
