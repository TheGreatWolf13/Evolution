package tgw.evolution.mixin;

import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.filters.VanillaPacketSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgeMod.class)
public abstract class ForgeModMixin {

    /**
     * @author TheGreatWolf
     * @reason Disable version check, we don't need that since we don't update anyways.
     */
    @Overwrite
    public void preInit(FMLCommonSetupEvent evt) {
        this.registerArgumentTypes();
        VanillaPacketSplitter.register();
    }

    @Shadow
    protected abstract void registerArgumentTypes();
}
