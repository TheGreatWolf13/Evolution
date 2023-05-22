package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
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
import tgw.evolution.patches.IClientboundLoginPacketPatch;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(ClientboundLoginPacket.class)
public abstract class ClientboundLoginPacketMixin implements IClientboundLoginPacketPatch {

    @Shadow
    @Final
    private int chunkRadius;
    @Unique
    private long daytime;
    @Shadow
    @Final
    private ResourceKey<Level> dimension;
    @Shadow
    @Final
    private Holder<DimensionType> dimensionType;
    @Shadow
    @Final
    private GameType gameType;
    @Shadow
    @Final
    private boolean hardcore;
    @Shadow
    @Final
    private boolean isDebug;
    @Shadow
    @Final
    private boolean isFlat;
    @Shadow
    @Final
    private Set<ResourceKey<Level>> levels;
    @Shadow
    @Final
    private int maxPlayers;
    @Unique
    private Vec3 motion = Vec3.ZERO;
    @Shadow
    @Final
    private int playerId;
    @Shadow
    @Final
    @Nullable
    private GameType previousGameType;
    @Shadow
    @Final
    private boolean reducedDebugInfo;
    @Shadow
    @Final
    private RegistryAccess.Frozen registryHolder;
    @Shadow
    @Final
    private long seed;
    @Shadow
    @Final
    private boolean showDeathScreen;
    @Shadow
    @Final
    private int simulationDistance;

    @Override
    public long getDaytime() {
        return this.daytime;
    }

    @Override
    public Vec3 getMotion() {
        return this.motion;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
    private void onRead(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.daytime = buffer.readLong();
        this.motion = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public void setDaytime(long daytime) {
        this.daytime = daytime;
    }

    @Override
    public void setMotion(Vec3 motion) {
        this.motion = motion;
    }

    /**
     * @author TheGreatWolf
     * @reason Add more info, avoid allocations when possible
     */
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
        buffer.writeDouble(this.motion.x);
        buffer.writeDouble(this.motion.y);
        buffer.writeDouble(this.motion.z);
    }
}
