package tgw.evolution.capabilities.modular.part;

import net.minecraft.network.chat.Component;

public interface IPartType<T extends IPartType<T>> {

    T byName(String name);

    boolean canBeSharpened();

    Component getComponent();

    String getName();

    float getVolume();

    boolean isTwoHanded();
}
