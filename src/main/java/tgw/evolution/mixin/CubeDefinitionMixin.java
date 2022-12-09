package tgw.evolution.mixin;

import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.UVPair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.ICubeDefinitionPatch;
import tgw.evolution.patches.ICubePatch;

@Mixin(CubeDefinition.class)
public abstract class CubeDefinitionMixin implements ICubeDefinitionPatch {
    @Shadow
    @Final
    private Vector3f dimensions;
    /**
     * Bit 0: shouldFix; <br>
     * Bit 1: upBend; <br>
     * Bit 2: downBend; <br>
     */
    private byte flags;
    @Shadow
    @Final
    private CubeDeformation grow;
    @Shadow
    @Final
    private boolean mirror;
    @Shadow
    @Final
    private Vector3f origin;
    @Shadow
    @Final
    private UVPair texCoord;
    @Shadow
    @Final
    private UVPair texScale;

    /**
     * @author TheGreatWolf
     * @reason Fix HMs and make bends.
     */
    @Overwrite
    public ModelPart.Cube bake(int texWidth, int texHeight) {
        int u = (int) this.texCoord.u();
        int v = (int) this.texCoord.v();
        float dimX = this.dimensions.x();
        float dimY = this.dimensions.y();
        float dimZ = this.dimensions.z();
        float growX = this.grow.growX;
        float growY = this.grow.growY;
        float growZ = this.grow.growZ;
        float width = texWidth * this.texScale.u();
        float height = texHeight * this.texScale.v();
        ModelPart.Cube cube = new ModelPart.Cube(u, v, this.origin.x(), this.origin.y(), this.origin.z(), dimX, dimY, dimZ, growX, growY, growZ,
                                                 this.mirror, width, height);
        if ((this.flags & 1) != 0) {
            ((ICubePatch) cube).fixInit(u, v, dimX, dimY, dimZ, growX, growY, growZ, this.mirror, width, height);
        }
        else {
            if ((this.flags & 2) != 0) {
                ((ICubePatch) cube).fixInitBend(u, v, dimX, dimY, dimZ, growX, growY, growZ, this.mirror, width, height, true);
            }
            else if ((this.flags & 4) != 0) {
                ((ICubePatch) cube).fixInitBend(u, v, dimX, dimY, dimZ, growX, growY, growZ, this.mirror, width, height, false);
            }
        }
        return cube;
    }

    @Override
    public void markBend(boolean up) {
        if (up) {
            this.flags |= 2;
        }
        else {
            this.flags |= 4;
        }
    }

    @Override
    public void requestFix() {
        this.flags |= 1;
    }
}
