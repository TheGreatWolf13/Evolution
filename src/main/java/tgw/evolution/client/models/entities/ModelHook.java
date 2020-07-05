package tgw.evolution.client.models.entities;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelBox;
import tgw.evolution.util.MathHelper;

public class ModelHook extends Model {

    private final RendererModel bone;

    public ModelHook() {
        this.textureWidth = 16;
        this.textureHeight = 16;
        this.bone = new RendererModel(this, 0, 0);
        this.bone.setRotationPoint(0.0F, 24.0F, 0.0F);
        MathHelper.setRotationAngle(this.bone, -1.5708F, 0.0F, 0.0F);
        this.bone.cubeList.add(new ModelBox(this.bone, 8, 0, 2.5F, 2.0F, -23.0F, 1, 1, 3, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 8, 4, -3.5F, 2.0F, -23.0F, 1, 1, 3, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 7, 1.5F, 1.0F, -24.0F, 1, 1, 1, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 5, 0, -2.5F, 1.0F, -24.0F, 1, 1, 1, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 5, -1.5F, 0.0F, -25.0F, 1, 1, 1, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 9, 0.5F, 0.0F, -25.0F, 1, 1, 1, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 11, -0.5F, -1.0F, -25.0F, 1, 1, 1, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 13, -0.5F, -2.0F, -24.0F, 1, 1, 1, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 0, -0.5F, -3.0F, -23.0F, 1, 1, 3, 0.0F, false));
        this.bone.cubeList.add(new ModelBox(this.bone, 0, 8, -0.5F, -0.5F, -24.0F, 1, 1, 7, 0.0F, false));
    }

    public void renderer() {
        this.bone.render(0.0625F);
    }
}
