package tgw.evolution.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;

@Mixin(BlockStateParser.class)
public abstract class Mixin_M_BlockStateParser {

    @Shadow @Final public static DynamicCommandExceptionType ERROR_UNKNOWN_BLOCK;
    @Shadow @Final public static Dynamic3CommandExceptionType ERROR_INVALID_VALUE;
    @Shadow private StateDefinition<Block, BlockState> definition;
    @Shadow private ResourceLocation id;
    @Shadow @Final private Map<Property<?>, Comparable<?>> properties;
    @Shadow @Final private StringReader reader;
    @Shadow private BlockState state;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void readBlock() throws CommandSyntaxException {
        int cursor = this.reader.getCursor();
        this.id = ResourceLocation.read(this.reader);
        Block block = (Block) Registry.BLOCK.getNullable(this.id);
        if (block == null) {
            this.reader.setCursor(cursor);
            throw ERROR_UNKNOWN_BLOCK.createWithContext(this.reader, this.id.toString());
        }
        this.definition = block.getStateDefinition();
        this.state = block.defaultBlockState();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private CommandSyntaxException method_17956(int par1) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private <T extends Comparable<T>> void setValue(Property<T> property, String name, int cursor) throws CommandSyntaxException {
        T value = (T) property.getValue_(name);
        if (value != null) {
            this.state = this.state.setValue(property, value);
            this.properties.put(property, value);
        }
        else {
            this.reader.setCursor(cursor);
            throw ERROR_INVALID_VALUE.createWithContext(this.reader, this.id.toString(), property.getName(), name);
        }
    }
}
