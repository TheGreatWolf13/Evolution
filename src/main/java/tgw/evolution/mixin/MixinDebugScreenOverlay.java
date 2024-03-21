package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.GlUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.util.AllocationRateCalculator;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.ChunkPosMutable;
import tgw.evolution.util.math.Metric;
import tgw.evolution.util.time.Time;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay extends GuiComponent {

    @Unique private final AllocationRateCalculator allocationRateCalculator = new AllocationRateCalculator();
    @Unique private final OList<String> gameInfo = new OArrayList<>();
    @Unique private final OList<String> systemInfo = new OArrayList<>();
    @Shadow public HitResult block;
    @Shadow public HitResult liquid;
    @Unique private @Nullable String cpu;
    @Unique private @Nullable String evolution;
    @Unique private @Nullable String javaVersion;
    @Shadow private @Nullable ChunkPos lastPos = new ChunkPosMutable();
    @Unique private @Nullable String mc;
    @Unique private @Nullable String mcFull;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private @Nullable CompletableFuture<LevelChunk> serverChunk;

    @Contract(value = "_ -> _", pure = true)
    @Shadow
    private static long bytesToMegabytes(long pBytes) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Shadow
    public abstract void clearChunkCache();

    @Shadow
    protected abstract LevelChunk getClientChunk();

    @Unique
    private String getCpu() {
        if (this.cpu == null) {
            this.cpu = "CPU: " + GlUtil.getCpuInfo();
        }
        return this.cpu;
    }

    @Unique
    private String getEvolution() {
        if (this.evolution == null) {
            //noinspection OptionalGetWithoutIsPresent
            this.evolution = "Evolution " + FabricLoader.getInstance().getModContainer(Evolution.MODID).get().getMetadata().getVersion().getFriendlyString();
        }
        return this.evolution;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public List<String> getGameInformation() {
        assert this.lastPos != null;
        assert this.minecraft.player != null;
        assert this.minecraft.level != null;
        assert this.minecraft.getConnection() != null;
        this.gameInfo.clear();
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        Connection connection = this.minecraft.getConnection().getConnection();
        float sentPackets = connection.getAverageSentPackets();
        float receivedPackets = connection.getAverageReceivedPackets();
        String pct;
        if (integratedServer != null) {
            pct = "Integrated server @ " +
                  Metric.format(integratedServer.getAverageTickTime(), 0) +
                  " ms ticks, " +
                  Metric.format(sentPackets, 0) +
                  " sent, " +
                  Metric.format(receivedPackets, 0) +
                  " received";
        }
        else {
            pct = "\"" +
                  this.minecraft.player.getServerBrand() +
                  "\" server, " +
                  Metric.format(sentPackets, 0) +
                  " sent, " +
                  Metric.format(receivedPackets, 0) +
                  " received";
        }
        BlockPos pos = this.minecraft.player.blockPosition();
        if (this.minecraft.showOnlyReducedInfo()) {
            this.gameInfo.add(this.getMC());
            this.gameInfo.add(this.getEvolution());
            this.gameInfo.add(this.minecraft.fpsString);
            this.gameInfo.add(pct);
            this.gameInfo.add(this.minecraft.lvlRenderer().getChunkStatistics());
            this.gameInfo.add(this.minecraft.lvlRenderer().getEntityStatistics());
            this.gameInfo.add("P: " + this.minecraft.particleEngine.getRenderedParticles() + "/" + this.minecraft.particleEngine.countParticles());
            this.gameInfo.add(this.minecraft.level.gatherChunkSourceStats());
            this.gameInfo.add("");
            this.gameInfo.add("Chunk-relative: " + (pos.getX() & 15) + " " + (pos.getY() & 15) + " " + (pos.getZ() & 15));
            return this.gameInfo;
        }
        Entity entity = this.minecraft.player;
        Direction direction = entity.getDirection();
        String towards = switch (direction) {
            case NORTH -> "Towards negative Z";
            case SOUTH -> "Towards positive Z";
            case WEST -> "Towards negative X";
            case EAST -> "Towards positive X";
            default -> "Invalid";
        };
        int secX = SectionPos.blockToSectionCoord(pos.getX());
        int secZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (this.lastPos.x != secX || this.lastPos.z != secZ) {
            ((ChunkPosMutable) this.lastPos).set(secX, secZ);
            this.clearChunkCache();
        }
        Level level = this.getLevel();
        int forcedChunks = level instanceof ServerLevel server ? server.getForcedChunks().size() : 0;
        this.gameInfo.add(this.getMCFull());
        this.gameInfo.add(this.getEvolution());
        this.gameInfo.add(this.minecraft.fpsString);
        this.gameInfo.add(pct);
        this.gameInfo.add(this.minecraft.lvlRenderer().getChunkStatistics());
        this.gameInfo.add(this.minecraft.lvlRenderer().getEntityStatistics());
        this.gameInfo.add("P: " + this.minecraft.particleEngine.getRenderedParticles() + "/" + this.minecraft.particleEngine.countParticles());
        this.gameInfo.add(this.minecraft.level.dimension().location() + " FC: " + forcedChunks);
        this.gameInfo.add("");
        this.gameInfo.add("XYZ: " +
                          Metric.formatForceDecimals(this.minecraft.player.getX(), 3) +
                          " / " +
                          Metric.formatForceDecimals(this.minecraft.player.getY(), 5) +
                          " / " +
                          Metric.formatForceDecimals(this.minecraft.player.getZ(), 3)
        );
        this.gameInfo.add("Block: " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        this.gameInfo.add("Chunk: " +
                          (pos.getX() & 15) +
                          " " +
                          (pos.getY() & 15) +
                          " " +
                          (pos.getZ() & 15) +
                          " in " +
                          secX +
                          " " +
                          SectionPos.blockToSectionCoord(pos.getY()) +
                          " " +
                          secZ +
                          " [" +
                          (secX & 0x1F) +
                          " " +
                          (secZ & 0x1F) +
                          " in r." +
                          (secX >> 5) +
                          "." +
                          (secZ >> 5) +
                          ".mca]"
        );
        this.gameInfo.add("Facing: " +
                          direction +
                          " (" +
                          towards +
                          " ) (" +
                          Metric.formatForceDecimals(Mth.wrapDegrees(entity.getYRot()), 1) +
                          " / " +
                          Metric.formatForceDecimals(Mth.wrapDegrees(entity.getXRot()), 1) +
                          ")"
        );
        LevelChunk levelchunk = this.getClientChunk();
        if (levelchunk.isEmpty()) {
            this.gameInfo.add("Waiting for chunk...");
            this.clearChunkCache();
        }
        else {
            long packedPos = pos.asLong();
            LevelLightEngine lightEngine = this.minecraft.level.getChunkSource().getLightEngine();
            int light = lightEngine.getRawBrightness_(packedPos, 0);
            int skyLight = lightEngine.getLayerListener(LightLayer.SKY).getLightValue_(packedPos);
            int blockLight = lightEngine.getLayerListener(LightLayer.BLOCK).getClampledLightValue(packedPos);
            this.gameInfo.add("Client Light: " + light + " (" + skyLight + " sky, " + blockLight + " block)");
            if (pos.getY() >= this.minecraft.level.getMinBuildHeight() && pos.getY() < this.minecraft.level.getMaxBuildHeight()) {
                this.gameInfo.add("Biome: " + this.minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(this.minecraft.level.getBiome_(pos).value()));
            }
            this.gameInfo.add("Day " + (1 + (this.minecraft.level.getDayTime() + 6L * Time.TICKS_PER_HOUR) / Time.TICKS_PER_DAY));
        }
        PostChain postchain = this.minecraft.gameRenderer.currentEffect();
        if (postchain != null) {
            this.gameInfo.add("Shader: " + postchain.getName());
        }
        this.gameInfo.add(this.minecraft.getSoundManager().getDebugString() + " (Mood " + Math.round(this.minecraft.player.getCurrentMood() * 100.0F) + "%)");
        return this.gameInfo;
    }

    @Unique
    private String getJavaVersion() {
        if (this.javaVersion == null) {
            this.javaVersion = "Java: " + System.getProperty("java.version") + (this.minecraft.is64Bit() ? " 64bit" : " 32bit");
        }
        return this.javaVersion;
    }

    @Shadow
    protected abstract Level getLevel();

    @Unique
    private String getMC() {
        if (this.mc == null) {
            this.mc = "Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")";
        }
        return this.mc;
    }

    @Unique
    private String getMCFull() {
        if (this.mcFull == null) {
            this.mcFull = "Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")";
        }
        return this.mcFull;
    }

    @Shadow
    protected abstract String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> pEntry);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @javax.annotation.Nullable
    @Overwrite
    private @Nullable LevelChunk getServerChunk() {
        if (this.serverChunk == null) {
            ServerLevel serverLevel = this.getServerLevel();
            if (serverLevel != null) {
                assert this.lastPos != null;
                this.serverChunk = serverLevel.getChunkSource()
                                              .getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
                                              .thenApply(e -> {
                                                  Optional<ChunkAccess> left = e.left();
                                                  if (left.isPresent()) {
                                                      return (LevelChunk) left.get();
                                                  }
                                                  //noinspection ReturnOfNull
                                                  return null;
                                              });
            }
            if (this.serverChunk == null) {
                LevelChunk clientChunk = this.getClientChunk();
                this.serverChunk = CompletableFuture.completedFuture(clientChunk);
                return clientChunk;
            }
        }
        return this.serverChunk.getNow(null);
    }

    @Shadow
    protected abstract @Nullable String getServerChunkStats();

    @Shadow
    protected abstract @Nullable ServerLevel getServerLevel();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public List<String> getSystemInformation() {
        assert this.minecraft.level != null;
        long maxMem = Runtime.getRuntime().maxMemory();
        long totMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totMem - freeMem;
        this.systemInfo.clear();
        this.systemInfo.add(this.getJavaVersion());
        this.systemInfo.add("Mem: " + usedMem * 100L / maxMem + "% " + bytesToMegabytes(usedMem) + "/" + bytesToMegabytes(maxMem) + "MB");
        this.systemInfo.add("Allocation rate: " + Metric.bytes(this.allocationRateCalculator.get(usedMem), 1) + "/s");
        this.systemInfo.add("Allocated: " + totMem * 100L / maxMem + "% " + bytesToMegabytes(totMem) + "MB");
        this.systemInfo.add("");
        this.systemInfo.add(this.getCpu());
        this.systemInfo.add("");
        this.systemInfo.add("Display: " + this.minecraft.getWindow().getWidth() + "x" + this.minecraft.getWindow().getHeight() + "(" + GlUtil.getVendor() + ")");
        this.systemInfo.add(GlUtil.getRenderer());
        this.systemInfo.add(GlUtil.getOpenGLVersion());
        if (this.minecraft.showOnlyReducedInfo()) {
            return this.systemInfo;
        }
        if (this.block.getType() == HitResult.Type.BLOCK) {
            BlockHitResult block = (BlockHitResult) this.block;
            int x = block.posX();
            int y = block.posY();
            int z = block.posZ();
            BlockState state = this.minecraft.level.getBlockState_(x, y, z);
            this.systemInfo.add("");
            this.systemInfo.add(ChatFormatting.UNDERLINE + "Targeted Block: " + x + ", " + y + ", " + z);
            this.systemInfo.add(String.valueOf(Registry.BLOCK.getKey(state.getBlock())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                this.systemInfo.add(this.getPropertyValueString(entry));
            }
            state.getTags().forEach(t -> this.systemInfo.add("#" + t.location()));
        }
        if (this.liquid.getType() == HitResult.Type.BLOCK) {
            BlockHitResult liquid = (BlockHitResult) this.liquid;
            int x = liquid.posX();
            int y = liquid.posY();
            int z = liquid.posZ();
            FluidState fluidstate = this.minecraft.level.getFluidState_(x, y, z);
            this.systemInfo.add("");
            this.systemInfo.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + x + ", " + y + ", " + z);
            this.systemInfo.add(String.valueOf(Registry.FLUID.getKey(fluidstate.getType())));
            for (Map.Entry<Property<?>, Comparable<?>> entry1 : fluidstate.getValues().entrySet()) {
                this.systemInfo.add(this.getPropertyValueString(entry1));
            }
            fluidstate.getTags().forEach(t -> this.systemInfo.add("#" + t.location()));
        }
        Entity entity = this.minecraft.crosshairPickEntity;
        if (entity != null) {
            this.systemInfo.add("");
            this.systemInfo.add(ChatFormatting.UNDERLINE + "Targeted Entity");
            this.systemInfo.add(String.valueOf(Registry.ENTITY_TYPE.getKey(entity.getType())));
        }
        return this.systemInfo;
    }
}
