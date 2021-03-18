package tgw.evolution.world.dimension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    private Vec3d fogColor = Vec3d.ZERO;
    private boolean isInSolarEclipse;
    private float latitude;
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
            float intensity = 1.0F - this.getEclipseIntensity();
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
        WorldType worldtype = this.world.getWorldInfo().getGenerator();
        ChunkGeneratorType<FlatGenerationSettings, FlatChunkGenerator> chunkgeneratortype = ChunkGeneratorType.FLAT;
        ChunkGeneratorType<DebugGenerationSettings, DebugChunkGenerator> chunkgeneratortype1 = ChunkGeneratorType.DEBUG;
        ChunkGeneratorType<NetherGenSettings, NetherChunkGenerator> chunkgeneratortype2 = ChunkGeneratorType.CAVES;
        ChunkGeneratorType<EndGenerationSettings, EndChunkGenerator> chunkgeneratortype3 = ChunkGeneratorType.FLOATING_ISLANDS;
        ChunkGeneratorType<OverworldGenSettings, OverworldChunkGenerator> chunkgeneratortype4 = ChunkGeneratorType.SURFACE;
        BiomeProviderType<SingleBiomeProviderSettings, SingleBiomeProvider> biomeprovidertype = BiomeProviderType.FIXED;
        BiomeProviderType<OverworldBiomeProviderSettings, OverworldBiomeProvider> biomeprovidertype1 = BiomeProviderType.VANILLA_LAYERED;
        BiomeProviderType<CheckerboardBiomeProviderSettings, CheckerboardBiomeProvider> biomeprovidertype2 = BiomeProviderType.CHECKERBOARD;
        if (worldtype == WorldType.FLAT) {
            FlatGenerationSettings flatgenerationsettings = FlatGenerationSettings.createFlatGenerator(new Dynamic<>(NBTDynamicOps.INSTANCE,
                                                                                                                     this.world.getWorldInfo()
                                                                                                                               .getGeneratorOptions()));
            SingleBiomeProviderSettings singlebiomeprovidersettings1 = biomeprovidertype.createSettings().setBiome(flatgenerationsettings.getBiome());
            return chunkgeneratortype.create(this.world, biomeprovidertype.create(singlebiomeprovidersettings1), flatgenerationsettings);
        }
        if (worldtype == WorldType.DEBUG_ALL_BLOCK_STATES) {
            SingleBiomeProviderSettings singlebiomeprovidersettings = biomeprovidertype.createSettings().setBiome(Biomes.PLAINS);
            return chunkgeneratortype1.create(this.world,
                                              biomeprovidertype.create(singlebiomeprovidersettings),
                                              chunkgeneratortype1.createSettings());
        }
        if (worldtype != WorldType.BUFFET) {
            OverworldGenSettings overworldgensettings = chunkgeneratortype4.createSettings();
            OverworldBiomeProviderSettings overworldbiomeprovidersettings = biomeprovidertype1.createSettings()
                                                                                              .setWorldInfo(this.world.getWorldInfo())
                                                                                              .setGeneratorSettings(overworldgensettings);
            return chunkgeneratortype4.create(this.world, biomeprovidertype1.create(overworldbiomeprovidersettings), overworldgensettings);
        }
        BiomeProvider biomeprovider = null;
        JsonElement jsonelement = Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, this.world.getWorldInfo().getGeneratorOptions());
        JsonObject jsonobject = jsonelement.getAsJsonObject();
        JsonObject jsonobject1 = jsonobject.getAsJsonObject("biome_source");
        if (jsonobject1 != null && jsonobject1.has("type") && jsonobject1.has("options")) {
            BiomeProviderType<?, ?> biomeprovidertype3 = Registry.BIOME_SOURCE_TYPE.getOrDefault(new ResourceLocation(jsonobject1.getAsJsonPrimitive(
                    "type").getAsString()));
            JsonObject jsonobject2 = jsonobject1.getAsJsonObject("options");
            Biome[] abiome = {Biomes.OCEAN};
            if (jsonobject2.has("biomes")) {
                JsonArray jsonarray = jsonobject2.getAsJsonArray("biomes");
                abiome = jsonarray.size() > 0 ? new Biome[jsonarray.size()] : new Biome[]{Biomes.OCEAN};

                for (int i = 0; i < jsonarray.size(); ++i) {
                    //noinspection ObjectAllocationInLoop
                    abiome[i] = Registry.BIOME.getValue(new ResourceLocation(jsonarray.get(i).getAsString())).orElse(Biomes.OCEAN);
                }
            }
            if (BiomeProviderType.FIXED == biomeprovidertype3) {
                SingleBiomeProviderSettings singlebiomeprovidersettings2 = biomeprovidertype.createSettings().setBiome(abiome[0]);
                biomeprovider = biomeprovidertype.create(singlebiomeprovidersettings2);
            }
            if (BiomeProviderType.CHECKERBOARD == biomeprovidertype3) {
                int j = jsonobject2.has("size") ? jsonobject2.getAsJsonPrimitive("size").getAsInt() : 2;
                CheckerboardBiomeProviderSettings checkerboardbiomeprovidersettings = biomeprovidertype2.createSettings()
                                                                                                        .setBiomes(abiome)
                                                                                                        .setSize(j);
                biomeprovider = biomeprovidertype2.create(checkerboardbiomeprovidersettings);
            }
            if (BiomeProviderType.VANILLA_LAYERED == biomeprovidertype3) {
                OverworldBiomeProviderSettings overworldbiomeprovidersettings1 = biomeprovidertype1.createSettings()
                                                                                                   .setGeneratorSettings(new OverworldGenSettings())
                                                                                                   .setWorldInfo(this.world.getWorldInfo());
                biomeprovider = biomeprovidertype1.create(overworldbiomeprovidersettings1);
            }
        }
        if (biomeprovider == null) {
            biomeprovider = biomeprovidertype.create(biomeprovidertype.createSettings().setBiome(Biomes.OCEAN));
        }
        BlockState blockstate = Blocks.STONE.getDefaultState();
        BlockState blockstate1 = Blocks.WATER.getDefaultState();
        JsonObject jsonobject3 = jsonobject.getAsJsonObject("chunk_generator");
        if (jsonobject3 != null && jsonobject3.has("options")) {
            JsonObject jsonobject4 = jsonobject3.getAsJsonObject("options");
            if (jsonobject4.has("default_block")) {
                String s = jsonobject4.getAsJsonPrimitive("default_block").getAsString();
                blockstate = Registry.BLOCK.getOrDefault(new ResourceLocation(s)).getDefaultState();
            }
            if (jsonobject4.has("default_fluid")) {
                String s1 = jsonobject4.getAsJsonPrimitive("default_fluid").getAsString();
                blockstate1 = Registry.BLOCK.getOrDefault(new ResourceLocation(s1)).getDefaultState();
            }
        }
        if (jsonobject3 != null && jsonobject3.has("type")) {
            ChunkGeneratorType<?, ?> chunkgeneratortype5 =
                    Registry.CHUNK_GENERATOR_TYPE.getOrDefault(new ResourceLocation(jsonobject3.getAsJsonPrimitive(
                    "type").getAsString()));
            if (ChunkGeneratorType.CAVES == chunkgeneratortype5) {
                NetherGenSettings nethergensettings = chunkgeneratortype2.createSettings();
                nethergensettings.setDefaultBlock(blockstate);
                nethergensettings.setDefaultFluid(blockstate1);
                return chunkgeneratortype2.create(this.world, biomeprovider, nethergensettings);
            }
            if (ChunkGeneratorType.FLOATING_ISLANDS == chunkgeneratortype5) {
                EndGenerationSettings endgenerationsettings = chunkgeneratortype3.createSettings();
                endgenerationsettings.setSpawnPos(new BlockPos(0, 64, 0));
                endgenerationsettings.setDefaultBlock(blockstate);
                endgenerationsettings.setDefaultFluid(blockstate1);
                return chunkgeneratortype3.create(this.world, biomeprovider, endgenerationsettings);
            }
        }
        OverworldGenSettings overworldgensettings1 = chunkgeneratortype4.createSettings();
        overworldgensettings1.setDefaultBlock(blockstate);
        overworldgensettings1.setDefaultFluid(blockstate1);
        return chunkgeneratortype4.create(this.world, biomeprovider, overworldgensettings1);
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

    public float getEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.solarEclipseAngle);
        float amplitudeMod = 9.0F - Math.abs(this.solarEclipseAmplitude);
        return angleMod * amplitudeMod / 81.0F;
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
            float intensity = MathHelper.clampMax(this.getEclipseIntensity(), 0.9F);
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
        if (this.moonElevationAngle < 90) {
            return 1.0f - this.moonPhase.getMoonLight();
        }
        float mult = 1.0f - (this.moonElevationAngle - 90) / 5.0f;
        return 0.97f - (this.moonPhase.getMoonLight() - 0.03f) * mult;
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
            float eclipseAngle = 360 * (this.sunAngle - this.moonAngle);
            if (eclipseAngle > 300) {
                eclipseAngle = 360 - eclipseAngle;
            }
            else if (eclipseAngle < -300) {
                eclipseAngle += 360;
            }
            this.isInSolarEclipse = false;
            if (Math.abs(eclipseAngle) <= 7.0f) {
                float eclipseAmplitude = seasonAngle - monthlyAngle;
                if (eclipseAmplitude > 300) {
                    eclipseAmplitude = 360 - eclipseAmplitude;
                }
                else if (eclipseAmplitude < -300) {
                    eclipseAmplitude += 360;
                }
                if (Math.abs(eclipseAmplitude) <= 7.0f) {
                    this.isInSolarEclipse = true;
                    this.solarEclipseAngle = EarthHelper.getSolarEclipseAmount(eclipseAngle);
                    this.solarEclipseAmplitude = EarthHelper.getSolarEclipseAmount(eclipseAmplitude);
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
