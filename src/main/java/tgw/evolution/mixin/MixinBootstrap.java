package tgw.evolution.mixin;

import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Bootstrap.class)
public abstract class MixinBootstrap {

    @Shadow private static volatile boolean isBootstrapped;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static void bootStrap() {
        if (!isBootstrapped) {
            isBootstrapped = true;
            if (Registry.REGISTRY.size() == 0) {
                throw new IllegalStateException("Unable to load registries");
            }
            FireBlock.bootStrap();
            ComposterBlock.bootStrap();
            //noinspection ConstantValue
            if (EntityType.getKey(EntityType.PLAYER) == null) {
                throw new IllegalStateException("Failed loading EntityTypes");
            }
            PotionBrewing.bootStrap();
            EntitySelectorOptions.bootStrap();
            DispenseItemBehavior.bootStrap();
            CauldronInteraction.bootStrap();
            ArgumentTypes.bootStrap();
            Registry.freezeBuiltins();
            wrapStreams();
        }
    }

    @Shadow
    private static void wrapStreams() {
        throw new AbstractMethodError();
    }
}
