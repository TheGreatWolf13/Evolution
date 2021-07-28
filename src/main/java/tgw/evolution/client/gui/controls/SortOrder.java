package tgw.evolution.client.gui.controls;

import net.minecraft.client.resources.I18n;

import java.util.Comparator;
import java.util.List;

public enum SortOrder {
    NONE(entries -> {
    }),
    AZ(entries -> {
        entries.sort(Comparator.comparing(o -> ((ListKeyBinding.KeyEntry) o).getKeyDesc()));
    }),
    ZA(entries -> {
        entries.sort((o1, o2) -> ((ListKeyBinding.KeyEntry) o2).getKeyDesc().compareTo(((ListKeyBinding.KeyEntry) o1).getKeyDesc()));
    });

    private final ISort sorter;

    SortOrder(ISort sorter) {
        this.sorter = sorter;
    }

    public SortOrder cycle() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public String getName() {
        switch (this) {
            default:
            case NONE:
                return I18n.format("evolution.options.controls.none");
            case AZ:
                return I18n.format("evolution.options.controls.az");
            case ZA:
                return I18n.format("evolution.options.controls.za");
        }
    }

    public void sort(List<ListKeyBinding.Entry> list) {
        this.sorter.sort(list);
    }
}
