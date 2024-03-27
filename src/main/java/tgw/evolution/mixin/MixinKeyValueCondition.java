package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(KeyValueCondition.class)
public abstract class MixinKeyValueCondition implements Condition {

    @Shadow @Final private String key;

    @Shadow @Final private String value;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private Predicate<BlockState> getBlockStatePredicate(StateDefinition<Block, BlockState> stateDefinition, Property<?> property, String name) {
        Comparable<?> propValue = property.getValue_(name);
        if (propValue == null) {
            throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", name, this.key, stateDefinition.getOwner(), this.value));
        }
        return state -> state.getValue(property).equals(propValue);
    }
}
