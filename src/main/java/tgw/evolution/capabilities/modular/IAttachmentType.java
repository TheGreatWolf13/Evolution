package tgw.evolution.capabilities.modular;

import tgw.evolution.capabilities.modular.part.IPartType;

public interface IAttachmentType<T extends IPartType<T>> extends IPartType<T> {

    double getRelativeCenterOfMass(int grabLength);
}
