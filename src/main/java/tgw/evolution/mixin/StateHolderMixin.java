package tgw.evolution.mixin;

import com.google.common.collect.Table;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.obj.FastImmutableTable;
import tgw.evolution.patches.obj.StatePropertyTableCache;

import java.util.Map;

@Mixin(StateHolder.class)
public abstract class StateHolderMixin<O, S> {

    @Shadow
    @Final
    protected O owner;
    @Shadow
    private Table<Property<?>, Comparable<?>, S> neighbours;

    @Inject(method = "populateNeighbours", at = @At("RETURN"))
    private void postPopulateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> states, CallbackInfo ci) {
        this.neighbours = new FastImmutableTable<>(this.neighbours, StatePropertyTableCache.getTableCache(this.owner));
    }
}
