package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelTileShadowHound extends Model {

    private final RendererModel cube;

    public ModelTileShadowHound() {
        this.textureHeight = 32;
        this.textureWidth = 64;
        this.cube = new RendererModel(this, 0, 0);
        this.cube.addBox(0, 0, 0, 16, 16, 16);
    }

    public void render() {
        this.cube.render(1.0F / 16.0F);
    }
}
