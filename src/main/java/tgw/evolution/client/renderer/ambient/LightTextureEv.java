package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;
import org.jocl.*;
import org.lwjgl.opengl.GL11C;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.renderer.DimensionOverworld;

import java.nio.IntBuffer;

public class LightTextureEv extends LightTexture {

    private static final String CL_SOURCE = """
                        inline float invGamma(float value) {
                        	float f = 1.0F - value;
                        	return 1.0F - f * f * f;
                        }
                        			
                        kernel void computeLightmap(global const float* table, float skyFlash, float corrSkyBrightness, float skyRed, float skyGreen, float skyBlue, float darkenAmount, float nightVisionMod, float gamma, global int* lightmap) {
                        	int x = get_global_id(0);
                        	int y = get_global_id(1);
                        	int bl = get_global_id(2);
                        	for (int sl = 0; sl < 16; ++sl) {
                        		float skyLight = table[sl] * skyFlash;
                        		float bLRed = bl < 16 ? table[bl] * 0.5f : table[bl - 16];
                        		float bLGreen = x < 16 ? table[x] * 0.5f : table[x - 16];
                        		float bLBlue = y < 16 ? table[y] * 0.5f : table[y - 16];
                        		float addend = corrSkyBrightness * skyLight;
                        		float redMod = addend * skyRed;
                        		float greenMod = addend * skyGreen;
                        		float blueMod = addend * skyBlue;
                        		bLRed *= 1 - redMod;
                        		bLGreen *= 1 - greenMod;
                        		bLBlue *= 1 - blueMod;
                        		bLRed += redMod;
                        		bLGreen += greenMod;
                        		bLBlue += blueMod;
                        		if (darkenAmount > 0.0F) {
                        			float r = bLRed * 0.7f * darkenAmount;
                        			float g = bLGreen * 0.6f * darkenAmount;
                        			float b = bLBlue * 0.6f * darkenAmount;
                        			float f = 1.0f - darkenAmount;
                        			bLRed *= f;
                        			bLGreen *= f;
                        			bLBlue *= f;
                        			bLRed += r;
                        			bLGreen += g;
                        			bLBlue += b;
                        		}
                        		if (nightVisionMod > 0.0F) {
                        			float max = fmax(bLRed, fmax(bLGreen, bLBlue));
                        			if (max < 1.0F) {
                        				float r = bLRed;
                        				float g = bLGreen;
                        				float b = bLBlue;
                        				float f = 1.0F / max;
                        				if (isinf(f) == 1) {
                        					r = 1.0f;
                        					g = 1.0f;
                        					b = 1.0f;
                        				}
                        				else {
                        					r *= f;
                        					g *= f;
                        					b *= f;
                        				}
                        				float m = 1 - nightVisionMod;
                        				bLRed *= m;
                        				bLGreen *= m;
                        				bLBlue *= m;
                        				bLRed += r * nightVisionMod;
                        				bLGreen += g * nightVisionMod;
                        				bLBlue += b * nightVisionMod;
                        			}
                        		}
                        		float max = fmax(bLRed, fmax(bLGreen, bLBlue));
                        		float gammaMult = max == 0 ? 0 : (max * (1.0f - gamma) + invGamma(max) * gamma) / max;
                        		bLRed *= gammaMult;
                        		bLGreen *= gammaMult;
                        		bLBlue *= gammaMult;
                        		bLRed *= 255.0f;
                        		bLGreen *= 255.0f;
                        		bLBlue *= 255.0f;
                        		int red = (int) bLRed;
                        		int green = (int) bLGreen;
                        		int blue = (int) bLBlue;
                        		lightmap[(x * 32 + bl) + 1024 * (y * 16 + sl)] = 0xff000000 | blue << 16 | green << 8 | red;
                        	}
                        }
            """;
    private cl_command_queue commandQueue;
    private cl_context context;
    private final float[] corrSkyBrightness = new float[1];
    private final Pointer corrSkyBrightnessPointer = Pointer.to(this.corrSkyBrightness);
    private final float[] darkenAmount = new float[1];
    private final Pointer darkenAmountPointer = Pointer.to(this.darkenAmount);
    private final GameRenderer gameRenderer;
    private final float[] gamma = new float[1];
    private final Pointer gammaPointer = Pointer.to(this.gamma);
    private final long[] globalWorkSize = new long[3];
    private final Pointer imagePointer;
    private cl_kernel kernel;
    private final DynamicTexture lightTexture;
    private final ResourceLocation lightTextureLocation;
    private final Minecraft mc;
    private cl_mem memOut;
    private Pointer memOutPointer;
    private @Nullable cl_mem memTable;
    private boolean needsUpdate;
    private final float[] nightVisionMod = new float[1];
    private final Pointer nightVisionPointer = Pointer.to(this.nightVisionMod);
    private float[] oldTable;
    private cl_program program;
    private final float[] skyBlue = new float[1];
    private final Pointer skyBluePointer = Pointer.to(this.skyBlue);
    private final float[] skyFlash = new float[1];
    private final Pointer skyFlashPointer = Pointer.to(this.skyFlash);
    private final float[] skyGreen = new float[1];
    private final Pointer skyGreenPointer = Pointer.to(this.skyGreen);
    private final float[] skyRed = new float[1];
    private final Pointer skyRedPointer = Pointer.to(this.skyRed);
    private Pointer tablePointer;

    public LightTextureEv(GameRenderer gameRenderer, Minecraft mc) {
        super(gameRenderer, mc);
        this.gameRenderer = gameRenderer;
        this.mc = mc;
        this.lightTexture = new DynamicTexture(1_024, 512, true);
        this.lightTextureLocation = this.mc.getTextureManager().register("light_map", this.lightTexture);
        NativeImage pixels = this.lightTexture.getPixels();
        assert pixels != null;
        for (int i = 0; i < 512; ++i) {
            for (int j = 0; j < 1_024; ++j) {
                pixels.setPixelRGBA(j, i, 0xffff_ffff);
            }
        }
        this.lightTexture.upload();
        this.initCL();
        IntBuffer buffer = pixels.getBuffer();
        if (buffer == null) {
            throw new RuntimeException("Null pointer for image!");
        }
        this.imagePointer = Pointer.to(buffer);
    }

    public static float getLightBrightness(LevelReader level, int lightLevel) {
        if (level.dimensionType().natural()) {
            DimensionOverworld dimension = EvolutionClient.getDimension();
            if (dimension != null) {
                return dimension.getAmbientLight(lightLevel);
            }
        }
        return level.dimensionType().brightness(lightLevel);
    }

    private static float[] getLightBrightnessTable(LevelReader level) {
        if (level.dimensionType().natural()) {
            DimensionOverworld dimension = EvolutionClient.getDimension();
            if (dimension != null) {
                return dimension.getLightBrightnessTable();
            }
        }
        return level.dimensionType().brightnessRamp;
    }

    private static float getSunBrightness(ClientLevel world, float partialTicks) {
        if (world.dimensionType().natural()) {
            assert EvolutionClient.getDimension() != null;
            return EvolutionClient.getDimension().getSkyBrightness(partialTicks);
        }
        return world.getSkyDarken(partialTicks);
    }

    @Override
    public void close() {
        if (this.memTable != null) {
            CL.clReleaseMemObject(this.memTable);
        }
        CL.clReleaseMemObject(this.memOut);
        CL.clReleaseKernel(this.kernel);
        CL.clReleaseProgram(this.program);
        CL.clReleaseCommandQueue(this.commandQueue);
        CL.clReleaseContext(this.context);
        this.lightTexture.close();
    }

    private void initCL() {
        final int platformIndex = 0;
        final long deviceType = CL.CL_DEVICE_TYPE_GPU;
        final int deviceIndex = 0;
        CL.setExceptionsEnabled(true);
        int[] numPlatformsArray = new int[1];
        CL.clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        CL.clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);
        int[] numDevicesArray = new int[1];
        CL.clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        cl_device_id[] devices = new cl_device_id[numDevices];
        CL.clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];
        this.context = CL.clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);
        cl_queue_properties properties = new cl_queue_properties();
        properties.addProperty(CL.CL_QUEUE_PROFILING_ENABLE, 1);
        properties.addProperty(CL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE, 1);
        this.commandQueue = CL.clCreateCommandQueueWithProperties(this.context, device, properties, null);
        this.program = CL.clCreateProgramWithSource(this.context, 1, new String[]{CL_SOURCE}, null, null);
        CL.clBuildProgram(this.program, 0, null, null, null, null);
        this.kernel = CL.clCreateKernel(this.program, "computeLightmap", null);
        this.globalWorkSize[0] = 32;
        this.globalWorkSize[1] = 32;
        this.globalWorkSize[2] = 32;
        this.memOut = CL.clCreateBuffer(this.context, CL.CL_MEM_WRITE_ONLY, Sizeof.cl_int * 1_024 * 512, null, null);
        this.memOutPointer = Pointer.to(this.memOut);
    }

    @Override
    public void tick() {
        this.needsUpdate = true;
    }

    @Override
    public void turnOnLightLayer() {
        RenderSystem.setShaderTexture(2, this.lightTextureLocation);
        this.mc.getTextureManager().bindForSetup(this.lightTextureLocation);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, 0x2601);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, 0x2601);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void updateLightTexture(float partialTicks) {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            ProfilerFiller profiler = this.mc.getProfiler();
            profiler.push("lightTex");
            ClientLevel level = this.mc.level;
            if (level != null) {
                float[] table = getLightBrightnessTable(level);
                //noinspection ArrayEquality
                if (table != this.oldTable) {
                    this.oldTable = table;
                    if (this.memTable != null) {
                        CL.clReleaseMemObject(this.memTable);
                    }
                    this.memTable = CL.clCreateBuffer(this.context, CL.CL_MEM_READ_ONLY | CL.CL_MEM_USE_HOST_PTR, Sizeof.cl_float * 16, Pointer.to(table), null);
                    this.tablePointer = Pointer.to(this.memTable);
                }
                float skyBrightness = getSunBrightness(level, partialTicks);
                if (level.getSkyFlashTime() > 0) {
                    this.skyFlash[0] = 1.0F;
                }
                else {
                    this.skyFlash[0] = skyBrightness;
                }
                assert this.mc.player != null;
                float waterBrightness = this.mc.player.getWaterVision();
                float nightVisionMod;
                if (this.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
                    nightVisionMod = GameRenderer.getNightVisionScale(this.mc.player, partialTicks);
                }
                else if (waterBrightness > 0.0F && this.mc.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                    nightVisionMod = waterBrightness;
                }
                else {
                    nightVisionMod = 0.0F;
                }
                this.nightVisionMod[0] = nightVisionMod;
                this.corrSkyBrightness[0] = skyBrightness * 0.65f + 0.35f;
                this.darkenAmount[0] = this.gameRenderer.getDarkenWorldAmount(partialTicks);
                this.gamma[0] = (float) this.mc.options.gamma;
                float r = 1.0f;
                float g = 1.0f;
                float b = 1.0f;
                if (EvolutionClient.isInitialized()) {
                    DimensionOverworld dimension = EvolutionClient.getDimension();
                    assert dimension != null;
                    float[] duskDawnColors = dimension.getDuskDawnColors();
                    if (duskDawnColors != null) {
                        float alpha = duskDawnColors[3];
                        alpha *= alpha;
                        if (alpha > 0.5f) {
                            alpha = 0.5f;
                        }
                        float antiAlpha = 1 - alpha;
                        r *= antiAlpha;
                        g *= antiAlpha;
                        b *= antiAlpha;
                        r += alpha * duskDawnColors[0];
                        g += alpha * duskDawnColors[1];
                        b += alpha * duskDawnColors[2];
                    }
                }
                this.skyRed[0] = r;
                this.skyGreen[0] = g;
                this.skyBlue[0] = b;
                cl_kernel kernel = this.kernel;
                CL.clSetKernelArg(kernel, 0, Sizeof.cl_mem, this.tablePointer);
                CL.clSetKernelArg(kernel, 1, Sizeof.cl_float, this.skyFlashPointer);
                CL.clSetKernelArg(kernel, 2, Sizeof.cl_float, this.corrSkyBrightnessPointer);
                CL.clSetKernelArg(kernel, 3, Sizeof.cl_float, this.skyRedPointer);
                CL.clSetKernelArg(kernel, 4, Sizeof.cl_float, this.skyGreenPointer);
                CL.clSetKernelArg(kernel, 5, Sizeof.cl_float, this.skyBluePointer);
                CL.clSetKernelArg(kernel, 6, Sizeof.cl_float, this.darkenAmountPointer);
                CL.clSetKernelArg(kernel, 7, Sizeof.cl_float, this.nightVisionPointer);
                CL.clSetKernelArg(kernel, 8, Sizeof.cl_float, this.gammaPointer);
                CL.clSetKernelArg(kernel, 9, Sizeof.cl_mem, this.memOutPointer);
                CL.clEnqueueNDRangeKernel(this.commandQueue, kernel, 3, null, this.globalWorkSize, null, 0, null, null);
                // Read the output data
                CL.clEnqueueReadBuffer(this.commandQueue, this.memOut, CL.CL_TRUE, 0, 1_024 * 512 * Sizeof.cl_int, this.imagePointer, 0, null, null);
                this.lightTexture.upload();
            }
            profiler.pop();
        }
    }
}
