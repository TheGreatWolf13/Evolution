package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchClientboundBlockEntityDataPacket;

@Mixin(ClientboundBlockEntityDataPacket.class)
public abstract class Mixin_CF_ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener>, PatchClientboundBlockEntityDataPacket {

    @Unique private final int x;
    @Unique private final int y;
    @Unique private final int z;
    @Shadow @Final @DeleteField private BlockPos pos;
    @Mutable @Shadow @Final @RestoreFinal private @Nullable CompoundTag tag;
    @Mutable @Shadow @Final @RestoreFinal private BlockEntityType<?> type;

    @ModifyConstructor
    private Mixin_CF_ClientboundBlockEntityDataPacket(BlockPos pos, BlockEntityType<?> type, CompoundTag tag) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.type = type;
        this.tag = tag.isEmpty() ? null : tag;
    }

    @ModifyConstructor
    public Mixin_CF_ClientboundBlockEntityDataPacket(FriendlyByteBuf buf) {
        this.x = buf.readVarInt();
        this.y = buf.readVarInt();
        this.z = buf.readVarInt();
        //noinspection ConstantConditions
        this.type = Registry.BLOCK_ENTITY_TYPE.byId(buf.readVarInt());
        this.tag = buf.readNbt();
    }

    @Overwrite
    public BlockPos getPos() {
        Evolution.deprecatedMethod();
        return BlockPos.ZERO;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    @Overwrite
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.x);
        buf.writeVarInt(this.y);
        buf.writeVarInt(this.z);
        buf.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(this.type));
        buf.writeNbt(this.tag);
    }
}
