package tgw.evolution.capabilities.modular.part;

public class PolePart extends GrabPart<PartTypes.Pole> {

    public static final PolePart DUMMY = new PolePart();

    public PolePart() {
        this.type = PartTypes.Pole.NULL;
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.pole." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Pole getType(String type) {
        return PartTypes.Pole.byName(type);
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }
}
