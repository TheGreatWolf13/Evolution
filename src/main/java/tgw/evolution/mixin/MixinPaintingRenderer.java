package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.LevelRenderer;

@Mixin(PaintingRenderer.class)
public abstract class MixinPaintingRenderer extends EntityRenderer<Painting> {

    public MixinPaintingRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    private void renderPainting(PoseStack matrices,
                                VertexConsumer builder,
                                Painting painting,
                                int width,
                                int height,
                                TextureAtlasSprite sprite,
                                TextureAtlasSprite backSprite) {
        PoseStack.Pose last = matrices.last();
        Matrix4f pose = last.pose();
        Matrix3f normal = last.normal();
        float f = -width / 2.0F;
        float f1 = -height / 2.0F;
        float f3 = backSprite.getU0();
        float f4 = backSprite.getU1();
        float f5 = backSprite.getV0();
        float f6 = backSprite.getV1();
        float f7 = backSprite.getU0();
        float f8 = backSprite.getU1();
        float f9 = backSprite.getV0();
        float f10 = backSprite.getV(1);
        float f11 = backSprite.getU0();
        float f12 = backSprite.getU(1);
        float f13 = backSprite.getV0();
        float f14 = backSprite.getV1();
        int i = width / 16;
        int j = height / 16;
        double d0 = 16.0 / i;
        double d1 = 16.0 / j;
        for (int k = 0; k < i; ++k) {
            for (int l = 0; l < j; ++l) {
                float f15 = f + (k + 1) * 16;
                float f16 = f + k * 16;
                float f17 = f1 + (l + 1) * 16;
                float f18 = f1 + l * 16;
                int x = painting.getBlockX();
                int y = Mth.floor(painting.getY() + (f17 + f18) / 32.0F);
                int z = painting.getBlockZ();
                Direction dir = painting.getDirection();
                switch (dir) {
                    case NORTH -> x = Mth.floor(painting.getX() + (f15 + f16) / 32.0F);
                    case WEST -> z = Mth.floor(painting.getZ() - (f15 + f16) / 32.0F);
                    case SOUTH -> x = Mth.floor(painting.getX() - (f15 + f16) / 32.0F);
                    case EAST -> z = Mth.floor(painting.getZ() + (f15 + f16) / 32.0F);
                }
                int l1 = LevelRenderer.getLightColor(painting.level, x, y, z);
                float f19 = sprite.getU(d0 * (i - k));
                float f20 = sprite.getU(d0 * (i - (k + 1)));
                float f21 = sprite.getV(d1 * (j - l));
                float f22 = sprite.getV(d1 * (j - (l + 1)));
                this.vertex(pose, normal, builder, f15, f18, f20, f21, -0.5F, 0, 0, -1, l1);
                this.vertex(pose, normal, builder, f16, f18, f19, f21, -0.5F, 0, 0, -1, l1);
                this.vertex(pose, normal, builder, f16, f17, f19, f22, -0.5F, 0, 0, -1, l1);
                this.vertex(pose, normal, builder, f15, f17, f20, f22, -0.5F, 0, 0, -1, l1);
                this.vertex(pose, normal, builder, f15, f17, f4, f5, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, builder, f16, f17, f3, f5, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, builder, f16, f18, f3, f6, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, builder, f15, f18, f4, f6, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, builder, f15, f17, f7, f9, -0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, builder, f16, f17, f8, f9, -0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, builder, f16, f17, f8, f10, 0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, builder, f15, f17, f7, f10, 0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, builder, f15, f18, f7, f9, 0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, builder, f16, f18, f8, f9, 0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, builder, f16, f18, f8, f10, -0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, builder, f15, f18, f7, f10, -0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, builder, f15, f17, f12, f13, 0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, builder, f15, f18, f12, f14, 0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, builder, f15, f18, f11, f14, -0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, builder, f15, f17, f11, f13, -0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, builder, f16, f17, f12, f13, -0.5F, 1, 0, 0, l1);
                this.vertex(pose, normal, builder, f16, f18, f12, f14, -0.5F, 1, 0, 0, l1);
                this.vertex(pose, normal, builder, f16, f18, f11, f14, 0.5F, 1, 0, 0, l1);
                this.vertex(pose, normal, builder, f16, f17, f11, f13, 0.5F, 1, 0, 0, l1);
            }
        }
    }

    @Shadow
    protected abstract void vertex(Matrix4f p_115537_,
                                   Matrix3f p_115538_,
                                   VertexConsumer p_115539_,
                                   float p_115540_,
                                   float p_115541_,
                                   float p_115542_,
                                   float p_115543_, float p_115544_, int p_115545_, int p_115546_, int p_115547_, int p_115548_);
}
