package tgw.evolution.mixin;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.entities.util.IWrapCallback;
import tgw.evolution.patches.PatchCamera;
import tgw.evolution.patches.obj.NearPlane;
import tgw.evolution.util.math.ClipContextMutable;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.VectorUtil;

@Mixin(Camera.class)
public abstract class MixinCamera implements PatchCamera {

    @Shadow @Final private BlockPos.MutableBlockPos blockPosition;
    @Unique private final ClipContextMutable clipContext = new ClipContextMutable();
    @Shadow private boolean detached;
    @Shadow private @Nullable Entity entity;
    @Shadow @Final private Vector3f forwards;
    @Shadow private boolean initialized;
    @Unique private int lastAttachId = -1;
    @Shadow @Final private Vector3f left;
    @Shadow private BlockGetter level;
    @Unique private final NearPlane nearPlane = new NearPlane();
    @Shadow private Vec3 position = new Vec3d(Vec3.ZERO);
    @Unique private final Quaternion quatX = new Quaternion(Vector3f.XP, 0, true);
    @Unique private final Quaternion quatY = new Quaternion(Vector3f.YP, 0, true);
    @Shadow @Final private Quaternion rotation;
    @Shadow @Final private Vector3f up;
    @Shadow private float xRot;
    @Unique private byte xWrap;
    @Shadow private float yRot;
    @Unique private byte zWrap;
    @Unique private final IWrapCallback wrapCallback = new IWrapCallback() {

        @Override
        public void onX2NegativeWrap() {
            MixinCamera.this.xWrap = -1;
        }

        @Override
        public void onX2PositiveWrap() {
            MixinCamera.this.xWrap = 1;
        }

        @Override
        public void onZ2NegativeWrap() {
            MixinCamera.this.zWrap = -1;
        }

        @Override
        public void onZ2PositiveWrap() {
            MixinCamera.this.zWrap = 1;
        }
    };

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public FogType getFluidInCamera() {
        if (!this.initialized) {
            return FogType.NONE;
        }
        int x = this.blockPosition.getX();
        int y = this.blockPosition.getY();
        int z = this.blockPosition.getZ();
        FluidState fluidState = this.level.getFluidState_(x, y, z);
        if (fluidState.is(FluidTags.WATER) && this.position.y < y + fluidState.getHeight_(this.level, x, y, z)) {
            return FogType.WATER;
        }
        NearPlane nearPlane = this.setupAndGetNearPlane();
        return nearPlane.getFogType(this.position, this.level);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private double getMaxZoom(double startingDistance) {
        for (int i = 0; i < 8; ++i) {
            float dx = (i & 1) * 2 - 1;
            float dy = (i >> 1 & 1) * 2 - 1;
            float dz = (i >> 2 & 1) * 2 - 1;
            dx *= 0.1F;
            dy *= 0.1F;
            dz *= 0.1F;
            HitResult hitresult = this.level.clip(this.clipContext.set(this.position.x + dx, this.position.y + dy, this.position.z + dz,
                                                                       this.position.x - this.forwards.x() * startingDistance + dx + dz,
                                                                       this.position.y - this.forwards.y() * startingDistance + dy,
                                                                       this.position.z - this.forwards.z() * startingDistance + dz,
                                                                       ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity)
            );
            this.clipContext.reset();
            if (hitresult.getType() != HitResult.Type.MISS) {
                double dist = VectorUtil.dist(this.position, hitresult.x(), hitresult.y(), hitresult.z());
                if (dist < startingDistance) {
                    startingDistance = dist - 0.1;
                    if (startingDistance < 0) {
                        startingDistance = 0;
                    }
                }
            }
        }
        return startingDistance;
    }

    @Shadow
    public abstract Camera.NearPlane getNearPlane();

    @Override
    public int getXWrap() {
        return this.xWrap;
    }

    @Override
    public int getZWrap() {
        return this.zWrap;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void move(double distOffset, double vertOffset, double horizOffset) {
        double dx = this.forwards.x() * distOffset + this.up.x() * vertOffset + this.left.x() * horizOffset;
        double dy = this.forwards.y() * distOffset + this.up.y() * vertOffset + this.left.y() * horizOffset;
        double dz = this.forwards.z() * distOffset + this.up.z() * vertOffset + this.left.z() * horizOffset;
        this.setPosition(this.position.x + dx, this.position.y + dy, this.position.z + dz);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setPosition(Vec3 vec) {
        ((Vec3d) this.position).set(vec);
        this.blockPosition.set(vec.x, vec.y, vec.z);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setPosition(double x, double y, double z) {
        ((Vec3d) this.position).set(x, y, z);
        this.blockPosition.set(x, y, z);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setRotation(float yRot, float xRot) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation.mul(this.quatY.set(Vector3f.YP, -yRot, true));
        this.rotation.mul(this.quatX.set(Vector3f.XP, xRot, true));
        this.forwards.set(0.0F, 0.0F, 1.0F);
        this.forwards.transform(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.transform(this.rotation);
        this.left.set(1.0F, 0.0F, 0.0F);
        this.left.transform(this.rotation);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTicks) {
        this.xWrap = 0;
        this.zWrap = 0;
        this.initialized = true;
        this.level = level;
        if (!entity.equals(this.entity)) {
            if (this.entity != null && this.lastAttachId != -1) {
                this.entity.detachWrapCallback(this.lastAttachId);
            }
            this.lastAttachId = entity.attachWrapCallback(this.wrapCallback);
            this.entity = entity;
        }
        this.detached = detached;
        this.setRotation(entity.getViewYRot(partialTicks), entity.getViewXRot(partialTicks));
        if (detached) {
            this.setPosition(Mth.lerp(partialTicks, entity.xo, entity.getX()),
                             Mth.lerp(partialTicks, entity.yo, entity.getY()) + entity.getEyeHeight(),
                             Mth.lerp(partialTicks, entity.zo, entity.getZ())
            );
        }
        else {
            Vec3 eyePosition = entity.getEyePosition(partialTicks);
            this.setPosition(eyePosition.x(), eyePosition.y(), eyePosition.z());
        }
        if (detached) {
            if (thirdPersonReverse) {
                this.setRotation(this.yRot + 180.0F, -this.xRot);
            }
            this.move(-this.getMaxZoom(4.0), 0.0, 0.0);
        }
        else if (entity instanceof LivingEntity living && living.isSleeping()) {
            Direction bedOrientation = living.getBedOrientation();
            this.setRotation(bedOrientation != null ? bedOrientation.toYRot() - 180.0F : 0.0F, -60);
            this.move(0.0, 0.3, 0.0);
        }
    }

    @Unique
    private NearPlane setupAndGetNearPlane() {
        Minecraft minecraft = Minecraft.getInstance();
        double aspectRatio = minecraft.getWindow().getWidth() / (double) minecraft.getWindow().getHeight();
        double d1 = Math.tan(minecraft.options.fov * Mth.DEG_TO_RAD / 2.0) * 0.05F;
        double d2 = d1 * aspectRatio;
        this.nearPlane.setup(this.forwards, 0.05, this.left, d2, this.up, d1);
        return this.nearPlane;
    }
}
