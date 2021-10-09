package tgw.evolution.client.gui.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.advancements.AdvancementState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class BetterDisplayInfo implements IBetterDisplayInfo {
    private static final int WHITE = 0xff_ffff;
    private static final int MINECRAFT_COMPLETED_ICON_COLOR = 0xDB_A213;
    private static final int MINECRAFT_UNCOMPLETED_ICON_COLOR = 0xFF_FFFF;
    private static final int MINECRAFT_COMPLETED_TITLE_COLOR = 0xDB_A213;
    private static final int MINECRAFT_UNCOMPLETED_TITLE_COLOR = 0x04_89C1;
    public static int defaultCompletedTitleColor = MINECRAFT_COMPLETED_TITLE_COLOR;
    public static int defaultUncompletedTitleColor = MINECRAFT_UNCOMPLETED_TITLE_COLOR;
    public static int defaultUncompletedIconColor = MINECRAFT_UNCOMPLETED_ICON_COLOR;
    public static int defaultCompletedIconColor = MINECRAFT_COMPLETED_ICON_COLOR;
    public static int defaultCompletedLineColor = 0xff_ffff;
    public static int defaultUncompletedLineColor = 0x33_3333;
    private final ResourceLocation id;
    private boolean allowDragging;
    private int completedIconColor;
    private int completedLineColor;
    private int completedTitleColor;
    private boolean drawDirectLines;
    private boolean hideLines;
    @Nullable
    private Integer posX;
    @Nullable
    private Integer posY;
    private int unCompletedIconColor;
    private int unCompletedLineColor;
    private int unCompletedTitleColor;

    public BetterDisplayInfo(Advancement advancement) {
        this(advancement.getId());
        if (advancement instanceof IBetterDisplayInfo) {
            this.parseIBetterDisplayInfo((IBetterDisplayInfo) advancement);
        }
        if (advancement.getDisplay() instanceof IBetterDisplayInfo) {
            this.parseIBetterDisplayInfo((IBetterDisplayInfo) advancement.getDisplay());
        }
    }

    public BetterDisplayInfo(ResourceLocation id) {
        this.id = id;
        this.defaults();
    }

    @Override
    public boolean allowDragging() {
        return this.allowDragging;
    }

    private void defaults() {
        this.completedIconColor = defaultCompletedIconColor;
        this.completedTitleColor = defaultCompletedTitleColor;
        this.unCompletedIconColor = defaultUncompletedIconColor;
        this.unCompletedTitleColor = defaultUncompletedTitleColor;
        this.drawDirectLines = false;
        this.unCompletedLineColor = defaultUncompletedLineColor;
        this.completedLineColor = defaultCompletedLineColor;
        this.posX = null;
        this.posY = null;
        this.hideLines = false;
        this.allowDragging = false;
    }

    @Override
    public Boolean drawDirectLines() {
        return this.drawDirectLines;
    }

    @Override
    public int getCompletedIconColor() {
        return this.completedIconColor;
    }

    @Override
    public int getCompletedLineColor() {
        return this.completedLineColor;
    }

    @Override
    public int getCompletedTitleColor() {
        return this.completedTitleColor;
    }

    public int getIconColor(AdvancementState state) {
        if (!this.hasCustomIconColor()) {
            return WHITE;
        }
        return state == AdvancementState.OBTAINED ? this.completedIconColor : this.unCompletedIconColor;
    }

    public int getIconYMultiplier(AdvancementState state) {
        if (this.hasCustomIconColor()) {
            return 2;
        }
        return state == AdvancementState.OBTAINED ? 0 : 1;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nullable
    @Override
    public Integer getPosX() {
        return this.posX;
    }

    @Nullable
    @Override
    public Integer getPosY() {
        return this.posY;
    }

    public int getTitleColor(AdvancementState state) {
        if (!this.hasCustomIconColor()) {
            return WHITE;
        }
        return state == AdvancementState.OBTAINED ? this.completedTitleColor : this.unCompletedTitleColor;
    }

    public int getTitleYMultiplier(AdvancementState state) {
        if (this.hasCustomTitleColor()) {
            return 3;
        }
        return state == AdvancementState.OBTAINED ? 0 : 1;
    }

    @Override
    public int getUnCompletedIconColor() {
        return this.unCompletedIconColor;
    }

    @Override
    public int getUnCompletedLineColor() {
        return this.unCompletedLineColor;
    }

    @Override
    public int getUnCompletedTitleColor() {
        return this.unCompletedTitleColor;
    }

    public boolean hasCustomIconColor() {
        return this.completedIconColor != MINECRAFT_COMPLETED_ICON_COLOR || this.unCompletedIconColor != MINECRAFT_UNCOMPLETED_ICON_COLOR;
    }

    public boolean hasCustomTitleColor() {
        return this.completedTitleColor != MINECRAFT_COMPLETED_TITLE_COLOR || this.unCompletedTitleColor != MINECRAFT_UNCOMPLETED_TITLE_COLOR;
    }

    @Override
    public Boolean hideLines() {
        return this.hideLines;
    }

    private void parseIBetterDisplayInfo(IBetterDisplayInfo betterDisplayInfo) {
        if (betterDisplayInfo.getCompletedIconColor() != -1) {
            this.completedIconColor = betterDisplayInfo.getCompletedIconColor();
        }
        if (betterDisplayInfo.getUnCompletedIconColor() != -1) {
            this.unCompletedIconColor = betterDisplayInfo.getUnCompletedIconColor();
        }
        if (betterDisplayInfo.getCompletedTitleColor() != -1) {
            this.completedTitleColor = betterDisplayInfo.getCompletedTitleColor();
        }
        if (betterDisplayInfo.getUnCompletedTitleColor() != -1) {
            this.unCompletedTitleColor = betterDisplayInfo.getUnCompletedTitleColor();
        }
        if (betterDisplayInfo.drawDirectLines() != null) {
            this.drawDirectLines = betterDisplayInfo.drawDirectLines();
        }
        if (betterDisplayInfo.getCompletedLineColor() != -1) {
            this.completedLineColor = betterDisplayInfo.getCompletedLineColor();
        }
        if (betterDisplayInfo.getUnCompletedLineColor() != -1) {
            this.unCompletedLineColor = betterDisplayInfo.getUnCompletedLineColor();
        }
        if (betterDisplayInfo.getPosX() != null) {
            this.posX = betterDisplayInfo.getPosX();
        }
        if (betterDisplayInfo.getPosY() != null) {
            this.posY = betterDisplayInfo.getPosY();
        }
        if (betterDisplayInfo.hideLines() != null) {
            this.hideLines = betterDisplayInfo.hideLines();
        }
        this.allowDragging = betterDisplayInfo.allowDragging();
    }
}
