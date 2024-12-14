package tgw.evolution.client.renderer.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Octree {
    private final int playerSectionCenterX;
    private final int playerSectionCenterY;
    private final int playerSectionCenterZ;
    private final Octree.Branch root;

    public Octree(int camSecX, int camSecY, int camSecZ, int renderDistance, int height, int minBuildHeight) {
        int viewDistance = renderDistance * 2 + 1;
        int width = Mth.smallestEncompassingPowerOfTwo(viewDistance);
        int radius = renderDistance * 16;
        int camX = SectionPos.sectionToBlockCoord(camSecX);
        int camY = SectionPos.sectionToBlockCoord(camSecY);
        int camZ = SectionPos.sectionToBlockCoord(camSecZ);
        this.playerSectionCenterX = camX + 8;
        this.playerSectionCenterY = camY + 8;
        this.playerSectionCenterZ = camZ + 8;
        int minX = camX - radius;
        int maxX = minX + width * 16 - 1;
        int minY = width >= height ? minBuildHeight : camY - radius;
        int maxY = minY + width * 16 - 1;
        int minZ = camZ - radius;
        int maxZ = minZ + width * 16 - 1;
        this.root = new Octree.Branch(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean add(SectionRenderDispatcher.RenderSection section) {
        return this.root.add(section);
    }

    public int visitNodes(Octree.OctreeVisitor visitor, Frustum frustum) {
        return this.root.visitNodes(visitor, Visibility.OUTSIDE, frustum, 0, 0);
    }

    @Environment(EnvType.CLIENT)
    class Branch implements Octree.Node {
        private final int bbCenterX;
        private final int bbCenterY;
        private final int bbCenterZ;
        /**
         * Bit 0 ~ 2: Ordinal of AxisSorting;<br>
         * Bit 3: playerXDiffNegative;<br>
         * Bit 4: playerYDiffNegative;<br>
         * Bit 5: playerZDiffNegative;<br>
         */
        private final byte flags;
        private final int maxX;
        private final int maxY;
        private final int maxZ;
        private final int minX;
        private final int minY;
        private final int minZ;
        private @Nullable Octree.Node node0;
        private @Nullable Octree.Node node1;
        private @Nullable Octree.Node node2;
        private @Nullable Octree.Node node3;
        private @Nullable Octree.Node node4;
        private @Nullable Octree.Node node5;
        private @Nullable Octree.Node node6;
        private @Nullable Octree.Node node7;

        public Branch(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.bbCenterX = minX + (maxX - minX + 1) / 2;
            this.bbCenterY = minY + (maxY - minY + 1) / 2;
            this.bbCenterZ = minZ + (maxZ - minZ + 1) / 2;
            this.flags = this.getFlags();
        }

        private static int getNodeIndex(Octree.AxisSorting axisSorting, boolean xShift, boolean yShift, boolean zShift) {
            int i = 0;
            if (xShift) {
                i += axisSorting.xShift;
            }
            if (yShift) {
                i += axisSorting.yShift;
            }
            if (zShift) {
                i += axisSorting.zShift;
            }
            return i;
        }

        public boolean add(SectionRenderDispatcher.RenderSection section) {
            boolean isXNeg = section.getX() - this.bbCenterX < 0;
            boolean isYNeg = section.getY() - this.bbCenterY < 0;
            boolean isZNeg = section.getZ() - this.bbCenterZ < 0;
            boolean xShift = isXNeg == ((this.flags & 1 << 3) == 0);
            boolean yShift = isYNeg == ((this.flags & 1 << 4) == 0);
            boolean zShift = isZNeg == ((this.flags & 1 << 5) == 0);
            int i = getNodeIndex(AxisSorting.VALUES[this.flags & 0b111], xShift, yShift, zShift);
            Node node = this.getNode(i);
            if (this.areChildrenLeaves()) {
                boolean existed = node != null;
                this.setNode(i, section);
                return !existed;
            }
            if (node != null) {
                return ((Branch) node).add(section);
            }
            Branch branch = this.getBranch(isXNeg, isYNeg, isZNeg);
            this.setNode(i, branch);
            return branch.add(section);
        }

        private boolean areChildrenLeaves() {
            return this.maxX - this.minX + 1 == 32;
        }

        private Branch getBranch(boolean isXNeg, boolean isYNeg, boolean isZNeg) {
            int minX;
            int maxX;
            if (isXNeg) {
                minX = this.minX;
                maxX = this.bbCenterX - 1;
            }
            else {
                minX = this.bbCenterX;
                maxX = this.maxX;
            }
            int minY;
            int maxY;
            if (isYNeg) {
                minY = this.minY;
                maxY = this.bbCenterY - 1;
            }
            else {
                minY = this.bbCenterY;
                maxY = this.maxY;
            }
            int minZ;
            int maxZ;
            if (isZNeg) {
                minZ = this.minZ;
                maxZ = this.bbCenterZ - 1;
            }
            else {
                minZ = this.bbCenterZ;
                maxZ = this.maxZ;
            }
            return Octree.this.new Branch(minX, minY, minZ, maxX, maxY, maxZ);
        }

        private byte getFlags() {
            int dx = Octree.this.playerSectionCenterX - this.bbCenterX;
            int dy = Octree.this.playerSectionCenterY - this.bbCenterY;
            int dz = Octree.this.playerSectionCenterZ - this.bbCenterZ;
            byte flags = (byte) AxisSorting.getAxisSorting(Math.abs(dx), Math.abs(dy), Math.abs(dz)).ordinal();
            if (dx < 0) {
                flags |= 1 << 3;
            }
            if (dy < 0) {
                flags |= 1 << 4;
            }
            if (dz < 0) {
                flags |= 1 << 5;
            }
            return flags;
        }

        private @Nullable Node getNode(int index) {
            return switch (index) {
                case 0 -> this.node0;
                case 1 -> this.node1;
                case 2 -> this.node2;
                case 3 -> this.node3;
                case 4 -> this.node4;
                case 5 -> this.node5;
                case 6 -> this.node6;
                case 7 -> this.node7;
                default -> throw new IncompatibleClassChangeError();
            };
        }

        @Override
        public @Nullable SectionRenderDispatcher.RenderSection getSection() {
            return null;
        }

        @Override
        public double getX0() {
            return this.minX;
        }

        @Override
        public double getX1() {
            return this.maxX + 1;
        }

        @Override
        public double getY0() {
            return this.minY;
        }

        @Override
        public double getY1() {
            return this.maxY + 1;
        }

        @Override
        public double getZ0() {
            return this.minZ;
        }

        @Override
        public double getZ1() {
            return this.maxZ + 1;
        }

        private void setNode(int index, Node node) {
            switch (index) {
                case 0 -> this.node0 = node;
                case 1 -> this.node1 = node;
                case 2 -> this.node2 = node;
                case 3 -> this.node3 = node;
                case 4 -> this.node4 = node;
                case 5 -> this.node5 = node;
                case 6 -> this.node6 = node;
                case 7 -> this.node7 = node;
                default -> throw new IncompatibleClassChangeError();
            }
        }

        @Override
        public int visitNodes(Octree.OctreeVisitor octreeVisitor, @Visibility int visibility, Frustum frustum, int depth, int count) {
            boolean shouldRender = visibility == Visibility.INSIDE;
            if (!shouldRender) {
                ++count;
                visibility = frustum.intersectWith(this.getX0(), this.getY0(), this.getZ0(), this.getX1(), this.getY1(), this.getZ1());
                shouldRender = visibility > Visibility.OUTSIDE;
            }
            if (shouldRender) {
                count = octreeVisitor.visit(this, visibility, depth, count);
                for (int i = 0; i < 8; ++i) {
                    Node node = this.getNode(i);
                    if (node != null) {
                        count = node.visitNodes(octreeVisitor, visibility, frustum, depth + 1, count);
                    }
                }
            }
            return count;
        }
    }

    @Environment(EnvType.CLIENT)
    enum AxisSorting {
        XYZ(4, 2, 1),
        XZY(4, 1, 2),
        YXZ(2, 4, 1),
        YZX(1, 4, 2),
        ZXY(2, 1, 4),
        ZYX(1, 2, 4);

        static final AxisSorting[] VALUES = values();

        final int xShift;
        final int yShift;
        final int zShift;

        AxisSorting(int xShift, int yShift, int zShift) {
            this.xShift = xShift;
            this.yShift = yShift;
            this.zShift = zShift;
        }

        public static Octree.AxisSorting getAxisSorting(int x, int y, int z) {
            if (x > y && x > z) {
                return y > z ? XYZ : XZY;
            }
            if (y > x && y > z) {
                return x > z ? YXZ : YZX;
            }
            return x > y ? ZXY : ZYX;
        }
    }

    @Environment(EnvType.CLIENT)
    public interface Node {

        @Nullable SectionRenderDispatcher.RenderSection getSection();

        double getX0();

        double getX1();

        double getY0();

        double getY1();

        double getZ0();

        double getZ1();

        int visitNodes(Octree.OctreeVisitor octreeVisitor, @Visibility int visibility, Frustum frustum, int depth, int count);
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface OctreeVisitor {
        int visit(Octree.Node node, @Visibility int visibility, int depth, int count);
    }
}

