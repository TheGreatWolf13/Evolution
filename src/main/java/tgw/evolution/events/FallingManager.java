package tgw.evolution.events;

import com.google.common.collect.MapMaker;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockDirt;
import tgw.evolution.blocks.BlockLeaves;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.util.TreeUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FallingManager {

    public static final Map<IWorld, FallingManager> fallingManagers;

    static {
        fallingManagers = new MapMaker().weakKeys().makeMap();
    }

    private final Collection<Tree> fellQueue = new LinkedList<>();
    private final IWorld world;

    public FallingManager(IWorld world) {
        this.world = world;
    }

    public boolean isEmpty() {
        return this.fellQueue.isEmpty();
    }

    void tick() {
        this.fellQueue.removeIf(tree -> !tree.hasLogsToFell());
        this.fellQueue.forEach(tree -> {
            tree.prepForFelling();
            Iterator<BlockPos> it = tree.logsToFell.iterator();
            if (!it.hasNext()) {
                return;
            }
            BlockPos log = it.next();
            tree.fellLog(log);
            it.remove();
        });
    }

    public void onChop(BlockPos pos, Direction fellingDirection) {
        Tree tree = new Tree(this, pos, fellingDirection);
        tree.buildTree();
        tree.queueForFelling();
    }

    private class Branch {

        final FallingManager instance;
        private final Set<BlockPos> logs = new HashSet<>();
        private final Tree tree;
        private final BlockPos start;
        private boolean hasLeaves;
        private boolean rooted;

        Branch(FallingManager fallingManager, Tree tree, BlockPos startPos) {
            this.instance = fallingManager;
            this.tree = tree;
            this.start = startPos;
            this.addLog(new BlockPos.MutableBlockPos(startPos));
        }

        private void scan() {
            this.expandLogs(this.start);
            if (this.rooted) {
                return;
            }
            this.tree.addLogsToFell(this.logs);
        }

        private BlockPos addLog(BlockPos.MutableBlockPos targetPos) {
            BlockPos immutable = targetPos.toImmutable();
            this.logs.add(immutable);
            this.tree.logs.add(immutable);
            if (this.rooted) {
                return immutable;
            }
            targetPos.move(Direction.DOWN);
            if (this.tree.contains(targetPos)) {
                return immutable;
            }
            BlockState targetState = this.instance.world.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof BlockDirt)) {
                return immutable;
            }
            this.rooted = true;
            return immutable;
        }

        private void expandLogs(BlockPos root) {
            ArrayDeque<BlockPos> logsToExpand = new ArrayDeque<>();
            logsToExpand.add(root);
            BlockPos nextBlock;
            while ((nextBlock = logsToExpand.poll()) != null) {
                //noinspection ObjectAllocationInLoop
                TreeUtils.iterateBlocks(1, nextBlock, targetPos -> {
                    if (this.tree.contains(targetPos)) {
                        return;
                    }
                    BlockState targetState = this.instance.world.getBlockState(targetPos);
                    if (targetState.getBlock() instanceof BlockLog && targetState.get(BlockLog.TREE)) {
                        logsToExpand.addLast(this.addLog(targetPos));
                        return;
                    }
                    if (!this.hasLeaves && targetState.getBlock() instanceof BlockLeaves) {
                        this.hasLeaves = true;
                    }
                });
            }
        }
    }

    private class Tree {

        final FallingManager instance;
        private final Collection<Branch> branches = new ConcurrentLinkedQueue<>();
        private final Set<BlockPos> logs = new HashSet<>();
        private final List<BlockPos> logsToFell = new LinkedList<>();
        private final List<BlockPos> newLogsToFell = new LinkedList<>();
        private final BlockPos choppedBlock;
        private final Direction fellingDirection;
        private Vec3d centroid = Vec3d.ZERO;

        Tree(FallingManager fallingManager, BlockPos choppedBlockPos, Direction fallingDirection) {
            this.instance = fallingManager;
            this.choppedBlock = choppedBlockPos;
            this.fellingDirection = fallingDirection;
            this.makeBranch(choppedBlockPos);
        }

        boolean contains(BlockPos pos) {
            return this.logs.contains(pos);
        }

        void addLogsToFell(Collection<BlockPos> logs) {
            this.newLogsToFell.addAll(logs);
        }

        void prepForFelling() {
            if (this.newLogsToFell.isEmpty()) {
                return;
            }
            this.logsToFell.addAll(this.newLogsToFell);
            this.updateCentroid();
            this.logsToFell.sort((o1, o2) -> {
                int yCompare = Integer.compare(o1.getY(), o2.getY());
                if (yCompare != 0) {
                    return yCompare;
                }
                int distCompare = Double.compare(this.centroid.squareDistanceTo(o2.getX(), o2.getY(), o2.getZ()), this.centroid.squareDistanceTo(o1.getX(), o1.getY(), o1.getZ()));
                if (distCompare == 0) {
                    return o1.compareTo(o2);
                }
                return distCompare;
            });
            this.newLogsToFell.clear();
        }

        void updateCentroid() {
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            Iterator<BlockPos> iterator = this.logs.iterator();
            while (true) {
                if (!iterator.hasNext()) {
                    int size = this.logs.size();
                    this.centroid = new Vec3d(x / size, y / size, z / size);
                    return;
                }
                BlockPos pos = iterator.next();
                x += pos.getX();
                y += pos.getY();
                z += pos.getZ();
            }
        }

        boolean hasLogsToFell() {
            return !this.logsToFell.isEmpty() || !this.newLogsToFell.isEmpty();
        }

        Branch makeBranch(BlockPos pos) {
            Branch branch = new Branch(this.instance, this, pos);
            this.branches.add(branch);
            return branch;
        }

        private void buildTree() {
            TreeUtils.iterateBlocks(1, this.choppedBlock, targetPos -> {
                if (this.contains(targetPos)) {
                    return;
                }
                BlockState targetState = this.instance.world.getBlockState(targetPos);
                if (!(targetState.getBlock() instanceof BlockLog)) {
                    return;
                }
                if (!targetState.get(BlockLog.TREE)) {
                    return;
                }
                this.scanNewBranch(targetPos.toImmutable());
            });
        }

        private void scanNewBranch(BlockPos pos) {
            Branch branch = this.makeBranch(pos);
            branch.scan();
        }

        private void queueForFelling() {
            if (!this.hasLogsToFell()) {
                return;
            }
            this.instance.fellQueue.add(this);
        }

        private void fellLog(BlockPos logPos) {
            TreeUtils.spawnFallingLog((World) this.instance.world, logPos, this.choppedBlock, this.fellingDirection);
            TreeUtils.iterateBlocks(4, logPos, targetPos -> {
                BlockState targetState = this.instance.world.getBlockState(targetPos);
                if (targetState.getBlock() instanceof BlockLeaves) {
                    TreeUtils.spawnFallingLeaves((World) this.instance.world, targetPos, logPos, this.choppedBlock, targetState, this.fellingDirection);
                    return;
                }
                if (!(targetState.getBlock() instanceof BlockLog)) {
                    return;
                }
                if (this.contains(targetPos)) {
                    return;
                }
                this.scanNewBranch(targetPos.toImmutable());
            });
        }
    }
}
