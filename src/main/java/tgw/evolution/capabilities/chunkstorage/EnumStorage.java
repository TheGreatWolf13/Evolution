package tgw.evolution.capabilities.chunkstorage;

public enum EnumStorage {
	NITROGEN(0, "Nitrogen"),
	PHOSPHORUS(1, "Phosphorus"),
	POTASSIUM(2, "Potassium"),
	WATER(3, "Water"),
	CARBON_DIOXIDE(4, "Carbon Dioxide"),
	OXYGEN(5, "Oxygen"),
	GAS_NITROGEN(6, "Nitrogen Gas"),
	ORGANIC(7, "Organic Matter");
	
	private final int id;
	private final String name;
	
	EnumStorage(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getId() {
		return this.id;
	}
}
