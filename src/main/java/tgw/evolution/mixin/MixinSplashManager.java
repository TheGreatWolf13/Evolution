package tgw.evolution.mixin;

import net.minecraft.client.User;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;

import java.util.*;

@Mixin(SplashManager.class)
public abstract class MixinSplashManager extends SimplePreparableReloadListener<List<String>> {

    @Shadow @Final private static Random RANDOM;
    @Mutable @Shadow @Final private static ResourceLocation SPLASHES_LOCATION;

    static {
        SPLASHES_LOCATION = Evolution.getResource("texts/splashes.txt");
    }

    @Shadow @Final private List<String> splashes;
    @Shadow @Final private @Nullable User user;

    /**
     * @author TheGreatWolf
     * @reason Make Merry X-mas! appear on 25th Dec as well.
     */
    @Overwrite
    public @Nullable String getSplash() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        if (month == Calendar.DECEMBER && (day == 24 || day == 25)) {
            return "Merry X-mas!";
        }
        if (month == Calendar.JANUARY && day == 1) {
            return "Happy new year!";
        }
        if (month == Calendar.OCTOBER && day == 31) {
            return "OOoooOOOoooo! Spooky!";
        }
        if (this.splashes.isEmpty()) {
            return null;
        }
        return this.user != null && RANDOM.nextInt(this.splashes.size()) == 42 ?
               this.user.getName().toUpperCase(Locale.ROOT) + " IS YOU" :
               this.splashes.get(RANDOM.nextInt(this.splashes.size()));
    }
}
