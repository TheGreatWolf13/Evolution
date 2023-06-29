package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.GlUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
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
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.patches.IParticleEnginePatch;
import tgw.evolution.util.AllocationRateCalculator;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;
import tgw.evolution.util.math.Metric;
import tgw.evolution.util.time.Time;

import java.util.List;
import java.util.Map;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin extends GuiComponent {

    @Shadow
    @Final
    private static Map<Heightmap.Types, String> HEIGHTMAP_NAMES;
    private final AllocationRateCalculator allocationRateCalculator = new AllocationRateCalculator();
    private final OList<String> gameInfo = new OArrayList<>();
    private final Heightmap.Types[] heightmapTypes = Heightmap.Types.values();
    private final MobCategory[] mobCategories = MobCategory.values();
    private final OList<String> systemInfo = new OArrayList<>();
    @Shadow
    protected HitResult block;
    @Shadow
    protected HitResult liquid;
    @Nullable
    @Unique
    private String cpu;
    @Unique
    @Nullable
    private String javaVersion;
    @Shadow
    @Nullable
    private ChunkPos lastPos;
    @Unique
    @Nullable
    private String mc;
    @Unique
    @Nullable
    private String mcFull;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private static long bytesToMegabytes(long pBytes) {
        throw new AbstractMethodError();
    }

    @Shadow
    public abstract void clearChunkCache();

    @Shadow
    protected abstract LevelChunk getClientChunk();

    private String getCpu() {
        if (this.cpu == null) {
            this.cpu = "CPU: " + GlUtil.getCpuInfo();
        }
        return this.cpu;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    protected List<String> getGameInformation() {
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
                  " tx, " +
                  Metric.format(receivedPackets, 0) +
                  " rx";
        }
        else {
            pct = "\"" +
                  this.minecraft.player.getServerBrand() +
                  "\" server, " +
                  Metric.format(sentPackets, 0) +
                  " tx, " +
                  Metric.format(receivedPackets, 0) +
                  " rx";
        }
        BlockPos pos = this.minecraft.player.blockPosition();
        if (this.minecraft.showOnlyReducedInfo()) {
            this.gameInfo.add(this.getMC());
            this.gameInfo.add(this.minecraft.fpsString);
            this.gameInfo.add(pct);
            this.gameInfo.add(((IMinecraftPatch) this.minecraft).lvlRenderer().getChunkStatistics());
            this.gameInfo.add(((IMinecraftPatch) this.minecraft).lvlRenderer().getEntityStatistics());
            this.gameInfo.add("P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount());
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
        if (this.lastPos == null ||
            this.lastPos.x != SectionPos.blockToSectionCoord(pos.getX()) ||
            this.lastPos.z != SectionPos.blockToSectionCoord(pos.getZ())) {
            this.lastPos = new ChunkPos(pos);
            this.clearChunkCache();
        }
        Level level = this.getLevel();
        LongSet forcedChunks = level instanceof ServerLevel server ? server.getForcedChunks() : LongSets.EMPTY_SET;
        this.gameInfo.add(this.getMCFull());
        this.gameInfo.add(this.minecraft.fpsString);
        this.gameInfo.add(pct);
        this.gameInfo.add(((IMinecraftPatch) this.minecraft).lvlRenderer().getChunkStatistics());
        this.gameInfo.add(((IMinecraftPatch) this.minecraft).lvlRenderer().getEntityStatistics());
        this.gameInfo.add("P: " +
                          ((IParticleEnginePatch) this.minecraft.particleEngine).getRenderedParticles() +
                          "/" +
                          this.minecraft.particleEngine.countParticles());
        this.gameInfo.add(this.minecraft.level.gatherChunkSourceStats());
        String serverChunkStats = this.getServerChunkStats();
        if (serverChunkStats != null) {
            this.gameInfo.add(serverChunkStats);
        }
        this.gameInfo.add(this.minecraft.level.dimension().location() + " FC: " + forcedChunks.size());
        this.gameInfo.add("");
        this.gameInfo.add("XYZ: " +
                          Metric.formatForceDecimals(this.minecraft.player.getX(), 3) +
                          " / " +
                          Metric.formatForceDecimals(this.minecraft.player.getY(), 5) +
                          " / " +
                          Metric.formatForceDecimals(this.minecraft.player.getZ(), 3));
        this.gameInfo.add("Block: " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        this.gameInfo.add("Chunk: " +
                          (pos.getX() & 15) +
                          " " +
                          (pos.getY() & 15) +
                          " " +
                          (pos.getZ() & 15) +
                          " in " +
                          SectionPos.blockToSectionCoord(pos.getX()) +
                          " " +
                          SectionPos.blockToSectionCoord(pos.getY()) +
                          " " +
                          SectionPos.blockToSectionCoord(pos.getZ()));
        this.gameInfo.add("Facing: " +
                          direction +
                          " (" +
                          towards +
                          " ) (" +
                          Metric.formatForceDecimals(Mth.wrapDegrees(entity.getYRot()), 1) +
                          " / " +
                          Metric.formatForceDecimals(Mth.wrapDegrees(entity.getXRot()), 1) +
                          ")");
        LevelChunk levelchunk = this.getClientChunk();
        if (levelchunk.isEmpty()) {
            this.gameInfo.add("Waiting for chunk...");
            this.clearChunkCache();
        }
        else {
            int light = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(pos, 0);
            int skyLight = this.minecraft.level.getBrightness(LightLayer.SKY, pos);
            int blockLight = this.minecraft.level.getBrightness(LightLayer.BLOCK, pos);
            this.gameInfo.add("Client Light: " + light + " (" + skyLight + " sky, " + blockLight + " block)");
            LevelChunk chunk = this.getServerChunk();
            StringBuilder builder = new StringBuilder("CH");
            for (Heightmap.Types types : this.heightmapTypes) {
                if (types.sendToClient()) {
                    builder.append(" ").append(HEIGHTMAP_NAMES.get(types)).append(": ").append(levelchunk.getHeight(types, pos.getX(), pos.getZ()));
                }
            }
            this.gameInfo.add(builder.toString());
            builder.setLength(0);
            builder.append("SH");
            for (Heightmap.Types types : this.heightmapTypes) {
                if (types.keepAfterWorldgen()) {
                    builder.append(" ").append(HEIGHTMAP_NAMES.get(types)).append(": ");
                    if (chunk != null) {
                        builder.append(chunk.getHeight(types, pos.getX(), pos.getZ()));
                    }
                    else {
                        builder.append("??");
                    }
                }
            }
            this.gameInfo.add(builder.toString());
            if (pos.getY() >= this.minecraft.level.getMinBuildHeight() && pos.getY() < this.minecraft.level.getMaxBuildHeight()) {
                this.gameInfo.add("Biome: " +
                                  this.minecraft.level.registryAccess()
                                                      .registryOrThrow(Registry.BIOME_REGISTRY)
                                                      .getKey(this.minecraft.level.getBiome(pos).value()));
                this.gameInfo.add("Day " + (1 + (this.minecraft.level.getDayTime() + 6L * Time.TICKS_PER_HOUR) / Time.TICKS_PER_DAY));
            }
        }
        ServerLevel serverLevel = this.getServerLevel();
        if (serverLevel != null) {
            ServerChunkCache chunkSource = serverLevel.getChunkSource();
            ChunkGenerator generator = chunkSource.getGenerator();
            Climate.Sampler sampler = generator.climateSampler();
            BiomeSource biomeSource = generator.getBiomeSource();
            biomeSource.addDebugInfo(this.gameInfo, pos, sampler);
            NaturalSpawner.SpawnState lastSpawnState = chunkSource.getLastSpawnState();
            if (lastSpawnState != null) {
                Object2IntMap<MobCategory> categoryCounts = lastSpawnState.getMobCategoryCounts();
                int spawnableCount = lastSpawnState.getSpawnableChunkCount();
                StringBuilder builder = new StringBuilder("SC: ");
                builder.append(spawnableCount);
                builder.append(", ");
                for (MobCategory category : this.mobCategories) {
                    builder.append(Character.toUpperCase(category.getName().charAt(0)));
                    builder.append(": ");
                    builder.append(categoryCounts.getInt(category));
                    builder.append(", ");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.deleteCharAt(builder.length() - 1);
                this.gameInfo.add(builder.toString());
            }
            else {
                this.gameInfo.add("SC: N/A");
            }
        }
        PostChain postchain = this.minecraft.gameRenderer.currentEffect();
        if (postchain != null) {
            this.gameInfo.add("Shader: " + postchain.getName());
        }
        this.gameInfo.add(
                this.minecraft.getSoundManager().getDebugString() + " (Mood " + Math.round(this.minecraft.player.getCurrentMood() * 100.0F) + "%)");
        return this.gameInfo;
    }

    private String getJavaVersion() {
        if (this.javaVersion == null) {
            this.javaVersion = "Java: " + System.getProperty("java.version") + (this.minecraft.is64Bit() ? " 64bit" : " 32bit");
        }
        return this.javaVersion;
    }

    @Shadow
    protected abstract Level getLevel();

    private String getMC() {
        if (this.mc == null) {
            this.mc = "Minecraft " +
                      SharedConstants.getCurrentVersion().getName() +
                      " (" +
                      this.minecraft.getLaunchedVersion() +
                      "/" +
                      ClientBrandRetriever.getClientModName() +
                      ")";
        }
        return this.mc;
    }

    private String getMCFull() {
        if (this.mcFull == null) {
            this.mcFull = "Minecraft " +
                          SharedConstants.getCurrentVersion().getName() +
                          " (" +
                          this.minecraft.getLaunchedVersion() +
                          "/" +
                          ClientBrandRetriever.getClientModName() +
                          ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) +
                          ")";
        }
        return this.mcFull;
    }

    @Shadow
    protected abstract String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> pEntry);

    @Shadow
    @Nullable
    protected abstract LevelChunk getServerChunk();

    @Shadow
    @Nullable
    protected abstract String getServerChunkStats();

    @Shadow
    @Nullable
    protected abstract ServerLevel getServerLevel();

    protected List<String> getSystemInformation() {
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
        this.systemInfo.add(
                "Display: " + this.minecraft.getWindow().getWidth() + "x" + this.minecraft.getWindow().getHeight() + "(" + GlUtil.getVendor() + ")");
        this.systemInfo.add(GlUtil.getRenderer());
        this.systemInfo.add(GlUtil.getOpenGLVersion());
        if (this.minecraft.showOnlyReducedInfo()) {
            return this.systemInfo;
        }
        if (this.block.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) this.block).getBlockPos();
            BlockState state = this.minecraft.level.getBlockState(hitPos);
            this.systemInfo.add("");
            this.systemInfo.add(ChatFormatting.UNDERLINE + "Targeted Block: " + hitPos.getX() + ", " + hitPos.getY() + ", " + hitPos.getZ());
            this.systemInfo.add(String.valueOf(Registry.BLOCK.getKey(state.getBlock())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                this.systemInfo.add(this.getPropertyValueString(entry));
            }
            state.getTags().forEach(t -> this.systemInfo.add("#" + t.location()));
        }
        if (this.liquid.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos1 = ((BlockHitResult) this.liquid).getBlockPos();
            FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
            this.systemInfo.add("");
            this.systemInfo.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
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
            entity.getType().getTags().forEach(t -> this.systemInfo.add("#" + t.location()));
        }
        return this.systemInfo;
    }
}
