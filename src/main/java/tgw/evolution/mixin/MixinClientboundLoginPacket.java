package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.PatchClientboundLoginPacket;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(ClientboundLoginPacket.class)
public abstract class MixinClientboundLoginPacket implements PatchClientboundLoginPacket, Packet<ClientGamePacketListener> {

    @Shadow @Final private int chunkRadius;
    @Unique private long daytime;
    @Shadow @Final private ResourceKey<Level> dimension;
    @Shadow @Final private Holder<DimensionType> dimensionType;
    @Shadow @Final private GameType gameType;
    @Shadow @Final private boolean hardcore;
    @Shadow @Final private boolean isDebug;
    @Shadow @Final private boolean isFlat;
    @Shadow @Final private Set<ResourceKey<Level>> levels;
    @Shadow @Final private int maxPlayers;
    @Unique private double motionX;
    @Unique private double motionY;
    @Unique private double motionZ;
    @Shadow @Final private int playerId;
    @Shadow @Final @Nullable private GameType previousGameType;
    @Shadow @Final private boolean reducedDebugInfo;
    @Shadow @Final private RegistryAccess.Frozen registryHolder;
    @Shadow @Final private long seed;
    @Shadow @Final private boolean showDeathScreen;
    @Shadow @Final private int simulationDistance;

    @Override
    public long getDaytime() {
        return this.daytime;
    }

    @Override
    public double getMotionX() {
        return this.motionX;
    }

    @Override
    public double getMotionY() {
        return this.motionY;
    }

    @Override
    public double getMotionZ() {
        return this.motionZ;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
    private void onRead(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.daytime = buffer.readLong();
        this.motionX = buffer.readDouble();
        this.motionY = buffer.readDouble();
        this.motionZ = buffer.readDouble();
    }

    @Override
    public ClientboundLoginPacket setDaytime(long daytime) {
        this.daytime = daytime;
        return (ClientboundLoginPacket) (Object) this;
    }

    @Override
    public ClientboundLoginPacket setMotion(Vec3 motion) {
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
        return (ClientboundLoginPacket) (Object) this;
    }

    /**
     * @author TheGreatWolf
     * @reason Add more info, avoid allocations when possible
     */
    @Override
    @Overwrite
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.playerId);
        buffer.writeBoolean(this.hardcore);
        buffer.writeByte(this.gameType.getId());
        buffer.writeByte(GameType.getNullableId(this.previousGameType));
        Set<ResourceKey<Level>> levels = this.levels;
        buffer.writeVarInt(levels.size());
        for (ResourceKey<Level> level : levels) {
            buffer.writeResourceLocation(level.location());
        }
        buffer.writeWithCodec(RegistryAccess.NETWORK_CODEC, this.registryHolder);
        buffer.writeWithCodec(DimensionType.CODEC, this.dimensionType);
        buffer.writeResourceLocation(this.dimension.location());
        buffer.writeLong(this.seed);
        buffer.writeVarInt(this.maxPlayers);
        buffer.writeVarInt(this.chunkRadius);
        buffer.writeVarInt(this.simulationDistance);
        buffer.writeBoolean(this.reducedDebugInfo);
        buffer.writeBoolean(this.showDeathScreen);
        buffer.writeBoolean(this.isDebug);
        buffer.writeBoolean(this.isFlat);
        buffer.writeLong(this.daytime);
        buffer.writeDouble(this.motionX);
        buffer.writeDouble(this.motionY);
        buffer.writeDouble(this.motionZ);
    }
}
