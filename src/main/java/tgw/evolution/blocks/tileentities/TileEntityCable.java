//package tgw.evolution.blocks.tileentities;
//
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.tileentity.TileEntity;
//import tgw.evolution.init.EvolutionTileEntities;
//
//public class TileEntityCable extends TileEntity {
//	
//	public int positiveConnection;
//	public int negativeConnection;
//
//	public TileEntityCable() {
//		super(EvolutionTileEntities.CABLE);
//	}
//
//	@Override
//	public void read(CompoundNBT compound) {
//		this.positiveConnection = compound.getInt("PositiveConnection");
//		this.negativeConnection = compound.getInt("NegativeConnection");
//		super.read(compound);
//	}
//	
//	@Override
//	public CompoundNBT write(CompoundNBT compound) {
//		compound.putInt("PositiveConnection", this.positiveConnection);
//		compound.putInt("NegativeConnection", this.negativeConnection);
//		return super.write(compound);
//	}
//}
