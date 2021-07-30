package tgw.evolution.client.gui.controls;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface ISort {

    void sort(List<ListKeyBinding.Entry> entries);
}
