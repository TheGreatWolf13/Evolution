package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;

@Mixin(ClientSuggestionProvider.class)
public abstract class MixinClientSuggestionProvider implements SharedSuggestionProvider {

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    private static String prettyPrint(double d) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static String prettyPrint(int i) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    public Collection<TextCoordinates> getAbsoluteCoordinates() {
        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            return Collections.singleton(
                    new SharedSuggestionProvider.TextCoordinates(prettyPrint(hitResult.x()), prettyPrint(hitResult.y()), prettyPrint(hitResult.z())));
        }
        return SharedSuggestionProvider.super.getAbsoluteCoordinates();
    }

    @Override
    @Overwrite
    public Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            return Collections.singleton(new SharedSuggestionProvider.TextCoordinates(prettyPrint(blockHitResult.posX()),
                                                                                      prettyPrint(blockHitResult.posY()),
                                                                                      prettyPrint(blockHitResult.posZ())));
        }
        return SharedSuggestionProvider.super.getRelevantCoordinates();
    }
}
