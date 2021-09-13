package tgw.evolution.client.gui.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.stats.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.stats.EvolutionStatisticsManager;
import tgw.evolution.stats.IEvoStatFormatter;
import tgw.evolution.util.reflection.FieldHandler;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ScreenStats extends Screen implements IProgressMeter {

    private static final FieldHandler<Stat, IStatFormatter> FORMATTER = new FieldHandler<>(Stat.class, "field_75976_b");
    private static final FieldHandler<StatsScreen, Screen> PARENT_SCREEN = new FieldHandler<>(StatsScreen.class, "field_146332_f");
    private final EvolutionStatisticsManager stats;
    private ListDamageStats damageStats;
    private ListDeathStats deathStats;
    private int displayId;
    @Nullable
    private ExtendedList<?> displaySlot;
    private ListDistanceStats distanceStats;
    private boolean doesGuiPauseGame = true;
    private ListCustomStats generalStats;
    private ListStats itemStats;
    private ListMobStats mobStats;
    private ListTimeStats timeStats;

    public ScreenStats(StatsScreen parent, StatisticsManager manager) {
        super(new TranslationTextComponent("gui.stats"));
        this.stats = (EvolutionStatisticsManager) manager;
    }

    private static int getCategoryOffset(int category) {
        return 115 + 40 * category;
    }

    public static String getFormattedName(Stat<ResourceLocation> stat) {
        return I18n.get("stat." + stat.getValue().toString().replace(':', '.'));
    }

    @Nullable
    public ExtendedList<?> byId(int displayId) {
        switch (displayId) {
            case 0: {
                return this.generalStats;
            }
            case 1: {
                return this.itemStats;
            }
            case 2: {
                return this.mobStats;
            }
            case 3: {
                return this.distanceStats;
            }
            case 4: {
                return this.timeStats;
            }
            case 5: {
                return this.deathStats;
            }
            case 6: {
                return this.damageStats;
            }
        }
        return null;
    }

    private void drawDamageSprite(MatrixStack matrices, int x, int y, EvolutionDamage.Type type) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_STATS_ICONS);
        blit(matrices, x, y, this.getBlitOffset(), 0, 0, 18, 18, 128, 128);
        RenderSystem.enableBlend();
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_DAMAGE_ICONS);
        blit(matrices, x + 1, y + 1, this.getBlitOffset(), type.getTexX() * 16, type.getTexY() * 16, 16, 16, 128, 128);
        RenderSystem.disableBlend();
    }

    private void drawSprite(MatrixStack matrices, int x, int y, int u, int v) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_STATS_ICONS);
        blit(matrices, x, y, this.getBlitOffset(), u, v, 18, 18, 128, 128);
    }

    private void drawStatsScreen(MatrixStack matrices, int x, int y, Item item) {
        this.drawSprite(matrices, x + 1, y + 1, 0, 0);
        RenderSystem.enableRescaleNormal();
        RenderHelper.turnBackOn();
        this.itemRenderer.renderGuiItem(item.getDefaultInstance(), x + 2, y + 2);
        RenderHelper.turnOff();
        RenderSystem.disableRescaleNormal();
    }

    @Nullable
    public ExtendedList<?> getDisplaySlot() {
        return this.displaySlot;
    }

    @Override
    protected void init() {
        this.doesGuiPauseGame = true;
        this.minecraft.getConnection().send(new CClientStatusPacket(CClientStatusPacket.State.REQUEST_STATS));
    }

    public void initButtons() {
        this.addButton(new Button(this.width / 2 - 160,
                                  this.height - 52,
                                  80,
                                  20,
                                  EvolutionTexts.GUI_STATS_GENERAL_BUTTON,
                                  button -> this.setDisplaySlot(0)));
        Button itemButton = this.addButton(new Button(this.width / 2 - 80,
                                                      this.height - 52,
                                                      80,
                                                      20,
                                                      EvolutionTexts.GUI_STATS_ITEMS_BUTTON,
                                                      button -> this.setDisplaySlot(1)));
        Button mobButton = this.addButton(new Button(this.width / 2,
                                                     this.height - 52,
                                                     80,
                                                     20,
                                                     EvolutionTexts.GUI_STATS_MOB_BUTTON,
                                                     button -> this.setDisplaySlot(2)));
        this.addButton(new Button(this.width / 2 + 80,
                                  this.height - 52,
                                  80,
                                  20,
                                  EvolutionTexts.GUI_STATS_DISTANCE_BUTTON,
                                  button -> this.setDisplaySlot(3)));
        this.addButton(new Button(this.width / 2 - 120,
                                  this.height - 32,
                                  80,
                                  20,
                                  EvolutionTexts.GUI_STATS_TIME_BUTTON,
                                  button -> this.setDisplaySlot(4)));
        this.addButton(new Button(this.width / 2 - 40,
                                  this.height - 32,
                                  80,
                                  20,
                                  EvolutionTexts.GUI_STATS_DEATH_BUTTON,
                                  button -> this.setDisplaySlot(5)));
        this.addButton(new Button(this.width / 2 + 40,
                                  this.height - 32,
                                  80,
                                  20,
                                  EvolutionTexts.GUI_STATS_DAMAGE_BUTTON,
                                  button -> this.setDisplaySlot(6)));
        if (this.itemStats.children().isEmpty()) {
            itemButton.active = false;
        }
        if (this.mobStats.children().isEmpty()) {
            mobButton.active = false;
        }
    }

    public void initLists() {
        this.generalStats = new ListCustomStats(this.minecraft);
        this.itemStats = new ListStats(this.minecraft);
        this.mobStats = new ListMobStats(this.minecraft);
        this.distanceStats = new ListDistanceStats(this.minecraft);
        this.timeStats = new ListTimeStats(this.minecraft);
        this.deathStats = new ListDeathStats(this.minecraft);
        this.damageStats = new ListDamageStats(this.minecraft);
    }

    @Override
    public boolean isPauseScreen() {
        return !this.doesGuiPauseGame;
    }

    @Override
    public void onStatsUpdated() {
        if (this.doesGuiPauseGame) {
            this.initLists();
            this.initButtons();
            this.setDisplaySlot(this.displayId);
            this.doesGuiPauseGame = false;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.doesGuiPauseGame) {
            this.renderBackground(matrices);
            drawCenteredString(matrices, this.font, I18n.get("multiplayer.downloadingStats"), this.width / 2, this.height / 2, 0xff_ffff);
            drawCenteredString(matrices,
                               this.font,
                               LOADING_SYMBOLS[(int) (Util.getMillis() / 150L % LOADING_SYMBOLS.length)],
                               this.width / 2,
                               this.height / 2 + 9 * 2,
                               0xff_ffff);
        }
        else {
            this.displaySlot.render(matrices, mouseX, mouseY, partialTicks);
            drawCenteredString(matrices, this.font, this.title, this.width / 2, 20, 0xff_ffff);
            super.render(matrices, mouseX, mouseY, partialTicks);
        }
    }

    public void setDisplaySlot(int displayId) {
        this.children.remove(this.generalStats);
        this.children.remove(this.itemStats);
        this.children.remove(this.mobStats);
        this.children.remove(this.distanceStats);
        this.children.remove(this.timeStats);
        this.children.remove(this.deathStats);
        this.children.remove(this.damageStats);
        if (displayId != -1) {
            ExtendedList<?> displaySlot = this.byId(displayId);
            this.children.add(0, displaySlot);
            this.displaySlot = displaySlot;
            this.displayId = displayId;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListCustomStats extends ExtendedList<ListCustomStats.Entry> {

        public ListCustomStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 12);
            List<Stat<ResourceLocation>> list = new ArrayList<>();
            for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
                if (stat.getValue().getNamespace().equals(Evolution.MODID)) {
                    if (!stat.getValue().getPath().startsWith("distance_") &&
                        !stat.getValue().getPath().startsWith("time_") &&
                        !stat.getValue().getPath().startsWith("death_") &&
                        !stat.getValue().getPath().startsWith("damage_")) {
                        list.add(stat);
                    }
                }
            }
            list.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(getFormattedName(a), getFormattedName(b)));
            for (Stat<ResourceLocation> stat : list) {
                //noinspection ObjectAllocationInLoop
                this.addEntry(new Entry(stat));
            }
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
            ScreenStats.this.renderBackground(matrices);
        }

        @OnlyIn(Dist.CLIENT)
        final class Entry extends ExtendedList.AbstractListEntry<Entry> {
            private final String title;
            private final String value;

            private Entry(Stat<ResourceLocation> stat) {
                this.title = getFormattedName(stat);
                this.value = ((IEvoStatFormatter) FORMATTER.get(stat)).format(ScreenStats.this.stats.getValueLong(stat));
            }

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                drawString(matrices, ScreenStats.this.font, this.title, x + 2, y + 1, index % 2 == 0 ? 0xff_ffff : 0x75_7575);
                drawString(matrices,
                           ScreenStats.this.font,
                           this.value,
                           x + 2 + 213 - ScreenStats.this.font.width(this.value),
                           y + 1,
                           index % 2 == 0 ? 0xff_ffff : 0x75_7575);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListDamageStats extends ExtendedList<ScreenStats.ListDamageStats.Entry> {
        protected final Comparator<EvolutionDamage.Type> comparator = new ListComparator();
        protected final List<EvolutionDamage.Type> damageList;
        protected final List<Map<EvolutionDamage.Type, ResourceLocation>> damageStatList;
        private final int[] headerTexture = {1, 2, 3, 4, 5};
        protected int currentHeader = -1;
        protected int sortOrder;

        @Nullable
        protected Map<EvolutionDamage.Type, ResourceLocation> sorting;

        public ListDamageStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 20);
            this.damageStatList = Lists.newArrayList(EvolutionStats.DAMAGE_DEALT_RAW,
                                                     EvolutionStats.DAMAGE_DEALT_ACTUAL,
                                                     EvolutionStats.DAMAGE_TAKEN_BLOCKED,
                                                     EvolutionStats.DAMAGE_TAKEN_RAW,
                                                     EvolutionStats.DAMAGE_TAKEN_ACTUAL);
            this.setRenderHeader(true, 20);
            this.damageList = Lists.newArrayList(EvolutionDamage.ALL);
            for (int i = 0; i < this.damageList.size(); i++) {
                //noinspection ObjectAllocationInLoop
                this.addEntry(new ScreenStats.ListDamageStats.Entry());
            }
            this.damageList.sort(this.comparator);
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.currentHeader = -1;
            if (mouseY < this.headerHeight + this.y0 + 3) {
                for (int i = 0; i < this.headerTexture.length; i++) {
                    int j = mouseX - getCategoryOffset(i);
                    if (j >= -36 && j <= 0) {
                        this.currentHeader = i;
                        break;
                    }
                }
            }
            if (this.currentHeader >= 0) {
                this.sortBy(this.damageStatList.get(this.currentHeader));
                this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

        protected void drawName(MatrixStack matrices, @Nullable ITextComponent text, int mouseX, int mouseY) {
            if (text != null) {
                int i = mouseX + 12;
                int j = mouseY - 12;
                int k = ScreenStats.this.font.width(text);
                this.fillGradient(matrices, i - 3, j - 3, i + k + 3, j + 8 + 3, 0xc000_0000, 0xc000_0000);
                ScreenStats.this.font.draw(matrices, text, i, j, -1);
            }
        }

        @Override
        public int getRowWidth() {
            return 375;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 140;
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
            ScreenStats.this.renderBackground(matrices);
        }

        @Override
        protected void renderDecorations(MatrixStack matrices, int mouseX, int mouseY) {
            if (mouseY >= this.y0 && mouseY <= this.y1) {
                ScreenStats.ListDamageStats.Entry entryAtPos = this.getEntryAtPosition(mouseX, mouseY);
                int i = (this.width - this.getRowWidth()) / 2;
                if (entryAtPos != null) {
                    if (mouseX < i + 40 || mouseX > i + 40 + 20) {
                        return;
                    }
                    EvolutionDamage.Type type = this.damageList.get(this.children().indexOf(entryAtPos));
                    this.drawName(matrices, type.getTextComponent(), mouseX, mouseY);
                }
                else {
                    ITextComponent tooltip = null;
                    if (mouseY < this.headerHeight + this.y0 + 3) {
                        int j = mouseX - i;
                        for (int k = 0; k < this.headerTexture.length; ++k) {
                            int l = getCategoryOffset(k);
                            if (j >= l - 18 && j <= l) {
                                switch (k) {
                                    case 0: {
                                        tooltip = EvolutionTexts.GUI_STATS_DAMAGE_DEALT_RAW;
                                        break;
                                    }
                                    case 1: {
                                        tooltip = EvolutionTexts.GUI_STATS_DAMAGE_DEALT_ACTUAL;
                                        break;
                                    }
                                    case 2: {
                                        tooltip = EvolutionTexts.GUI_STATS_DAMAGE_TAKEN_BLOCKED;
                                        break;
                                    }
                                    case 3: {
                                        tooltip = EvolutionTexts.GUI_STATS_DAMAGE_TAKEN_RAW;
                                        break;
                                    }
                                    case 4: {
                                        tooltip = EvolutionTexts.GUI_STATS_DAMAGE_TAKEN_ACTUAL;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    this.drawName(matrices, tooltip, mouseX, mouseY);
                }
            }
        }

        @Override
        protected void renderHeader(MatrixStack matrices, int mouseX, int mouseY, Tessellator tessellator) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.currentHeader = -1;
            }
            for (int i = 0; i < this.headerTexture.length; ++i) {
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(i) - 18, mouseY + 1, 0, this.currentHeader == i ? 0 : 90);
            }
            if (this.sorting != null) {
                int k = getCategoryOffset(this.damageStatList.indexOf(this.sorting)) - 36;
                int j = this.sortOrder == 1 ? 2 : 1;
                ScreenStats.this.drawSprite(matrices, mouseX + k, mouseY + 1, 18 * j, 0);
            }
            for (int l = 0; l < this.headerTexture.length; ++l) {
                int i1 = this.currentHeader == l ? 1 : 0;
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(l) - 18 + i1, mouseY + 1 + i1, 18 * this.headerTexture[l], 90);
            }
        }

        protected void sortBy(Map<EvolutionDamage.Type, ResourceLocation> statType) {
            if (statType != this.sorting) {
                this.sorting = statType;
                this.sortOrder = -1;
            }
            else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            }
            else {
                this.sorting = null;
                this.sortOrder = 0;
            }
            this.damageList.sort(this.comparator);
        }

        @OnlyIn(Dist.CLIENT)
        final class ListComparator implements Comparator<EvolutionDamage.Type> {

            private ListComparator() {
            }

            @Override
            public int compare(EvolutionDamage.Type a, EvolutionDamage.Type b) {
                long i;
                long j;
                if (ScreenStats.ListDamageStats.this.sorting == null) {
                    i = 0;
                    j = 0;
                }
                else {
                    ResourceLocation aRes = ListDamageStats.this.sorting.get(a);
                    if (aRes != null) {
                        Stat<ResourceLocation> aStat = Stats.CUSTOM.get(aRes);
                        i = ScreenStats.this.stats.getValueLong(aStat);
                    }
                    else {
                        i = -1;
                    }
                    ResourceLocation bRes = ListDamageStats.this.sorting.get(b);
                    if (bRes != null) {
                        Stat<ResourceLocation> bStat = Stats.CUSTOM.get(bRes);
                        j = ScreenStats.this.stats.getValueLong(bStat);
                    }
                    else {
                        j = -1;
                    }
                }
                return i == j ?
                       String.CASE_INSENSITIVE_ORDER.compare(a.getTextComponent().getString(), b.getTextComponent().getString()) :
                       ListDamageStats.this.sortOrder * Long.compare(i, j);
            }
        }

        @OnlyIn(Dist.CLIENT)
        final class Entry extends ExtendedList.AbstractListEntry<ScreenStats.ListDamageStats.Entry> {

            private Entry() {
            }

            private void drawStatCount(MatrixStack matrices, @Nullable Stat<?> stat, int x, int y, boolean highlight) {
                String s = stat == null ? "-" : EvolutionStats.METRIC.format(ScreenStats.this.stats.getValueLong(stat));
                drawString(matrices, ScreenStats.this.font, s, x - ScreenStats.this.font.width(s), y + 5, highlight ? 0xff_ffff : 0x75_7575);
            }

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                EvolutionDamage.Type type = ListDamageStats.this.damageList.get(index);
                ScreenStats.this.drawDamageSprite(matrices, x + 40, y, type);
                for (int j = 0; j < ListDamageStats.this.damageStatList.size(); j++) {
                    Stat<?> stat = null;
                    ResourceLocation resLoc = ListDamageStats.this.damageStatList.get(j).get(type);
                    if (resLoc != null) {
                        stat = Stats.CUSTOM.get(resLoc);
                    }
                    this.drawStatCount(matrices, stat, x + getCategoryOffset(j), y, index % 2 == 0);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListDeathStats extends ExtendedList<ListDeathStats.Entry> {
        private final Comparator<Stat<ResourceLocation>> comparator = new ListDeathStats.ListComparator();
        private final List<Stat<ResourceLocation>> deathList;
        private final int[] headerTexture = {2};
        protected int currentHeader = -1;
        protected int sortOrder;

        public ListDeathStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 12);
            this.setRenderHeader(true, 20);
            this.deathList = new ArrayList<>();
            for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
                if (stat.getValue().getNamespace().equals(Evolution.MODID)) {
                    if (stat.getValue().getPath().startsWith("death_")) {
                        this.deathList.add(stat);
                        //noinspection ObjectAllocationInLoop
                        this.addEntry(new Entry());
                    }
                }
            }
            this.deathList.sort(this.comparator);
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.currentHeader = -1;
            if (mouseY < this.headerHeight + this.y0 + 3) {
                for (int i = 0; i < this.headerTexture.length; i++) {
                    int j = mouseX - getCategoryOffset(i) - 18;
                    if (j >= -36 && j <= 0) {
                        this.currentHeader = i;
                        break;
                    }
                }
            }
            if (this.currentHeader >= 0) {
                this.sort();
                this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 144;
        }

        @Override
        protected void renderHeader(MatrixStack matrices, int mouseX, int mouseY, Tessellator tessellator) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.currentHeader = -1;
            }
            for (int i = 0; i < this.headerTexture.length; ++i) {
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(i), mouseY + 1, 0, this.currentHeader == i ? 0 : 36);
            }
            if (this.sortOrder != 0) {
                int k = getCategoryOffset(0) - 18;
                int j = this.sortOrder == 1 ? 2 : 1;
                ScreenStats.this.drawSprite(matrices, mouseX + k, mouseY + 1, 18 * j, 0);
            }
            for (int l = 0; l < this.headerTexture.length; ++l) {
                int i1 = this.currentHeader == l ? 1 : 0;
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(l) + i1, mouseY + 1 + i1, 18 * this.headerTexture[l], 36);
            }
        }

        protected void sort() {
            switch (this.sortOrder) {
                case -1:
                    this.sortOrder = 1;
                    break;
                case 0:
                    this.sortOrder = -1;
                    break;
                case 1:
                    this.sortOrder = 0;
                    break;
            }
            this.deathList.sort(this.comparator);
        }

        @OnlyIn(Dist.CLIENT)
        final class ListComparator implements Comparator<Stat<ResourceLocation>> {

            @Override
            public int compare(Stat<ResourceLocation> a, Stat<ResourceLocation> b) {
                long i;
                long j;
                if (ListDeathStats.this.sortOrder == 0) {
                    i = 0;
                    j = 0;
                }
                else {
                    i = ScreenStats.this.stats.getValueLong(a);
                    j = ScreenStats.this.stats.getValueLong(b);
                }
                return i == j ?
                       String.CASE_INSENSITIVE_ORDER.compare(getFormattedName(a), getFormattedName(b)) :
                       ListDeathStats.this.sortOrder * Long.compare(i, j);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ExtendedList.AbstractListEntry<ListDeathStats.Entry> {

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                String name = getFormattedName(ListDeathStats.this.deathList.get(index));
                int color = index % 2 == 0 ? 0xff_ffff : 0x75_7575;
                drawString(matrices, ScreenStats.this.font, name, x + 2, y + 1, color);
                String value = EvolutionStats.DEFAULT.format(ScreenStats.this.stats.getValueLong(ListDeathStats.this.deathList.get(index)));
                drawString(matrices,
                           ScreenStats.this.font,
                           value,
                           x + 2 + ListDeathStats.this.getRowWidth() - 7 - ScreenStats.this.font.width(value),
                           y + 1,
                           color);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListTimeStats extends ExtendedList<ListTimeStats.Entry> {
        private final Comparator<Stat<ResourceLocation>> comparator = new ListTimeStats.ListComparator();
        private final int[] headerTexture = {1};
        private final List<Stat<ResourceLocation>> timeList;
        protected int currentHeader = -1;
        protected int sortOrder;

        public ListTimeStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 12);
            this.setRenderHeader(true, 20);
            this.timeList = new ArrayList<>();
            for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
                if (stat.getValue().getNamespace().equals(Evolution.MODID)) {
                    if (stat.getValue().getPath().startsWith("time_")) {
                        this.timeList.add(stat);
                        //noinspection ObjectAllocationInLoop
                        this.addEntry(new Entry());
                    }
                }
            }
            this.timeList.sort(this.comparator);
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.currentHeader = -1;
            if (mouseY < this.headerHeight + this.y0 + 3) {
                for (int i = 0; i < this.headerTexture.length; i++) {
                    int j = mouseX - getCategoryOffset(i);
                    if (j >= -36 && j <= 0) {
                        this.currentHeader = i;
                        break;
                    }
                }
            }
            if (this.currentHeader >= 0) {
                this.sort();
                this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

        protected String getFormattedName(Stat<ResourceLocation> stat) {
            return I18n.get("stat." + stat.getValue().toString().replace(':', '.'));
        }

        @Override
        protected void renderHeader(MatrixStack matrices, int mouseX, int mouseY, Tessellator tessellator) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.currentHeader = -1;
            }
            for (int i = 0; i < this.headerTexture.length; ++i) {
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(i) - 18, mouseY + 1, 0, this.currentHeader == i ? 0 : 72);
            }
            if (this.sortOrder != 0) {
                int k = getCategoryOffset(0) - 18 * 2;
                int j = this.sortOrder == 1 ? 2 : 1;
                ScreenStats.this.drawSprite(matrices, mouseX + k, mouseY + 1, 18 * j, 0);
            }
            for (int l = 0; l < this.headerTexture.length; ++l) {
                int i1 = this.currentHeader == l ? 1 : 0;
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(l) - 18 + i1, mouseY + 1 + i1, 18 * this.headerTexture[l], 72);
            }
        }

        protected void sort() {
            switch (this.sortOrder) {
                case -1:
                    this.sortOrder = 1;
                    break;
                case 0:
                    this.sortOrder = -1;
                    break;
                case 1:
                    this.sortOrder = 0;
                    break;
            }
            this.timeList.sort(this.comparator);
        }

        @OnlyIn(Dist.CLIENT)
        final class ListComparator implements Comparator<Stat<ResourceLocation>> {

            @Override
            public int compare(Stat<ResourceLocation> a, Stat<ResourceLocation> b) {
                long i;
                long j;
                if (ListTimeStats.this.sortOrder == 0) {
                    i = 0;
                    j = 0;
                }
                else {
                    i = ScreenStats.this.stats.getValueLong(a);
                    j = ScreenStats.this.stats.getValueLong(b);
                }
                return i == j ?
                       String.CASE_INSENSITIVE_ORDER.compare(ListTimeStats.this.getFormattedName(a), ListTimeStats.this.getFormattedName(b)) :
                       ListTimeStats.this.sortOrder * Long.compare(i, j);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ExtendedList.AbstractListEntry<ListTimeStats.Entry> {

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                String name = ListTimeStats.this.getFormattedName(ListTimeStats.this.timeList.get(index));
                int color = index % 2 == 0 ? 0xff_ffff : 0x75_7575;
                drawString(matrices, ScreenStats.this.font, name, x + 2, y + 1, color);
                String value = EvolutionStats.TIME.format(ScreenStats.this.stats.getValueLong(ListTimeStats.this.timeList.get(index)));
                drawString(matrices, ScreenStats.this.font, value, x + 2 + 213 - ScreenStats.this.font.width(value), y + 1, color);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListDistanceStats extends ExtendedList<ListDistanceStats.Entry> {

        private final Comparator<Stat<ResourceLocation>> comparator = new ListComparator();
        private final List<Stat<ResourceLocation>> distanceList;
        private final int[] headerTexture = {1};
        protected int currentHeader = -1;
        protected int sortOrder;

        public ListDistanceStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 12);
            this.setRenderHeader(true, 20);
            this.distanceList = new ArrayList<>();
            for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
                if (stat.getValue().getNamespace().equals(Evolution.MODID)) {
                    if (stat.getValue().getPath().startsWith("distance")) {
                        this.distanceList.add(stat);
                        //noinspection ObjectAllocationInLoop
                        this.addEntry(new Entry());
                    }
                }
            }
            this.distanceList.sort(this.comparator);
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.currentHeader = -1;
            if (mouseY < this.headerHeight + this.y0 + 3) {
                for (int i = 0; i < this.headerTexture.length; i++) {
                    int j = mouseX - getCategoryOffset(i);
                    if (j >= -36 && j <= 0) {
                        this.currentHeader = i;
                        break;
                    }
                }
            }
            if (this.currentHeader >= 0) {
                this.sort();
                this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

        protected String getFormattedName(Stat<ResourceLocation> stat) {
            return I18n.get("stat." + stat.getValue().toString().replace(':', '.'));
        }

        @Override
        protected void renderHeader(MatrixStack matrices, int mouseX, int mouseY, Tessellator tessellator) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.currentHeader = -1;
            }
            for (int i = 0; i < this.headerTexture.length; ++i) {
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(i) - 18, mouseY + 1, 0, this.currentHeader == i ? 0 : 54);
            }
            if (this.sortOrder != 0) {
                int k = getCategoryOffset(0) - 18 * 2;
                int j = this.sortOrder == 1 ? 2 : 1;
                ScreenStats.this.drawSprite(matrices, mouseX + k, mouseY + 1, 18 * j, 0);
            }
            for (int l = 0; l < this.headerTexture.length; ++l) {
                int i1 = this.currentHeader == l ? 1 : 0;
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(l) - 18 + i1, mouseY + 1 + i1, 18 * this.headerTexture[l], 54);
            }
        }

        protected void sort() {
            switch (this.sortOrder) {
                case -1:
                    this.sortOrder = 1;
                    break;
                case 0:
                    this.sortOrder = -1;
                    break;
                case 1:
                    this.sortOrder = 0;
                    break;
            }
            this.distanceList.sort(this.comparator);
        }

        @OnlyIn(Dist.CLIENT)
        final class ListComparator implements Comparator<Stat<ResourceLocation>> {

            @Override
            public int compare(Stat<ResourceLocation> a, Stat<ResourceLocation> b) {
                long i;
                long j;
                if (ListDistanceStats.this.sortOrder == 0) {
                    i = 0;
                    j = 0;
                }
                else {
                    i = ScreenStats.this.stats.getValueLong(a);
                    j = ScreenStats.this.stats.getValueLong(b);
                }
                return i == j ?
                       String.CASE_INSENSITIVE_ORDER.compare(ListDistanceStats.this.getFormattedName(a), ListDistanceStats.this.getFormattedName(b)) :
                       ListDistanceStats.this.sortOrder * Long.compare(i, j);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ExtendedList.AbstractListEntry<ScreenStats.ListDistanceStats.Entry> {

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                String name = ListDistanceStats.this.getFormattedName(ListDistanceStats.this.distanceList.get(index));
                int color = index % 2 == 0 ? 0xff_ffff : 0x75_7575;
                drawString(matrices, ScreenStats.this.font, name, x + 2, y + 1, color);
                String value = EvolutionStats.DISTANCE.format(ScreenStats.this.stats.getValueLong(ListDistanceStats.this.distanceList.get(index)));
                drawString(matrices, ScreenStats.this.font, value, x + 2 + 213 - ScreenStats.this.font.width(value), y + 1, color);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListMobStats extends ExtendedList<ScreenStats.ListMobStats.Entry> {

        protected final List<StatType<EntityType<?>>> statTypes;
        private final Comparator<EntityType<?>> comparator = new ListMobStats.ListComparator();
        private final Map<EntityType<?>, LivingEntity> entities = new IdentityHashMap<>();
        private final List<EntityType<?>> entityList;
        private final int[] headerTexture = {1, 2, 3, 4};
        protected int currentHeader = -1;
        private int sortOrder;
        @Nullable
        private StatType<?> sorting;

        public ListMobStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 9 * 6);
            this.statTypes = Lists.newArrayList(Stats.ENTITY_KILLED,
                                                Stats.ENTITY_KILLED_BY,
                                                EvolutionStats.DAMAGE_DEALT.get(),
                                                EvolutionStats.DAMAGE_TAKEN.get());
            this.setRenderHeader(true, 20);
            this.entityList = new ArrayList<>();
            for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
                if (this.shouldAddEntry(entityType)) {
                    this.entityList.add(entityType);
                    this.entities.put(entityType, GUIUtils.getEntity(ScreenStats.this.minecraft.level, entityType));
                    //noinspection ObjectAllocationInLoop
                    this.addEntry(new ScreenStats.ListMobStats.Entry());
                }
            }
            this.entityList.sort(this.comparator);
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.currentHeader = -1;
            if (mouseY < this.headerHeight + this.y0 + 3) {
                for (int i = 0; i < this.headerTexture.length; i++) {
                    int j = mouseX - getCategoryOffset(i) + 18 * 3;
                    if (j >= -36 && j <= 0) {
                        this.currentHeader = i;
                        break;
                    }
                }
            }
            if (this.currentHeader >= 0) {
                this.sortBy(this.statTypes.get(this.currentHeader));
                this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

        protected void drawName(MatrixStack matrices, @Nullable ITextComponent text, int mouseX, int mouseY) {
            if (text != null) {
                int i = mouseX + 12;
                int j = mouseY - 12;
                int k = ScreenStats.this.font.width(text);
                this.fillGradient(matrices, i - 3, j - 3, i + k + 3, j + 8 + 3, 0xc000_0000, 0xc000_0000);
                ScreenStats.this.font.draw(matrices, text, i, j, -1);
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 180;
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
            ScreenStats.this.renderBackground(matrices);
        }

        @Override
        protected void renderDecorations(MatrixStack matrices, int mouseX, int mouseY) {
            if (mouseY >= this.y0 && mouseY <= this.y1) {
                ScreenStats.ListMobStats.Entry entryAtPos = this.getEntryAtPosition(mouseX, mouseY);
                if (entryAtPos == null) {
                    int i = (this.width - this.getRowWidth()) / 2;
                    ITextComponent text = null;
                    if (mouseY < this.headerHeight + this.y0 + 3) {
                        int j = mouseX - i;
                        for (int k = 0; k < this.headerTexture.length; ++k) {
                            int l = getCategoryOffset(k) - 18 * 3;
                            if (j >= l - 18 && j <= l) {
                                text = new TranslationTextComponent(this.statTypes.get(k).getTranslationKey() + ".name");
                                break;
                            }
                        }
                    }
                    this.drawName(matrices, text, mouseX, mouseY);
                }
            }
        }

        @Override
        protected void renderHeader(MatrixStack matrices, int mouseX, int mouseY, Tessellator tessellator) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.currentHeader = -1;
            }
            for (int i = 0; i < this.headerTexture.length; ++i) {
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(i) - 18 * 4, mouseY + 1, 0, this.currentHeader == i ? 0 : 36);
            }
            if (this.sorting != null) {
                int k = getCategoryOffset(this.statTypes.indexOf(this.sorting)) - 18 * 5;
                int j = this.sortOrder == 1 ? 2 : 1;
                ScreenStats.this.drawSprite(matrices, mouseX + k, mouseY + 1, 18 * j, 0);
            }
            for (int l = 0; l < this.headerTexture.length; ++l) {
                int i1 = this.currentHeader == l ? 1 : 0;
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(l) - 18 * 4 + i1, mouseY + 1 + i1, 18 * this.headerTexture[l], 36);
            }
        }

        private boolean shouldAddEntry(EntityType<?> type) {
            if (ScreenStats.this.stats.getValueLong(Stats.ENTITY_KILLED.get(type)) > 0) {
                return true;
            }
            if (ScreenStats.this.stats.getValueLong(Stats.ENTITY_KILLED_BY.get(type)) > 0) {
                return true;
            }
            if (ScreenStats.this.stats.getValueLong(EvolutionStats.DAMAGE_DEALT.get().get(type)) > 0) {
                return true;
            }
            return ScreenStats.this.stats.getValueLong(EvolutionStats.DAMAGE_TAKEN.get().get(type)) > 0;
        }

        protected void sortBy(StatType<?> statType) {
            if (statType != this.sorting) {
                this.sorting = statType;
                this.sortOrder = -1;
            }
            else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            }
            else {
                this.sorting = null;
                this.sortOrder = 0;
            }
            this.entityList.sort(this.comparator);
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ExtendedList.AbstractListEntry<ScreenStats.ListMobStats.Entry> {

            public Entry() {
            }

            private String getDmgDealtValue(String plural, long dmgDealt) {
                String s = EvolutionStats.DAMAGE_DEALT.get().getTranslationKey();
                return dmgDealt == 0 ? I18n.get(s + ".none", plural) : I18n.get(s, EvolutionStats.DAMAGE.format(dmgDealt), plural);
            }

            private String getDmgTakenValue(String plural, long dmgTaken) {
                String s = EvolutionStats.DAMAGE_TAKEN.get().getTranslationKey();
                return dmgTaken == 0 ? I18n.get(s + ".none", plural) : I18n.get(s, EvolutionStats.DAMAGE.format(dmgTaken), plural);
            }

            private String getKilledByValue(String plural, long killedBy) {
                String s = Stats.ENTITY_KILLED_BY.getTranslationKey();
                if (killedBy == 0) {
                    return I18n.get(s + ".none", plural);
                }
                return killedBy > 1 ? I18n.get(s, plural, killedBy) : I18n.get(s + ".once", plural);
            }

            private String getKilledValue(String singular, String plural, long killed) {
                String s = Stats.ENTITY_KILLED.getTranslationKey();
                if (killed == 0) {
                    return I18n.get(s + ".none", plural);
                }
                return killed > 1 ? I18n.get(s, killed, plural) : I18n.get(s + ".once", singular);
            }

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                EntityType<?> type = ListMobStats.this.entityList.get(index);
                LivingEntity entity = ListMobStats.this.entities.get(type);
                if (type == EntityType.PLAYER) {
                    entity = ListMobStats.this.minecraft.player;
                }
                GUIUtils.drawEntityOnScreen(x - 30, y + 45, GUIUtils.getEntityScale(entity, 1.0f, 40, 35), mouseX, mouseY, entity);
                long dmgDealt = ScreenStats.this.stats.getValueLong(EvolutionStats.DAMAGE_DEALT.get(), type);
                long dmgTaken = ScreenStats.this.stats.getValueLong(EvolutionStats.DAMAGE_TAKEN.get(), type);
                long killed = ScreenStats.this.stats.getValueLong(Stats.ENTITY_KILLED, type);
                long killedBy = ScreenStats.this.stats.getValueLong(Stats.ENTITY_KILLED_BY, type);
                String entityName = I18n.get(Util.makeDescriptionId("entity", EntityType.getKey(type)));
                String entityNamePlural = I18n.get(Util.makeDescriptionId("entity", EntityType.getKey(type)) + ".plural");
                drawString(matrices, ScreenStats.this.font, entityName, x + 2, y + 1, 0xff_ffff);
                int dmgColor = dmgDealt == dmgTaken ? dmgDealt == 0 ? 0x75_7575 : 0xc4_ad00 : dmgDealt > dmgTaken ? 0x33_b500 : 0xff_3030;
                drawString(matrices, ScreenStats.this.font, this.getDmgDealtValue(entityNamePlural, dmgDealt), x + 2 + 10, y + 1 + 9, dmgColor);
                drawString(matrices, ScreenStats.this.font, this.getDmgTakenValue(entityNamePlural, dmgTaken), x + 2 + 10, y + 1 + 9 * 2, dmgColor);
                int killColor = killed == killedBy ? killed == 0 ? 0x75_7575 : 0xc4_ad00 : killed > killedBy ? 0x33_b500 : 0xff_3030;
                drawString(matrices,
                           ScreenStats.this.font,
                           this.getKilledValue(entityName, entityNamePlural, killed),
                           x + 2 + 10,
                           y + 1 + 9 * 3,
                           killColor);
                drawString(matrices, ScreenStats.this.font, this.getKilledByValue(entityNamePlural, killedBy), x + 2 + 10, y + 1 + 9 * 4, killColor);
            }
        }

        @OnlyIn(Dist.CLIENT)
        final class ListComparator implements Comparator<EntityType<?>> {

            private ListComparator() {
            }

            @Override
            public int compare(EntityType<?> a, EntityType<?> b) {
                long i;
                long j;
                if (ScreenStats.ListMobStats.this.sorting == null) {
                    i = 0;
                    j = 0;
                }
                else {
                    StatType<EntityType<?>> entitySorting = (StatType<EntityType<?>>) ScreenStats.ListMobStats.this.sorting;
                    i = ScreenStats.this.stats.getValueLong(entitySorting, a);
                    j = ScreenStats.this.stats.getValueLong(entitySorting, b);
                }
                return i == j ?
                       String.CASE_INSENSITIVE_ORDER.compare(a.getDescription().getString(), b.getDescription().getString()) :
                       ListMobStats.this.sortOrder * Long.compare(i, j);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ListStats extends ExtendedList<ScreenStats.ListStats.Entry> {
        protected final List<StatType<Block>> blockStatList;
        protected final Comparator<Item> comparator = new ListComparator();
        protected final List<Item> itemList;
        protected final List<StatType<Item>> itemStatList;
        private final int[] headerTexture = {3, 4, 1, 2, 5, 6};
        protected int currentHeader = -1;
        protected int sortOrder;
        @Nullable
        protected StatType<?> sorting;

        public ListStats(Minecraft mc) {
            super(mc, ScreenStats.this.width, ScreenStats.this.height, 32, ScreenStats.this.height - 64, 20);
            this.blockStatList = Lists.newArrayList();
            this.blockStatList.add(Stats.BLOCK_MINED);
            this.itemStatList = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            this.setRenderHeader(true, 20);
            Set<Item> set = Sets.newIdentityHashSet();
            allItemInRegistry:
            for (Item item : Registry.ITEM) {
                for (StatType<Item> statType : this.itemStatList) {
                    if (statType.contains(item) && ScreenStats.this.stats.getValueLong(statType.get(item)) > 0) {
                        set.add(item);
                        continue allItemInRegistry;
                    }
                }
            }
            allBlockInRegistry:
            for (Block block : Registry.BLOCK) {
                for (StatType<Block> statType : this.blockStatList) {
                    if (statType.contains(block) && ScreenStats.this.stats.getValueLong(statType.get(block)) > 0) {
                        set.add(block.asItem());
                        continue allBlockInRegistry;
                    }
                }
            }
            set.remove(Items.AIR);
            this.itemList = Lists.newArrayList(set);
            for (int i = 0; i < this.itemList.size(); i++) {
                //noinspection ObjectAllocationInLoop
                this.addEntry(new ScreenStats.ListStats.Entry());
            }
            this.itemList.sort(this.comparator);
        }

        private StatType<?> byIndex(int index) {
            return index < this.blockStatList.size() ? this.blockStatList.get(index) : this.itemStatList.get(index - this.blockStatList.size());
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.currentHeader = -1;
            if (mouseY < this.headerHeight + this.y0 + 3) {
                for (int i = 0; i < this.headerTexture.length; i++) {
                    int j = mouseX - getCategoryOffset(i);
                    if (j >= -36 && j <= 0) {
                        this.currentHeader = i;
                        break;
                    }
                }
            }
            if (this.currentHeader >= 0) {
                this.sortBy(this.byIndex(this.currentHeader));
                this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

        protected void drawName(MatrixStack matrices, @Nullable ITextComponent text, int mouseX, int mouseY) {
            if (text != null) {
                int i = mouseX + 12;
                int j = mouseY - 12;
                int k = ScreenStats.this.font.width(text);
                this.fillGradient(matrices, i - 3, j - 3, i + k + 3, j + 8 + 3, 0xc000_0000, 0xc000_0000);
                ScreenStats.this.font.draw(matrices, text, i, j, -1);
            }
        }

        @Override
        public int getRowWidth() {
            return 375;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 140;
        }

        private int indexOf(StatType<?> statType) {
            int blockIndex = this.blockStatList.indexOf(statType);
            if (blockIndex >= 0) {
                return blockIndex;
            }
            int itemIndex = this.itemStatList.indexOf(statType);
            return itemIndex >= 0 ? itemIndex + this.blockStatList.size() : -1;
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
            ScreenStats.this.renderBackground(matrices);
        }

        @Override
        protected void renderDecorations(MatrixStack matrices, int mouseX, int mouseY) {
            if (mouseY >= this.y0 && mouseY <= this.y1) {
                ScreenStats.ListStats.Entry entryAtPos = this.getEntryAtPosition(mouseX, mouseY);
                int i = (this.width - this.getRowWidth()) / 2;
                if (entryAtPos != null) {
                    if (mouseX < i + 40 || mouseX > i + 40 + 20) {
                        return;
                    }
                    Item item = this.itemList.get(this.children().indexOf(entryAtPos));
                    this.drawName(matrices, item.getDescription(), mouseX, mouseY);
                }
                else {
                    ITextComponent itextcomponent = null;
                    if (mouseY < this.headerHeight + this.y0 + 3) {
                        int j = mouseX - i;
                        for (int k = 0; k < this.headerTexture.length; ++k) {
                            int l = getCategoryOffset(k);
                            if (j >= l - 18 && j <= l) {
                                itextcomponent = new TranslationTextComponent(this.byIndex(k).getTranslationKey());
                                break;
                            }
                        }
                    }
                    this.drawName(matrices, itextcomponent, mouseX, mouseY);
                }
            }
        }

        @Override
        protected void renderHeader(MatrixStack matrices, int mouseX, int mouseY, Tessellator tessellator) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.currentHeader = -1;
            }
            for (int i = 0; i < this.headerTexture.length; ++i) {
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(i) - 18, mouseY + 1, 0, this.currentHeader == i ? 0 : 18);
            }
            if (this.sorting != null) {
                int k = getCategoryOffset(this.indexOf(this.sorting)) - 36;
                int j = this.sortOrder == 1 ? 2 : 1;
                ScreenStats.this.drawSprite(matrices, mouseX + k, mouseY + 1, 18 * j, 0);
            }
            for (int l = 0; l < this.headerTexture.length; ++l) {
                int i1 = this.currentHeader == l ? 1 : 0;
                ScreenStats.this.drawSprite(matrices, mouseX + getCategoryOffset(l) - 18 + i1, mouseY + 1 + i1, 18 * this.headerTexture[l], 18);
            }
        }

        protected void sortBy(StatType<?> statType) {
            if (statType != this.sorting) {
                this.sorting = statType;
                this.sortOrder = -1;
            }
            else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            }
            else {
                this.sorting = null;
                this.sortOrder = 0;
            }
            this.itemList.sort(this.comparator);
        }

        @OnlyIn(Dist.CLIENT)
        final class ListComparator implements Comparator<Item> {

            private ListComparator() {
            }

            @Override
            public int compare(Item a, Item b) {
                long i;
                long j;
                if (ScreenStats.ListStats.this.sorting == null) {
                    i = 0;
                    j = 0;
                }
                else if (ScreenStats.ListStats.this.blockStatList.contains(ScreenStats.ListStats.this.sorting)) {
                    StatType<Block> blockSorting = (StatType<Block>) ScreenStats.ListStats.this.sorting;
                    i = a instanceof BlockItem ? ScreenStats.this.stats.getValueLong(blockSorting, ((BlockItem) a).getBlock()) : -1;
                    j = b instanceof BlockItem ? ScreenStats.this.stats.getValueLong(blockSorting, ((BlockItem) b).getBlock()) : -1;
                }
                else {
                    StatType<Item> itemSorting = (StatType<Item>) ScreenStats.ListStats.this.sorting;
                    i = ScreenStats.this.stats.getValueLong(itemSorting, a);
                    j = ScreenStats.this.stats.getValueLong(itemSorting, b);
                }
                return i == j ?
                       String.CASE_INSENSITIVE_ORDER.compare(a.getDescription().getString(), b.getDescription().getString()) :
                       ListStats.this.sortOrder * Long.compare(i, j);
            }
        }

        @OnlyIn(Dist.CLIENT)
        final class Entry extends ExtendedList.AbstractListEntry<ScreenStats.ListStats.Entry> {

            private Entry() {
            }

            private void drawStatCount(MatrixStack matrices, @Nullable Stat<?> stat, int x, int y, boolean highlight) {
                String s = stat == null ? "-" : EvolutionStats.METRIC.format(ScreenStats.this.stats.getValueLong(stat));
                drawString(matrices, ScreenStats.this.font, s, x - ScreenStats.this.font.width(s), y + 5, highlight ? 0xff_ffff : 0x75_7575);
            }

            @Override
            public void render(MatrixStack matrices,
                               int index,
                               int y,
                               int x,
                               int width,
                               int height,
                               int mouseX,
                               int mouseY,
                               boolean hovered,
                               float partialTicks) {
                Item item = ScreenStats.this.itemStats.itemList.get(index);
                ScreenStats.this.drawStatsScreen(matrices, x + 40, y, item);
                for (int i = 0; i < ScreenStats.this.itemStats.blockStatList.size(); ++i) {
                    Stat<Block> stat;
                    if (item instanceof BlockItem) {
                        stat = ScreenStats.this.itemStats.blockStatList.get(i).get(((BlockItem) item).getBlock());
                    }
                    else {
                        stat = null;
                    }
                    this.drawStatCount(matrices, stat, x + getCategoryOffset(i), y, index % 2 == 0);
                }
                for (int j = 0; j < ScreenStats.this.itemStats.itemStatList.size(); ++j) {
                    this.drawStatCount(matrices,
                                       ScreenStats.this.itemStats.itemStatList.get(j).get(item),
                                       x + getCategoryOffset(j + ScreenStats.this.itemStats.blockStatList.size()),
                                       y,
                                       index % 2 == 0);
                }
            }
        }
    }
}
