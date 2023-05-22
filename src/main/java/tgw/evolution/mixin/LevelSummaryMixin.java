package tgw.evolution.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.ILevelSummaryPatch;
import tgw.evolution.util.math.Metric;

@Mixin(LevelSummary.class)
public abstract class LevelSummaryMixin implements ILevelSummaryPatch {

    private long size;

    @Shadow
    public abstract boolean askToOpenWorld();

    /**
     * @author TheGreatWolf
     * @reason Add size in disk
     */
    @Overwrite
    private Component createInfo() {
        if (this.isLocked()) {
            return new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
        }
        if (this.requiresManualConversion()) {
            return new TranslatableComponent("selectWorld.conversion").withStyle(ChatFormatting.RED);
        }
        if (!this.isCompatible()) {
            return new TranslatableComponent("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
        }
        MutableComponent info = this.isHardcore() ?
                                new TextComponent("").append(new TranslatableComponent("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED)) :
                                new TranslatableComponent("gameMode." + this.getGameMode().getName());
        if (this.hasCheats()) {
            info.append(", ").append(new TranslatableComponent("selectWorld.cheats"));
        }
        MutableComponent versionName = this.getWorldVersionName();
        MutableComponent version = new TextComponent(", ").append(new TranslatableComponent("selectWorld.version")).append(" ");
        if (this.markVersionInList()) {
            version.append(versionName.withStyle(this.askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
        }
        else {
            version.append(versionName);
        }
        info.append(version);
        info.append(" (" + Metric.bytes(this.size, 1) + ")");
        return info;
    }

    @Shadow
    public abstract GameType getGameMode();

    @Shadow
    public abstract MutableComponent getWorldVersionName();

    @Shadow
    public abstract boolean hasCheats();

    @Shadow
    public abstract boolean isCompatible();

    @Shadow
    public abstract boolean isHardcore();

    @Shadow
    public abstract boolean isLocked();

    @Shadow
    public abstract boolean markVersionInList();

    @Shadow
    public abstract boolean requiresManualConversion();

    @Override
    public void setSizeOnDisk(long size) {
        this.size = size;
    }
}
