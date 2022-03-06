package tgw.evolution.capabilities.modular.part;

public class HiltPart extends GrabPart<PartTypes.Hilt> {

    public static final HiltPart DUMMY = new HiltPart();

    public HiltPart() {
        this.type = PartTypes.Hilt.NULL;
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.hilt." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Hilt getType(String type) {
        return PartTypes.Hilt.byName(type);
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }
}
