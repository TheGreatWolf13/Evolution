package tgw.evolution.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemParser.class)
public abstract class MixinItemParser {

    @Shadow @Final public static DynamicCommandExceptionType ERROR_UNKNOWN_ITEM;
    @Shadow private Item item;
    @Shadow @Final private StringReader reader;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void readItem() throws CommandSyntaxException {
        int i = this.reader.getCursor();
        ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
        Item item = (Item) Registry.ITEM.getNullable(resourceLocation);
        if (item == null) {
            this.reader.setCursor(i);
            throw ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourceLocation.toString());
        }
        this.item = item;
    }
}
