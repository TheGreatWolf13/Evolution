package tgw.evolution.client.gui.advancements;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum CriteriaDetail {
    OFF("Off", false, false),
    DEFAULT("Default", true, false),
    SPOILER("Spoiler", false, true),
    ALL("All", true, true);

    private final String name;
    private final boolean obtained;
    private final boolean unobtained;

    CriteriaDetail(String description, boolean obtained, boolean unobtained) {
        this.name = description;
        this.obtained = obtained;
        this.unobtained = unobtained;
    }

    public String getName() {
        return this.name;
    }

    public boolean showObtained() {
        return this.obtained;
    }

    public boolean showUnobtained() {
        return this.unobtained;
    }
}
