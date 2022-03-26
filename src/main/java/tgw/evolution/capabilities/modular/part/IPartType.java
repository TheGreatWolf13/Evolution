package tgw.evolution.capabilities.modular.part;

import net.minecraft.network.chat.Component;
import tgw.evolution.init.ItemMaterial;

public interface IPartType<T extends IPartType<T>> {

    boolean canBeSharpened();

    Component getComponent();

    String getName();

    double getVolume(ItemMaterial material);

    boolean isTwoHanded();
}
