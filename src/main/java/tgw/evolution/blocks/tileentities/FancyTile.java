//package tgw.evolution.blocks.tileentities;
//
//import net.minecraft.block.BlockState;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.nbt.NBTUtil;
//import net.minecraft.network.NetworkManager;
//import net.minecraft.network.play.server.SUpdateTileEntityPacket;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraftforge.client.model.ModelDataManager;
//import net.minecraftforge.client.model.data.IModelData;
//import net.minecraftforge.client.model.data.ModelDataMap;
//import net.minecraftforge.client.model.data.ModelProperty;
//import net.minecraftforge.common.util.Constants;
//import tgw.evolution.init.EvolutionTileEntities;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.Objects;
//
//public class FancyTile extends TileEntity {
//
//    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();
//
//    private BlockState mimic;
//
//    public FancyTile() {
//        super(EvolutionTileEntities.FANCYBLOCK_TILE.get());
//    }
//
//    public void setMimic(BlockState mimic) {
//        this.mimic = mimic;
//        this.markDirty();
//        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
//    }
//
//    @Nullable
//    @Override
//    public SUpdateTileEntityPacket getUpdatePacket() {
//        CompoundNBT tag = new CompoundNBT();
//        if (this.mimic != null) {
//            tag.put("mimic", NBTUtil.writeBlockState(this.mimic));
//        }
//        return new SUpdateTileEntityPacket(this.pos, 1, tag);
//    }
//
//    @Override
//    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
//        BlockState oldMimic = this.mimic;
//        CompoundNBT tag = pkt.getNbtCompound();
//        if (tag.contains("mimic")) {
//            this.mimic = NBTUtil.readBlockState(tag.getCompound("mimic"));
//            if (!Objects.equals(oldMimic, this.mimic)) {
//                ModelDataManager.requestModelDataRefresh(this);
//                this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
//            }
//        }
//    }
//
//    @Nonnull
//    @Override
//    public IModelData getModelData() {
//        return new ModelDataMap.Builder().withInitial(MIMIC, this.mimic).build();
//    }
//
//    @Override
//    public void read(CompoundNBT tag) {
//        super.read(tag);
//        if (tag.contains("mimic")) {
//            this.mimic = NBTUtil.readBlockState(tag.getCompound("mimic"));
//        }
//    }
//
//    @Override
//    public CompoundNBT write(CompoundNBT tag) {
//        if (this.mimic != null) {
//            tag.put("mimic", NBTUtil.writeBlockState(this.mimic));
//        }
//        return super.write(tag);
//    }
//}