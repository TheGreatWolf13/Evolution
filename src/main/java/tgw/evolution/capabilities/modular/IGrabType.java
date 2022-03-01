package tgw.evolution.capabilities.modular;

import tgw.evolution.capabilities.modular.part.IPartType;

public interface IGrabType<T extends IPartType<T>> extends IPartType<T> {

    double getGrabPoint();

    int getLength();
}
