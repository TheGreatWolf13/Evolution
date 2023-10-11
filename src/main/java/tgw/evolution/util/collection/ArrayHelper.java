package tgw.evolution.util.collection;

import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ArrayHelper {

    public static final ChatFormatting[] CHAT_FORMATTINGS = ChatFormatting.values();
    public static final InteractionHand[] HANDS_MAIN_PRIORITY = {InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
    public static final InteractionHand[] HANDS_OFF_PRIORITY = {InteractionHand.OFF_HAND, InteractionHand.MAIN_HAND};
    public static final Heightmap.Types[] HEIGHTMAP = Heightmap.Types.values();
    public static final SupportType[] SUPPORT_TYPE = SupportType.values();

    private ArrayHelper() {
    }
}
