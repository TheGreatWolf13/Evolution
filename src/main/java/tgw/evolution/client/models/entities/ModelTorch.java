package tgw.evolution.client.models.entities;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelBox;

public class ModelTorch extends Model {

    private final RendererModel model;

    public ModelTorch() {
        this.textureWidth = 16;
        this.textureHeight = 16;
        this.model = new RendererModel(this, 0, 0);
        this.model.cubeList.add(new ModelBox(this.model, 4, 4, -1.0f, 0.0F, -1.0f, 2, 10, 2, 0.0F, false));
    }

    public void renderer() {
        this.model.render(0.062_5F);
    }
}
