//package tgw.evolution.client.models.tile;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import com.mojang.blaze3d.vertex.IVertexBuilder;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.model.Model;
//import net.minecraft.client.renderer.model.ModelRenderer;
//
//public class ModelTileShadowHound extends Model {
//
//    private final ModelRenderer cube;
//
//    public ModelTileShadowHound() {
//        super(RenderType::entityCutout);
//        this.texHeight = 32;
//        this.texWidth = 64;
//        this.cube = new ModelRenderer(this, 0, 0);
//        this.cube.addBox(0, 0, 0, 16, 16, 16);
//    }
//
//    @Override
//    public void renderToBuffer(MatrixStack matrices,
//                               IVertexBuilder buffer,
//                               int packedLight,
//                               int packedOverlay,
//                               float red,
//                               float green,
//                               float blue,
//                               float alpha) {
//        this.cube.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//    }
//}
