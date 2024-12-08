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
import org.jocl.*;
import org.lwjgl.opengl.GL11C;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.renderer.DimensionOverworld;

import java.nio.IntBuffer;

public class LightingTexture extends LightTexture {

    private static final int SKY_FLASH = 16;
    private static final int SKY_BRIGHTNESS = 17;
    private static final int SKY_RED = 18;
    private static final int SKY_GREEN = 19;
    private static final int SKY_BLUE = 20;
    private static final int DARKEN_AMOUNT = 21;
    private static final int NIGHT_VISION_MOD = 22;
    private static final int GAMMA = 23;
    private static final String CL_SOURCE = """
                        inline float invGamma(float value) {
                        	float f = 1.0F - value;
                        	return 1.0F - f * f * f;
                        }
                        			
                        kernel void computeLightmap(global const float* data, global int* lightmap) {
                        	int x = get_global_id(0);
                        	int y = get_global_id(1);
                        	int bl = get_global_id(2);
                        	float skyFlash = data[16];
                        	float skyBrightness = data[17];
                        	float skyRed = data[18];
                        	float skyGreen = data[19];
                        	float skyBlue = data[20];
                        	float darkenAmount = data[21];
                        	float nightVisionMod = data[22];
                        	float gamma = data[23];
                        	for (int sl = 0; sl < 16; ++sl) {
                        		float skyLight = data[sl] * skyFlash;
                        		float bLRed = bl < 16 ? data[bl] * 0.5f : data[bl - 16];
                        		float bLGreen = x < 16 ? data[x] * 0.5f : data[x - 16];
                        		float bLBlue = y < 16 ? data[y] * 0.5f : data[y - 16];
                        		float addend = skyBrightness * skyLight;
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
    private final float[] data = new float[24];
    private cl_mem dataMem;
    private Pointer dataPointerDevice;
    private final Pointer dataPointerHost = Pointer.to(this.data);
    private final GameRenderer gameRenderer;
    private final long[] globalWorkSize = new long[3];
    private final Pointer imagePointer;
    private cl_kernel kernel;
    private final DynamicTexture lightTexture;
    private final ResourceLocation lightTextureLocation;
    private final Minecraft mc;
    private cl_mem memOut;
    private Pointer memOutPointer;
    private boolean needsUpdate;
    private float[] oldTable;
    private cl_program program;

    public LightingTexture(GameRenderer gameRenderer, Minecraft mc) {
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
        CL.clReleaseMemObject(this.dataMem);
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
        this.dataMem = CL.clCreateBuffer(this.context, CL.CL_MEM_READ_ONLY | CL.CL_MEM_USE_HOST_PTR, Sizeof.cl_float * 24, this.dataPointerHost, null);
        this.dataPointerDevice = Pointer.to(this.dataMem);
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
                    System.arraycopy(table, 0, this.data, 0, table.length);
                }
                float skyBrightness = getSunBrightness(level, partialTicks);
                if (level.getSkyFlashTime() > 0) {
                    this.data[SKY_FLASH] = 1.0F;
                }
                else {
                    this.data[SKY_FLASH] = skyBrightness;
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
                this.data[NIGHT_VISION_MOD] = nightVisionMod;
                this.data[SKY_BRIGHTNESS] = skyBrightness * 0.65f + 0.35f;
                this.data[DARKEN_AMOUNT] = this.gameRenderer.getDarkenWorldAmount(partialTicks);
                this.data[GAMMA] = (float) this.mc.options.gamma;
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
                this.data[SKY_RED] = r;
                this.data[SKY_GREEN] = g;
                this.data[SKY_BLUE] = b;
                CL.clEnqueueWriteBuffer(this.commandQueue, this.dataMem, true, 0, 24 * Sizeof.cl_float, this.dataPointerHost, 0, null, null);
                CL.clSetKernelArg(this.kernel, 0, Sizeof.cl_mem, this.dataPointerDevice);
                CL.clSetKernelArg(this.kernel, 1, Sizeof.cl_mem, this.memOutPointer);
                CL.clEnqueueNDRangeKernel(this.commandQueue, this.kernel, 3, null, this.globalWorkSize, null, 0, null, null);
                // Read the output data
                CL.clEnqueueReadBuffer(this.commandQueue, this.memOut, true, 0, 1_024 * 512 * Sizeof.cl_int, this.imagePointer, 0, null, null);
                this.lightTexture.upload();
            }
            profiler.pop();
        }
    }
}
