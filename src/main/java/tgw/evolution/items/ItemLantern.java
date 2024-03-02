package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.events.ClientEvents;

public class ItemLantern extends ItemEv {

    private final short lightColor;

    public ItemLantern(int lightColor, Properties properties) {
        super(properties);
        this.lightColor = (short) lightColor;
    }

    @Override
    public short getLightEmission(ItemStack stack) {
        int ticks = ClientEvents.getInstance().getTicks();
        int steps = 12;
        int time = 20;
        ticks %= steps * time;
        ticks /= time;
        float r = 0;
        float g = 0;
        float b = 0;
        //5 and 6 look the same
        //10 and 11 look the same
        switch (ticks) {
            case 0 -> {
                //RED
                r = 1;
                g = 0;
                b = 0;
            }
            case 1 -> {
                //ORANGE
                r = 1;
                g = 0.5f;
                b = 0;
            }
            case 2 -> {
                //YELLOW
                r = 1;
                g = 1;
                b = 0;
            }
            case 3 -> {
                //LIME
                r = 0.5f;
                g = 1;
                b = 0;
            }
            case 4 -> {
                //GREEN
                r = 0;
                g = 1;
                b = 0;
            }
            case 5 -> {
                //PRISMARINE
                r = 0;
                g = 1;
                b = 0.5f;
            }
            case 6 -> {
                //CYAN
                r = 0;
                g = 1;
                b = 1;
            }
            case 7 -> {
                //SOFT BLUE
                r = 0;
                g = 0.5f;
                b = 1;
            }
            case 8 -> {
                //BLUE
                r = 0;
                g = 0;
                b = 1;
            }
            case 9 -> {
                //PINK-ISH
                r = 0.5f;
                g = 0;
                b = 1;
            }
            case 10 -> {
                //MAGENTA
                r = 1;
                g = 0;
                b = 1;
            }
            case 11 -> {
                //PINK-ISH
                r = 1;
                g = 0;
                b = 0.5f;
            }
        }
        r = 1;
        g = 0.5f;
        b = 0;
        int rr = r != 0 ? 15 : 0;
        int rs = r == 1 ? 1 : 0;
        int gr = g != 0 ? 15 : 0;
        int gs = g == 1 ? 1 : 0;
        int br = b != 0 ? 15 : 0;
        int bs = b == 1 ? 1 : 0;
        return (short) (rr | rs << 4 | gr << 5 | gs << 9 | br << 10 | bs << 14);
    }
}
