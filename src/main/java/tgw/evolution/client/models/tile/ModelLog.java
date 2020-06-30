package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import tgw.evolution.util.MathHelper;

public class ModelLog extends Model {

    private final RendererModel log;

    public ModelLog(int x, int y) {
        this.textureWidth = 40;
        this.textureHeight = 8;
        this.log = new RendererModel(this, 0, 0);
        this.log.addBox(-16, 8 + 4 * y, 4 * x, 16, 4, 4);
        setRotationAngle(this.log, 0, MathHelper.degToRad(90), 0);
    }

    public void render() {
        this.log.render(1 / 16f);
    }

    public static void setRotationAngle(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
