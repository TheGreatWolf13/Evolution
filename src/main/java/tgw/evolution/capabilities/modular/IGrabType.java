package tgw.evolution.capabilities.modular;

import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.items.modular.part.ItemPart;

public interface IGrabType<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> extends IPartType<T, I, P> {

    double getGrabPoint();

    int getLength();
}
