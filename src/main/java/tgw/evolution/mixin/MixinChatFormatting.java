package tgw.evolution.mixin;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Collection;

@Mixin(ChatFormatting.class)
public abstract class MixinChatFormatting {

    @Overwrite
    public static @Nullable ChatFormatting getByCode(char c) {
        char d = Character.toLowerCase(c);
        for (ChatFormatting chatFormatting : ArrayHelper.CHAT_FORMATTINGS) {
            if (chatFormatting.code == d) {
                return chatFormatting;
            }
        }
        return null;
    }

    @Overwrite
    public static @Nullable ChatFormatting getById(int i) {
        if (i < 0) {
            return ChatFormatting.RESET;
        }
        for (ChatFormatting chatFormatting : ArrayHelper.CHAT_FORMATTINGS) {
            if (chatFormatting.getId() == i) {
                return chatFormatting;
            }
        }
        return null;
    }

    @Overwrite
    public static Collection<String> getNames(boolean bl, boolean bl2) {
        OList<String> list = new OArrayList<>();
        for (ChatFormatting chatFormatting : ArrayHelper.CHAT_FORMATTINGS) {
            if ((!chatFormatting.isColor() || bl) && (!chatFormatting.isFormat() || bl2)) {
                list.add(chatFormatting.getName());
            }
        }
        return list;
    }
}
