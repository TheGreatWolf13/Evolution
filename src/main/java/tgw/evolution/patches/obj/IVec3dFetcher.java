package tgw.evolution.patches.obj;

import tgw.evolution.util.math.Vec3d;

@FunctionalInterface
public interface IVec3dFetcher {

    Vec3d fetch(int x, int y, int z, Vec3d vec);
}
