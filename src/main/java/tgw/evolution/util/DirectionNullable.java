package tgw.evolution.util;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public enum DirectionNullable implements IStringSerializable {
	DOWN("down", Direction.DOWN),
	UP("up", Direction.UP),
	NORTH("north", Direction.NORTH),
	SOUTH("south", Direction.SOUTH),
	WEST("west", Direction.WEST),
	EAST("east", Direction.EAST),
	NONE("none", null);

	private final String name;
	private final Direction direction;

	DirectionNullable(String nameIn, Direction direction) {
		this.name = nameIn;
		this.direction = direction;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	public Direction getDirection() {
		return this.direction;
	}
	
	public static DirectionNullable fromDirection(Direction direction) {
		switch (direction) {
			case DOWN :
				return DOWN;
			case EAST :
				return EAST;
			case NORTH :
				return NORTH;
			case SOUTH :
				return SOUTH;
			case UP :
				return UP;
			case WEST :
				return WEST;
		}
        throw new IllegalStateException("Unable to get Direction facing for direction " + direction);
	}
}