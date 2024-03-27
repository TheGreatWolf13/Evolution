package tgw.evolution.mixin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OArrayMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.List;
import java.util.Map;

@Mixin(PartDefinition.class)
public abstract class Mixin_CF_PartDefinition {

    @Mutable @Shadow @Final @RestoreFinal private Map<String, PartDefinition> children;
    @Mutable @Shadow @Final @RestoreFinal private List<CubeDefinition> cubes;
    @Mutable @Shadow @Final @RestoreFinal private PartPose partPose;

    @ModifyConstructor
    Mixin_CF_PartDefinition(List<CubeDefinition> list, PartPose partPose) {
        this.children = new O2OHashMap<>();
        this.cubes = list;
        this.partPose = partPose;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public ModelPart bake(int scaleX, int scaleY) {
        O2OMap<String, PartDefinition> children = (O2OMap<String, PartDefinition>) this.children;
        O2OMap<String, ModelPart> parts = new O2OArrayMap<>(children.size());
        for (long it = children.beginIteration(); children.hasNextIteration(it); it = children.nextEntry(it)) {
            parts.put(children.getIterationKey(it), children.getIterationValue(it).bake(scaleX, scaleY));
        }
        List<CubeDefinition> cubes = this.cubes;
        OList<ModelPart.Cube> bakedCubes = new OArrayList<>(cubes.size());
        for (int i = 0, len = cubes.size(); i < len; ++i) {
            //noinspection ObjectAllocationInLoop
            bakedCubes.add(cubes.get(i).bake(scaleX, scaleY));
        }
        ModelPart modelPart = new ModelPart(bakedCubes.view(), parts);
        modelPart.loadPose(this.partPose);
        return modelPart;
    }
}
