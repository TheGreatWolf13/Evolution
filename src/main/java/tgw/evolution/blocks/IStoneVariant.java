package tgw.evolution.blocks;

import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;

public interface IStoneVariant {

    EnumRockVariant getVariant();

    void setVariant(EnumRockVariant variant);

    EnumRockNames getStoneName();
}
