package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTEs;
import tgw.evolution.items.ItemLog;
import tgw.evolution.items.ItemUtils;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.constants.WoodVariant;

public class TEChopping extends BlockEntity {

    private byte breakProgress;
    private byte id = -1;

    public TEChopping(BlockPos pos, BlockState state) {
        super(EvolutionTEs.CHOPPING, pos, state);
    }

    public void breakLog(Player player) {
        if (this.level != null && !this.level.isClientSide) {
            Item firewood = WoodVariant.byId(this.id).get(EvolutionItems.FIREWOODS);
            ItemStack stack = new ItemStack(firewood, 16);
            BlockPos pos = this.worldPosition;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            BlockUtils.dropItemStack(this.level, x, y, z, stack, 0.5);
            Block block = WoodVariant.byId(this.id).get(EvolutionBlocks.LOGS);
            ItemUtils.damageItem(player.getMainHandItem(), player, ItemModular.DamageCause.BREAK_BLOCK, EquipmentSlot.MAINHAND, block.getHarvestLevel(block.defaultBlockState(), this.level, x, y, z));
            player.awardStat(Stats.ITEM_CRAFTED.get(firewood), 16);
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    public void dropLog() {
        if (this.level != null && this.hasLog() && !this.level.isClientSide) {
            BlockPos pos = this.worldPosition;
            BlockUtils.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.getItemStack());
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
    }

    public byte getBreakProgress() {
        return this.breakProgress;
    }

    public ItemStack getItemStack() {
        return new ItemStack(WoodVariant.byId(this.id).get(EvolutionItems.LOGS));
    }

    /**
     * Return {@link ClientboundBlockEntityDataPacket#create(BlockEntity)} to send data to the client, or {@code null} if no data needs to be sent to
     * the client.
     */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * This data is sent to the client.
     */
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

    /**
     * Load the data on the server and on the client. Always call {@code super}.
     */
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.id = compound.getByte("Wood");
        this.breakProgress = compound.getByte("Break");
    }

    public void removeStack(Player player) {
        ItemStack stack = this.getItemStack();
        assert this.level != null;
        if (!this.level.isClientSide && !player.getInventory().add(stack)) {
            BlockPos pos = this.worldPosition;
            BlockUtils.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        this.id = -1;
        this.breakProgress = 0;
        TEUtils.sendRenderUpdate(this);
        player.level.playSound(player, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1.0f);
    }

    /**
     * Saves the data on the server. No need to call {@code super} the the parent class is {@link BlockEntity}.
     */
    @Override
    protected void saveAdditional(CompoundTag compound) {
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
