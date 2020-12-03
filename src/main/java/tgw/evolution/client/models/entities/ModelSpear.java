package tgw.evolution.client.models.entities;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelSpear extends Model {

    private final RendererModel pole;
    private final RendererModel headMain;
    private final RendererModel headSideWest;
    private final RendererModel headSideEast;

    public ModelSpear() {
        this.textureWidth = 32;
        this.textureHeight = 32;
        this.pole = new RendererModel(this, 0, 0);
        this.pole.addBox(-0.5f, -27.0f, -0.5f, 1, 31, 1, 0.0F);
        this.headMain = new RendererModel(this, 4, 0);
        this.headMain.addBox(-1.5f, -4.0f, -0.5f, 3, 7, 1);
        this.headSideWest = new RendererModel(this, 8, 8);
        this.headSideWest.addBox(1.5F, -3.0f, -0.5f, 1, 4, 1);
        this.headSideEast = new RendererModel(this, 4, 8);
        this.headSideEast.addBox(-2.5f, -3.0f, -0.5f, 1, 4, 1);
    }

    public void render() {
        this.pole.render(0.062_5F);
        this.headMain.render(0.062_5F);
        this.headSideWest.render(0.062_5F);
        this.headSideEast.render(0.062_5F);
    }
}
