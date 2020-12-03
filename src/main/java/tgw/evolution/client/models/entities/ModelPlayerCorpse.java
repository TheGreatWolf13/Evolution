package tgw.evolution.client.models.entities;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelBox;
import tgw.evolution.util.MathHelper;

public class ModelPlayerCorpse extends Model {

    private final RendererModel base;
    private final RendererModel overlay;

    public ModelPlayerCorpse() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.base = new RendererModel(this);
        this.base.setRotationPoint(-1.0F, 36.0F, -1.0F);
        MathHelper.setRotationAngle(this.base, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        this.base.cubeList.add(new ModelBox(this.base, 40, 16, 2.0F, 3.0F, -15.0F, 2, 12, 2, 0.0F, true));
        this.base.cubeList.add(new ModelBox(this.base, 0, 0, -3.0F, -17.0F, -18.0F, 8, 8, 8, 0.0F, false));
        this.base.cubeList.add(new ModelBox(this.base, 16, 16, -3.0F, -9.0F, -16.0F, 8, 12, 4, 0.0F, false));
        this.base.cubeList.add(new ModelBox(this.base, 0, 16, -5.0F, -9.0F, -15.0F, 2, 12, 2, 0.0F, false));
        this.base.cubeList.add(new ModelBox(this.base, 0, 16, 5.0F, -9.0F, -15.0F, 2, 12, 2, 0.0F, true));
        this.base.cubeList.add(new ModelBox(this.base, 40, 16, -2.0F, 3.0F, -15.0F, 2, 12, 2, 0.0F, false));
        this.overlay = new RendererModel(this);
    }

    public ModelPlayerCorpse(boolean smallArms) {
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.base = new RendererModel(this);
        this.base.setRotationPoint(0.0F, 24.0F, 0.0F);
        MathHelper.setRotationAngle(this.base, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        this.base.cubeList.add(new ModelBox(this.base, 0, 0, -4.0f, -16.0f, -6.0f, 8, 8, 8, 0.0F, false));
        this.base.cubeList.add(new ModelBox(this.base, 16, 16, -4.0f, -8.0f, -4.0f, 8, 12, 4, 0.0F, false));
        this.overlay = new RendererModel(this);
        this.overlay.setRotationPoint(0.0F, 24.0F, 0.0F);
        MathHelper.setRotationAngle(this.overlay, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        if (smallArms) {
            this.base.cubeList.add(new ModelBox(this.base, 40, 16, -9.0f, -8.0f, -4.0f, 3, 12, 4, 0.0F, false));
            this.base.cubeList.add(new ModelBox(this.base, 32, 48, 4.0F, -8.0f, -4.0f, 3, 12, 4, 0.0F, false));
            this.overlay.cubeList.add(new ModelBox(this.overlay, 40, 32, -9.0f, -8.0f, -4.0f, 3, 12, 4, 0.25F, false));
            this.overlay.cubeList.add(new ModelBox(this.overlay, 48, 48, 4.0F, -8.0f, -4.0f, 3, 12, 4, 0.25F, false));
        }
        else {
            this.base.cubeList.add(new ModelBox(this.base, 40, 16, -8.0f, -8.0f, -4.0f, 4, 12, 4, 0.0F, false));
            this.base.cubeList.add(new ModelBox(this.base, 32, 48, 4.0F, -8.0f, -4.0f, 4, 12, 4, 0.0F, false));
            this.overlay.cubeList.add(new ModelBox(this.overlay, 40, 32, -8.0f, -8.0f, -4.0f, 4, 12, 4, 0.25F, false));
            this.overlay.cubeList.add(new ModelBox(this.overlay, 48, 48, 4.0F, -8.0f, -4.0f, 4, 12, 4, 0.25F, false));
        }
        this.base.cubeList.add(new ModelBox(this.base, 0, 16, -4.0f, 4.0F, -4.0f, 4, 12, 4, 0.0F, false));
        this.base.cubeList.add(new ModelBox(this.base, 16, 48, 0.0F, 4.0F, -4.0f, 4, 12, 4, 0.0F, false));
        this.overlay.cubeList.add(new ModelBox(this.overlay, 32, 0, -4.0f, -16.0f, -6.0f, 8, 8, 8, 0.25F, false));
        this.overlay.cubeList.add(new ModelBox(this.overlay, 16, 32, -4.0f, -8.0f, -4.0f, 8, 12, 4, 0.26F, false));
        this.overlay.cubeList.add(new ModelBox(this.overlay, 0, 32, -4.0f, 4.0F, -4.0f, 4, 12, 4, 0.25F, false));
        this.overlay.cubeList.add(new ModelBox(this.overlay, 0, 48, 0.0F, 4.0F, -4.0f, 4, 12, 4, 0.25F, false));
    }

    public void render(float scale) {
        this.base.render(scale);
        this.overlay.render(scale);
    }
}
