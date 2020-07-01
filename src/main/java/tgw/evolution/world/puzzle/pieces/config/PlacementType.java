package tgw.evolution.world.puzzle.pieces.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.template.GravityStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import tgw.evolution.util.MathHelper;

public enum PlacementType {
    RIGID(0, ImmutableList.of()),
    TERRAIN_MATCHING(1, ImmutableList.of(new GravityStructureProcessor(Heightmap.Type.WORLD_SURFACE_WG, -1)));

    private final byte id;
    private final ImmutableList<StructureProcessor> structureProcessors;

    PlacementType(int id, ImmutableList<StructureProcessor> processors) {
        this.id = MathHelper.toByteExact(id);
        this.structureProcessors = processors;
    }

    public static PlacementType byId(int id) {
        for (PlacementType type : PlacementType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return RIGID;
    }

    public byte getId() {
        return this.id;
    }

    public ImmutableList<StructureProcessor> getStructureProcessors() {
        return this.structureProcessors;
    }
}
