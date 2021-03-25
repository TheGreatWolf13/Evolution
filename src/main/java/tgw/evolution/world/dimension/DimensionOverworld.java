package tgw.evolution.world.dimension;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.MoonPhase;
import tgw.evolution.util.Vec3f;

import javax.annotation.Nullable;

public class DimensionOverworld extends Dimension {

    private static final float[] SUNSET_COLORS = new float[4];
    private MoonPhase eclipsePhase = MoonPhase.FULL_MOON;
    private Vec3d fogColor = Vec3d.ZERO;
    private boolean isInLunarEclipse;
    private boolean isInSolarEclipse;
    private float latitude;
    private float lunarEclipseAmplitude;
    private float lunarEclipseAngle;
    private float moonAngle;
    private float moonCelestialRadius;
    private float moonElevationAngle;
    private float moonMonthlyOffset;
    private MoonPhase moonPhase = MoonPhase.NEW_MOON;
    private float solarEclipseAmplitude;
    private float solarEclipseAngle;
    private float sunAngle;
    private float sunCelestialRadius;
    private float sunElevationAngle;
    private float sunSeasonalOffset;
    private float[] sunsetColors;

    public DimensionOverworld(World world, DimensionType type) {
        super(world, type);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        if (celestialAngle != 0 || partialTicks != 0) {
            return null;
        }
        return this.sunsetColors;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return this.sunAngle;
    }

    private Vec3d calculateFogColor() {
        float sunAngle = 1.0f;
        if (this.sunElevationAngle > 80) {
            sunAngle = -this.sunElevationAngle * this.sunElevationAngle / 784.0f + 10.0f * this.sunElevationAngle / 49.0f - 7.163_265f;
            sunAngle = MathHelper.clamp(sunAngle, 0.0F, 1.0F);
        }
        if (this.isInSolarEclipse) {
            float intensity = 1.0F - this.getSolarEclipseIntensity();
            if (intensity < sunAngle) {
                sunAngle = intensity;
            }
        }
        float r = 0.752_941_2F;
        r *= sunAngle;
        float g = 0.847_058_83F;
        g *= sunAngle;
        float b = 1.0F;
        b *= sunAngle;
        return new Vec3d(r, g, b);
    }

    public float calculateMoonAngle() {
        return this.moonAngle;
    }

    @Override
    public boolean canRespawnHere() {
        return true;
    }

    @Override
    public ChunkGenerator<? extends GenerationSettings> createChunkGenerator() {
        WorldType worldType = this.world.getWorldInfo().getGenerator();
        if (worldType == WorldType.FLAT) {
            FlatGenerationSettings settings = FlatGenerationSettings.createFlatGenerator(new Dynamic<>(NBTDynamicOps.INSTANCE,
                                                                                                       this.world.getWorldInfo()
                                                                                                                 .getGeneratorOptions()));
            SingleBiomeProviderSettings biomeProviderSettings = BiomeProviderType.FIXED.createSettings().setBiome(settings.getBiome());
            return ChunkGeneratorType.FLAT.create(this.world, BiomeProviderType.FIXED.create(biomeProviderSettings), settings);
        }
        if (worldType == WorldType.DEBUG_ALL_BLOCK_STATES) {
            SingleBiomeProviderSettings biomeProviderSettings = BiomeProviderType.FIXED.createSettings().setBiome(Biomes.PLAINS);
            return ChunkGeneratorType.DEBUG.create(this.world,
                                                   BiomeProviderType.FIXED.create(biomeProviderSettings),
                                                   ChunkGeneratorType.DEBUG.createSettings());
        }
        if (worldType != WorldType.BUFFET) {
            OverworldGenSettings settings = ChunkGeneratorType.SURFACE.createSettings();
            OverworldBiomeProviderSettings biomeProviderSettings = BiomeProviderType.VANILLA_LAYERED.createSettings()
                                                                                                    .setWorldInfo(this.world.getWorldInfo())
                                                                                                    .setGeneratorSettings(settings);
            return ChunkGeneratorType.SURFACE.create(this.world, BiomeProviderType.VANILLA_LAYERED.create(biomeProviderSettings), settings);
        }
        BiomeProvider biomeProvider = null;
        JsonObject generatorOptions = Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, this.world.getWorldInfo().getGeneratorOptions())
                                             .getAsJsonObject();
        JsonObject biomeSource = generatorOptions.getAsJsonObject("biome_source");
        if (biomeSource != null && biomeSource.has("type") && biomeSource.has("options")) {
            BiomeProviderType<?, ?> biomeProviderType = Registry.BIOME_SOURCE_TYPE.getOrDefault(new ResourceLocation(biomeSource.getAsJsonPrimitive(
                    "type").getAsString()));
            JsonObject options = biomeSource.getAsJsonObject("options");
            Biome[] biomeArray = {Biomes.OCEAN};
            if (options.has("biomes")) {
                JsonArray biomes = options.getAsJsonArray("biomes");
                if (biomes.size() > 0) {
                    biomeArray = new Biome[biomes.size()];
                    for (int i = 0; i < biomes.size(); i++) {
                        //noinspection ObjectAllocationInLoop
                        biomeArray[i] = Registry.BIOME.getValue(new ResourceLocation(biomes.get(i).getAsString())).orElse(Biomes.OCEAN);
                    }
                }
            }
            if (BiomeProviderType.FIXED == biomeProviderType) {
                biomeProvider = BiomeProviderType.FIXED.create(BiomeProviderType.FIXED.createSettings().setBiome(biomeArray[0]));
            }
            else if (BiomeProviderType.CHECKERBOARD == biomeProviderType) {
                int size = options.has("size") ? options.getAsJsonPrimitive("size").getAsInt() : 2;
                CheckerboardBiomeProviderSettings biomeProviderSettings = BiomeProviderType.CHECKERBOARD.createSettings()
                                                                                                        .setBiomes(biomeArray)
                                                                                                        .setSize(size);
                biomeProvider = BiomeProviderType.CHECKERBOARD.create(biomeProviderSettings);
            }
            else if (BiomeProviderType.VANILLA_LAYERED == biomeProviderType) {
                OverworldBiomeProviderSettings biomeProviderSettings = BiomeProviderType.VANILLA_LAYERED.createSettings()
                                                                                                        .setGeneratorSettings(new OverworldGenSettings())
                                                                                                        .setWorldInfo(this.world.getWorldInfo());
                biomeProvider = BiomeProviderType.VANILLA_LAYERED.create(biomeProviderSettings);
            }
        }
        if (biomeProvider == null) {
            biomeProvider = BiomeProviderType.FIXED.create(BiomeProviderType.FIXED.createSettings().setBiome(Biomes.OCEAN));
        }
        BlockState primaryState = Blocks.STONE.getDefaultState();
        BlockState fluidState = Blocks.WATER.getDefaultState();
        JsonObject chunkGenerator = generatorOptions.getAsJsonObject("chunk_generator");
        if (chunkGenerator != null && chunkGenerator.has("options")) {
            JsonObject options = chunkGenerator.getAsJsonObject("options");
            if (options.has("default_block")) {
                String defaultBlock = options.getAsJsonPrimitive("default_block").getAsString();
                primaryState = Registry.BLOCK.getOrDefault(new ResourceLocation(defaultBlock)).getDefaultState();
            }
            if (options.has("default_fluid")) {
                String defaultFluid = options.getAsJsonPrimitive("default_fluid").getAsString();
                fluidState = Registry.BLOCK.getOrDefault(new ResourceLocation(defaultFluid)).getDefaultState();
            }
        }
        if (chunkGenerator != null && chunkGenerator.has("type")) {
            ChunkGeneratorType<?, ?> chunkGeneratorType =
                    Registry.CHUNK_GENERATOR_TYPE.getOrDefault(new ResourceLocation(chunkGenerator.getAsJsonPrimitive(
                    "type").getAsString()));
            if (ChunkGeneratorType.CAVES == chunkGeneratorType) {
                NetherGenSettings settings = ChunkGeneratorType.CAVES.createSettings();
                settings.setDefaultBlock(primaryState);
                settings.setDefaultFluid(fluidState);
                return ChunkGeneratorType.CAVES.create(this.world, biomeProvider, settings);
            }
            if (ChunkGeneratorType.FLOATING_ISLANDS == chunkGeneratorType) {
                EndGenerationSettings settings = ChunkGeneratorType.FLOATING_ISLANDS.createSettings();
                settings.setSpawnPos(new BlockPos(0, 64, 0));
                settings.setDefaultBlock(primaryState);
                settings.setDefaultFluid(fluidState);
                return ChunkGeneratorType.FLOATING_ISLANDS.create(this.world, biomeProvider, settings);
            }
        }
        OverworldGenSettings settings = ChunkGeneratorType.SURFACE.createSettings();
        settings.setDefaultBlock(primaryState);
        settings.setDefaultFluid(fluidState);
        return ChunkGeneratorType.SURFACE.create(this.world, biomeProvider, settings);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    @Nullable
    @Override
    public BlockPos findSpawn(ChunkPos chunkPos, boolean checkValid) {
        int chunkPosEndX = chunkPos.getXEnd();
        int chunkPosEndZ = chunkPos.getZEnd();
        for (int i = chunkPos.getXStart(); i <= chunkPosEndX; ++i) {
            for (int j = chunkPos.getZStart(); j <= chunkPosEndZ; ++j) {
                BlockPos pos = this.findSpawn(i, j, checkValid);
                if (pos != null) {
                    return pos;
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(posX, 0, posZ);
        Biome biome = this.world.getBiome(mutablePos);
        BlockState biomeSurfaceState = biome.getSurfaceBuilderConfig().getTop();
        Chunk chunk = this.world.getChunk(posX >> 4, posZ >> 4);
        int i = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING, posX & 15, posZ & 15);
        if (i < 0) {
            return null;
        }
        if (chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, posX & 15, posZ & 15) >
            chunk.getTopBlockY(Heightmap.Type.OCEAN_FLOOR, posX & 15, posZ & 15)) {
            return null;
        }
        for (int j = i + 1; j >= 0; --j) {
            mutablePos.setPos(posX, j, posZ);
            BlockState stateAtWorld = this.world.getBlockState(mutablePos);
            if (!stateAtWorld.getFluidState().isEmpty()) {
                break;
            }
            if (BlockUtils.compareVanillaBlockStates(biomeSurfaceState, stateAtWorld)) {
                return mutablePos.up().toImmutable();
            }
        }
        return null;
    }

    @Override
    protected void generateLightBrightnessTable() {
        for (int lightLevel = 0; lightLevel <= 15; ++lightLevel) {
            float f1 = 1.0F - lightLevel / 15.0F;
            this.lightBrightnessTable[lightLevel] = (1.0F - f1) / (f1 * 3.0F + 1.0F);
        }
    }

    public MoonPhase getEclipsePhase() {
        return this.eclipsePhase;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return this.fogColor;
    }

    @OnlyIn(Dist.CLIENT)
    public float getLatitude() {
        return this.latitude;
    }

    public int getLunarEclipseAmplitudeIndex() {
        return Math.round(this.lunarEclipseAmplitude);
    }

    public int getLunarEclipseAngleIndex() {
        return Math.round(this.lunarEclipseAngle);
    }

    public float getLunarEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.lunarEclipseAngle);
        float amplitudeMod = 9.0F - Math.abs(this.lunarEclipseAmplitude);
        return angleMod * amplitudeMod / 81.0F;
    }

    public float getMoonCelestialRadius() {
        return this.moonCelestialRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public float getMoonElevationAngle() {
        return this.moonElevationAngle;
    }

    public float getMoonMonthlyOffset() {
        return this.moonMonthlyOffset;
    }

    public MoonPhase getMoonPhase() {
        return this.moonPhase;
    }

    @Override
    public Vec3d getSkyColor(BlockPos pos, float partialTick) {
        Vec3f skyColor = EarthHelper.getSkyColor(this.world, pos, partialTick, this);
        return new Vec3d(skyColor.x, skyColor.y, skyColor.z);
    }

    public int getSolarEclipseAmplitudeIndex() {
        return Math.round(this.solarEclipseAmplitude);
    }

    public int getSolarEclipseAngleIndex() {
        return Math.round(this.solarEclipseAngle);
    }

    public float getSolarEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.solarEclipseAngle);
        float amplitudeMod = 9.0F - Math.abs(this.solarEclipseAmplitude);
        return angleMod * amplitudeMod / 81.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getStarBrightness(float partialTicks) {
        float f1 = 1.0F - (MathHelper.cosDeg(this.sunElevationAngle) * 2.0F + 0.25F);
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    @Override
    public float getSunBrightness(float partialTicks) {
        if (!this.world.isRemote) {
            return this.world.getSunBrightness(partialTicks);
        }
        float moonlightMult = this.moonlightMult();
        float moonlightMin = 1 - moonlightMult;
        return this.getSunBrightnessPure(partialTicks) * moonlightMult + moonlightMin;
    }

    public float getSunBrightnessPure(float partialTicks) {
        float skyBrightness = 1.0F - (MathHelper.cosDeg(this.sunElevationAngle) * 2.0F + 0.62F);
        skyBrightness = MathHelper.clamp(skyBrightness, 0.0F, 1.0F);
        if (this.isInSolarEclipse) {
            float intensity = MathHelper.clampMax(this.getSolarEclipseIntensity(), 0.9F);
            if (skyBrightness < intensity) {
                skyBrightness = intensity;
            }
        }
        skyBrightness = 1.0F - skyBrightness;
        skyBrightness *= 1.0f - this.world.getRainStrength(partialTicks) * 0.312_5f;
        skyBrightness *= 1.0f - this.world.getThunderStrength(partialTicks) * 0.312_5f;
        return skyBrightness;
    }

    public float getSunCelestialRadius() {
        return this.sunCelestialRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSunElevationAngle() {
        return this.sunElevationAngle;
    }

    public float getSunSeasonalOffset() {
        return this.sunSeasonalOffset;
    }

    public boolean isInLunarEclipse() {
        return this.isInLunarEclipse;
    }

    public boolean isInSolarEclipse() {
        return this.isInSolarEclipse;
    }

    @Override
    public boolean isSurfaceWorld() {
        return true;
    }

    public float moonlightMult() {
        if (this.moonElevationAngle > 96) {
            return 0.97F;
        }
        float moonLight = this.isInLunarEclipse ? (1.0f - this.getLunarEclipseIntensity()) * 0.27f + 0.03f : this.moonPhase.getMoonLight();
        if (this.moonElevationAngle < 90) {
            return 1.0f - moonLight;
        }
        float mult = 1.0f - (this.moonElevationAngle - 90) / 5.0f;
        return 0.97f - (moonLight - 0.03f) * mult;
    }

    @Nullable
    private float[] sunsetColors() {
        if (this.sunElevationAngle >= 66.0F && this.sunElevationAngle <= 107.5F) {
            float cosElevation = MathHelper.cosDeg(this.sunElevationAngle);
            float mult = this.sunElevationAngle > 90 ? 1.5f : 1.1f;
            float f3 = cosElevation * mult + 0.5F;
            float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * MathHelper.PI)) * 0.99F;
            f4 *= f4;
            SUNSET_COLORS[0] = f3 * 0.3F + 0.7F;
            SUNSET_COLORS[1] = f3 * f3 * 0.7F + 0.2F;
            SUNSET_COLORS[2] = 0.2F;
            SUNSET_COLORS[3] = f4;
            return SUNSET_COLORS;
        }
        return null;
    }

    @Override
    public void tick() {
        this.sunAngle = EarthHelper.calculateSunAngle(this.world.getDayTime());
        if (this.world.isRemote) {
            this.moonAngle = EarthHelper.calculateMoonAngle(this.world.getDayTime());
            float seasonAngle = EarthHelper.sunSeasonalInclination(this.world.getDayTime());
            float monthlyAngle = EarthHelper.lunarMonthlyAmpl(this.world.getDayTime());
            float eclipseAngle = MathHelper.wrapDegrees(360 * (this.sunAngle - this.moonAngle));
            this.isInSolarEclipse = false;
            this.isInLunarEclipse = false;
            if (Math.abs(eclipseAngle) <= 3.0f) {
                float eclipseAmplitude = MathHelper.wrapDegrees(seasonAngle - monthlyAngle);
                if (Math.abs(eclipseAmplitude) <= 7.0f) {
                    this.isInSolarEclipse = true;
                    this.solarEclipseAngle = EarthHelper.getEclipseAmount(eclipseAngle * 7.0f / 3.0f);
                    this.solarEclipseAmplitude = EarthHelper.getEclipseAmount(eclipseAmplitude);
                }
            }
            else if (177.0f <= Math.abs(eclipseAngle) || Math.abs(eclipseAngle) <= -177.0f) {
                float eclipseAmplitude = MathHelper.wrapDegrees(seasonAngle - monthlyAngle);
                if (Math.abs(eclipseAmplitude) <= 14.0f) {
                    if (eclipseAngle > 0) {
                        eclipseAngle -= 180;
                    }
                    else {
                        eclipseAngle += 180;
                    }
                    eclipseAngle = -eclipseAngle;
                    this.isInLunarEclipse = true;
                    this.lunarEclipseAngle = EarthHelper.getEclipseAmount(Math.signum(eclipseAngle) * eclipseAngle * eclipseAngle * 7.0f / 9.0f);
                    this.lunarEclipseAmplitude = EarthHelper.getEclipseAmount(eclipseAmplitude * eclipseAmplitude * eclipseAmplitude / 392.0f);
                    this.eclipsePhase = EarthHelper.phaseByEclipseIntensity(this.getLunarEclipseAngleIndex(), this.getLunarEclipseAmplitudeIndex());
                }
            }
            this.latitude = EarthHelper.calculateLatitude(Evolution.PROXY.getClientPlayer().posZ);
            float sinLatitude = MathHelper.sinDeg(this.latitude);
            float cosLatitude = MathHelper.cosDeg(this.latitude);
            this.sunCelestialRadius = 100.0f * MathHelper.cosDeg(seasonAngle);
            this.moonCelestialRadius = 100.0f * MathHelper.cosDeg(monthlyAngle);
            this.sunSeasonalOffset = -100.0f * MathHelper.sinDeg(seasonAngle);
            this.moonMonthlyOffset = -100.0f * MathHelper.sinDeg(monthlyAngle);
            this.sunElevationAngle = EarthHelper.getSunElevation(sinLatitude,
                                                                 cosLatitude,
                                                                 this.sunAngle * 360,
                                                                 this.sunCelestialRadius,
                                                                 this.sunSeasonalOffset);
            this.moonElevationAngle = EarthHelper.getMoonElevation(sinLatitude,
                                                                   cosLatitude,
                                                                   this.moonAngle * 360,
                                                                   this.moonCelestialRadius,
                                                                   this.moonMonthlyOffset);
            this.fogColor = this.calculateFogColor();
            this.sunsetColors = this.sunsetColors();
            this.moonPhase = MoonPhase.byAngles(this.sunAngle * 360, this.moonAngle * 360);
        }
    }
}
