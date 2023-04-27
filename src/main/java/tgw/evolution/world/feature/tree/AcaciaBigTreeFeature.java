//package tgw.evolution.world.feature.tree;
//
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.gen.IWorldGenerationReader;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.BlockLeaves;
//import tgw.evolution.blocks.BlockSapling;
//import tgw.evolution.blocks.util.BlockUtils;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.TreeUtils;
//
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Function;
//
//import static tgw.evolution.init.EvolutionBStates.AXIS;
//import static tgw.evolution.init.EvolutionBStates.TREE;
//
//public class AcaciaBigTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
//
//    private static final BlockState LOG = EvolutionBlocks.LOG_ACACIA.get().getDefaultState().with(TREE, true).with(AXIS, Direction.Axis.Y);
//    private static final BlockState LEAVES = EvolutionBlocks.LEAVES_ACACIA.get().getDefaultState();
//
//    public AcaciaBigTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config, boolean doBlockNofityOnPlace) {
//        super(config, doBlockNofityOnPlace);
//    }
//
//    private static boolean placeTreeOfHeight(IWorldGenerationReader worldIn, BlockPos pos, int height) {
//        int posX = pos.getX();
//        int posY = pos.getY();
//        int posZ = pos.getZ();
//        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
//        for (int l = 0; l <= height + 1; ++l) {
//            int i1 = 1;
//            if (l == 0) {
//                i1 = 0;
//            }
//            if (l >= height - 1) {
//                i1 = 2;
//            }
//            for (int j1 = -i1; j1 <= i1; ++j1) {
//                for (int k1 = -i1; k1 <= i1; ++k1) {
//                    if (!BlockSapling.canGrowInto(worldIn, blockpos$mutableblockpos.setPos(posX + j1, posY + l, posZ + k1))) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private void makeCanopy(IWorldGenerationReader worldIn, BlockPos pos) {
//        int posX = pos.getX();
//        int posY = pos.getY();
//        int posZ = pos.getZ();
//        for (int i = -3; i <= 3; i++) {
//            for (int j = -3; j <= 3; j++) {
//                if ((Math.abs(i) != 3 || Math.abs(j) != 3) && (Math.abs(i) != 3 || Math.abs(j) != 2) && (Math.abs(i) != 2 || Math.abs(j) != 3)) {
//                    this.placeLeaves(worldIn, posX + i, posY, posZ + j);
//                }
//            }
//        }
//        for (int i = -1; i <= 1; i++) {
//            for (int j = -1; j <= 1; j++) {
//                this.placeLeaves(worldIn, posX + i, posY + 1, posZ + j);
//            }
//        }
//        this.placeLeaves(worldIn, posX - 2, posY + 1, posZ);
//        this.placeLeaves(worldIn, posX + 2, posY + 1, posZ);
//        this.placeLeaves(worldIn, posX, posY + 1, posZ + 2);
//        this.placeLeaves(worldIn, posX, posY + 1, posZ - 2);
//    }
//
//    private void makeTopCanopy(IWorldGenerationReader worldIn, BlockPos pos) {
//        int posX = pos.getX();
//        int posY = pos.getY();
//        int posZ = pos.getZ();
//        for (int i = -3; i <= 3; i++) {
//            for (int j = -3; j <= 3; j++) {
//                if (Math.abs(i) != 3 || Math.abs(j) != 3) {
//                    this.placeLeaves(worldIn, posX + i, posY, posZ + j);
//                }
//            }
//        }
//        for (int i = -2; i <= 2; i++) {
//            for (int j = -2; j <= 2; j++) {
//                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
//                    this.placeLeaves(worldIn, posX + i, posY + 1, posZ + j);
//                }
//            }
//        }
//    }
//
//    @Override
//    public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position, MutableBoundingBox box) {
//        int totalHeight = 9 + rand.nextInt(4);
//        int trunkHeight = totalHeight - 6;
//        int posX = position.getX();
//        int posY = position.getY();
//        int posZ = position.getZ();
//        //tree within world height limit
//        if (posY >= 1 && posY + trunkHeight + 1 < 256) {
//            BlockPos posDown = position.down();
//            boolean isSoil = BlockUtils.canSustainSapling(((IBlockReader) worldIn).getBlockState(posDown), EvolutionBlocks.SAPLING_ACACIA.get());
//            if (!isSoil) {
//                return false;
//            }
//            if (!placeTreeOfHeight(worldIn, position, trunkHeight)) {
//                return false;
//            }
//            //the NW corner sapling grows first
//            TreeUtils.setDirtAt(worldIn, posDown);
//            TreeUtils.setDirtAt(worldIn, posDown.east());
//            TreeUtils.setDirtAt(worldIn, posDown.south());
//            TreeUtils.setDirtAt(worldIn, posDown.south().east());
//            BlockPos.MutableBlockPos trunkPos = new BlockPos.MutableBlockPos();
//            for (int placingTrunks = 0; placingTrunks < trunkHeight; ++placingTrunks) {
//                //placing main trunk
//                int posYForPlacement = posY + placingTrunks;
//                trunkPos.setPos(posX, posYForPlacement, posZ);
//                this.placeTrunk(changedBlocks, worldIn, trunkPos, box);
//                this.placeTrunk(changedBlocks, worldIn, trunkPos.east(), box);
//                this.placeTrunk(changedBlocks, worldIn, trunkPos.south(), box);
//                this.placeTrunk(changedBlocks, worldIn, trunkPos.east().south(), box);
//            }
//            //place "root" blocks
//            if (rand.nextInt(4) > 0) {
//                if (rand.nextBoolean()) {
//                    this.placeTrunk(changedBlocks, worldIn, position.west(), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.west().down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.west().down(), box);
//                    }
//                }
//                else {
//                    this.placeTrunk(changedBlocks, worldIn, position.west().south(), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.west().south().down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.west().south().down(), box);
//                    }
//                }
//            }
//            if (rand.nextInt(4) > 0) {
//                if (rand.nextBoolean()) {
//                    this.placeTrunk(changedBlocks, worldIn, position.east().north(), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.east().north().down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.east().north().down(), box);
//                    }
//                }
//                else {
//                    this.placeTrunk(changedBlocks, worldIn, position.north(), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.north().down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.north().down(), box);
//                    }
//                }
//            }
//            if (rand.nextInt(4) > 0) {
//                if (rand.nextBoolean()) {
//                    this.placeTrunk(changedBlocks, worldIn, position.south().east(2), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.south().east(2).down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.south().east(2).down(), box);
//                    }
//                }
//                else {
//                    this.placeTrunk(changedBlocks, worldIn, position.east(2), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.east(2).down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.east(2).down(), box);
//                    }
//                }
//            }
//            if (rand.nextInt(4) > 0) {
//                if (rand.nextBoolean()) {
//                    this.placeTrunk(changedBlocks, worldIn, position.east().south(2), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.east().south(2).down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.east().south(2).down(), box);
//                    }
//                }
//                else {
//                    this.placeTrunk(changedBlocks, worldIn, position.south(2), box);
//                    if (BlockUtils.isReplaceable(((IBlockReader) worldIn).getBlockState(position.south(2).down()))) {
//                        this.placeTrunk(changedBlocks, worldIn, position.south(2).down(), box);
//                    }
//                }
//            }
//            //choosing directions
//            int chooseTrunkToShorten = rand.nextInt(4);
//            BlockPos shortenTrunk = new BlockPos(posX, posY + trunkHeight - 1, posZ);
//            BlockPos smallStartPos = new BlockPos(posX, posY + trunkHeight - 1, posZ);
//            Direction smallMainInc = Direction.NORTH;
//            Direction smallSecInc = Direction.NORTH;
//            if (chooseTrunkToShorten == 0) {
//                if (rand.nextBoolean()) {
//                    smallSecInc = Direction.EAST;
//                    smallStartPos = smallStartPos.north().east();
//                }
//                else {
//                    smallMainInc = Direction.WEST;
//                    smallSecInc = Direction.SOUTH;
//                    smallStartPos = smallStartPos.south().west();
//                }
//            }
//            else if (chooseTrunkToShorten == 1) {
//                shortenTrunk = shortenTrunk.east();
//                if (rand.nextBoolean()) {
//                    smallSecInc = Direction.WEST;
//                    smallStartPos = smallStartPos.north();
//                }
//                else {
//                    smallMainInc = Direction.EAST;
//                    smallSecInc = Direction.SOUTH;
//                    smallStartPos = smallStartPos.east(2).south();
//                }
//            }
//            else if (chooseTrunkToShorten == 2) {
//                shortenTrunk = shortenTrunk.south();
//                if (rand.nextBoolean()) {
//                    smallMainInc = Direction.SOUTH;
//                    smallSecInc = Direction.EAST;
//                    smallStartPos = smallStartPos.south(2).east();
//                }
//                else {
//                    smallMainInc = Direction.WEST;
//                    smallStartPos = smallStartPos.west();
//                }
//            }
//            else {
//                shortenTrunk = shortenTrunk.east().south();
//                if (rand.nextBoolean()) {
//                    smallMainInc = Direction.SOUTH;
//                    smallSecInc = Direction.WEST;
//                    smallStartPos = smallStartPos.south(2);
//                }
//                else {
//                    smallMainInc = Direction.EAST;
//                    smallStartPos = smallStartPos.east(2);
//                }
//            }
//            //shorten trunk
//            int shorten = trunkHeight / 2;
//            for (int i = 0; i > -shorten; i--) {
//                worldIn.setBlockState(shortenTrunk.add(0, i, 0), Blocks.AIR.getDefaultState(), 18);
//            }
//            //placing small branch
//            int smallBranch = totalHeight - trunkHeight - 1 - rand.nextInt(2);
//            int smallMainLimit = smallBranch - 1 - rand.nextInt(3);
//            int smallSecLimit = smallMainLimit - 1;
//            int smallX = smallStartPos.getX();
//            int smallZ = smallStartPos.getZ();
//            int smallLastY = smallStartPos.getY();
//            //placing branch logs
//            BlockPos.MutableBlockPos branchPos = new BlockPos.MutableBlockPos();
//            for (int placeBranch = 0; placeBranch < smallBranch; placeBranch++) {
//                int branchY = smallStartPos.getY() + placeBranch;
//                if (smallMainLimit > 0 && placeBranch > 0) {
//                    smallX += smallMainInc.getXOffset();
//                    smallZ += smallMainInc.getZOffset();
//                    if (smallSecLimit > 0) {
//                        smallX += smallSecInc.getXOffset();
//                        smallZ += smallSecInc.getZOffset();
//                        --smallSecLimit;
//                    }
//                    --smallMainLimit;
//                }
//                branchPos.setPos(smallX, branchY, smallZ);
//                this.placeTrunk(changedBlocks, worldIn, branchPos, box);
//                smallLastY = branchY;
//            }
//            //placing branch leaves
//            this.makeCanopy(worldIn, new BlockPos(smallX, smallLastY, smallZ));
//            //med branch
//            Direction medMainInc = smallSecInc;
//            Direction medSecInc = smallMainInc.getOpposite();
//            int medBranch = totalHeight - trunkHeight + 1;
//            int medMainLimit = medBranch - 2 - rand.nextInt(3);
//            int medStartX = smallStartPos.getX();
//            int medStartZ = smallStartPos.getZ();
//            medStartX += medSecInc.getXOffset() + medSecInc.getXOffset() + medMainInc.getXOffset();
//            medStartZ += medSecInc.getZOffset() + medSecInc.getZOffset() + medMainInc.getZOffset();
//            int medX = medStartX;
//            int medZ = medStartZ;
//            int medLastY = posY;
//            BlockPos.MutableBlockPos medPos = new BlockPos.MutableBlockPos();
//            for (int placingMed = 0; placingMed < medBranch; placingMed++) {
//                int medY = posY + trunkHeight - 1 + placingMed;
//                if (medMainLimit > 0 && placingMed > 0) {
//                    medStartX += medMainInc.getXOffset();
//                    medStartZ += medMainInc.getZOffset();
//                    --medMainLimit;
//                }
//                medPos.setPos(medStartX, medY, medStartZ);
//                this.placeTrunk(changedBlocks, worldIn, medPos, box);
//                medLastY = medY;
//            }
//            this.makeTopCanopy(worldIn, new BlockPos(medStartX, medLastY, medStartZ));
//            //main branch
//            Direction mainInc = smallMainInc.getOpposite();
//            Direction p1Inc = medMainInc.getOpposite();
//            int mainBranch = totalHeight - trunkHeight + 1;
//            int p2Branch = mainBranch - 1 - rand.nextInt(3);
//            int p1Branch = p2Branch + 1;
//            int comp = mainBranch - 1;
//            int mainLimit = mainBranch - 1 - rand.nextInt(3);
//            int p1Limit = p1Branch - 1 - rand.nextInt(2);
//            int p2Limit = p2Branch - 1 - rand.nextInt(2);
//            int compLimit = 1 + rand.nextInt(2);
//            int mainX = medX + p1Inc.getXOffset() + p1Inc.getXOffset() + mainInc.getXOffset();
//            int mainZ = medZ + p1Inc.getZOffset() + p1Inc.getZOffset() + mainInc.getZOffset();
//            int p1X = mainX;
//            int p2X = mainX;
//            int p1Z = mainZ;
//            int p2Z = mainZ;
//            int compX = mainX;
//            int compZ = mainZ;
//            BlockPos.MutableBlockPos compPos = new BlockPos.MutableBlockPos();
//            for (int i = 0; i < comp; i++) {
//                int compY = posY + trunkHeight + i;
//                if (i > comp / 2 - 1 && compLimit > 0) {
//                    compX += mainInc.getOpposite().getXOffset();
//                    compZ += mainInc.getOpposite().getZOffset();
//                    --compLimit;
//                }
//                compPos.setPos(compX, compY, compZ);
//                this.placeTrunk(changedBlocks, worldIn, compPos, box);
//            }
//            int mainLastY = posY;
//            boolean p1 = false;
//            boolean p2 = false;
//            BlockPos.MutableBlockPos p1Pos = new BlockPos.MutableBlockPos();
//            BlockPos.MutableBlockPos p2Pos = new BlockPos.MutableBlockPos();
//            BlockPos.MutableBlockPos mainPos = new BlockPos.MutableBlockPos();
//            for (int placingMain = 0; placingMain < mainBranch; placingMain++) {
//                int mainY = posY + trunkHeight - 1 + placingMain;
//                if (mainLimit > 0 && placingMain > 0) {
//                    mainX += mainInc.getXOffset();
//                    mainZ += mainInc.getZOffset();
//                    if (!p1) {
//                        p1X += mainInc.getXOffset();
//                        p1Z += mainInc.getZOffset();
//                    }
//                    if (!p2) {
//                        p2X += mainInc.getXOffset();
//                        p2Z += mainInc.getZOffset();
//                    }
//                    --mainLimit;
//                }
//                if (placingMain >= mainBranch - p1Branch) {
//                    p1 = true;
//                    if (p1Limit > 0) {
//                        p1X += p1Inc.getXOffset();
//                        p1Z += p1Inc.getZOffset();
//                        --p1Limit;
//                    }
//                    p1Pos.setPos(p1X, mainY, p1Z);
//                    this.placeTrunk(changedBlocks, worldIn, p1Pos, box);
//                }
//                if (placingMain >= mainBranch - p2Branch) {
//                    p2 = true;
//                    if (p2Limit > 0) {
//                        p2X += medMainInc.getXOffset();
//                        p2Z += medMainInc.getZOffset();
//                        --p2Limit;
//                    }
//                    p2Pos.setPos(p2X, mainY, p2Z);
//                    this.placeTrunk(changedBlocks, worldIn, p2Pos, box);
//                }
//                mainPos.setPos(mainX, mainY, mainZ);
//                this.placeTrunk(changedBlocks, worldIn, mainPos, box);
//                mainLastY = mainY;
//            }
//            this.makeTopCanopy(worldIn, new BlockPos(mainX, mainLastY, mainZ));
//            this.makeTopCanopy(worldIn, new BlockPos(p1X, mainLastY, p1Z));
//            this.makeTopCanopy(worldIn, new BlockPos(p2X, mainLastY, p2Z));
//            this.makeTopCanopy(worldIn, new BlockPos(compX, mainLastY, compZ));
//            return true;
//        }
//        return false;
//    }
//
//    private void placeLeaves(IWorldGenerationReader world, int posX, int posY, int posZ) {
//        BlockPos leafPos = new BlockPos(posX, posY, posZ);
//        if (((IBlockReader) world).getBlockState(leafPos).isAir((IBlockReader) world, leafPos)) {
//            this.setBlockState(world, leafPos, LEAVES);
//        }
//    }
//
//    private void placeTrunk(Set<BlockPos> changedBlocks, IWorldGenerationReader world, BlockPos pos, MutableBoundingBox box) {
//        BlockState state = ((IBlockReader) world).getBlockState(pos);
//        if (state.isAir((IBlockReader) world, pos) || state.getBlock() instanceof BlockLeaves) {
//            if (BlockSapling.canGrowInto(world, pos)) {
//                this.setLogState(changedBlocks, world, pos, LOG, box);
//            }
//        }
//    }
//}