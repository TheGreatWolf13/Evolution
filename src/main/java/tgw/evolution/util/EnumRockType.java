package tgw.evolution.util;

public enum EnumRockType {
	SEDIMENTARY("sedimentary", 14.0F, 4),
	METAMORPHIC("metamorphic", 15.0F, 5),
	IGNEOUS_INTRUSIVE("igneous_intrusive", 16.0F, 6),
	IGNEOUS_EXTRUSIVE("igneous_extrusive", 16.0F, 6);
	
	private final String name;
	private final float hardness;
	private final int rangeStone;
	
	EnumRockType(String name, float hardness, int rangeStone) {
		this.name = name;
		this.hardness = hardness;
		this.rangeStone = rangeStone;
	}
	
	public String getName() {
		return this.name;
	}
	
	public float getHardness() {
		return this.hardness;
	}
	
	public int getRangeStone() {
		return this.rangeStone;
	}
}

