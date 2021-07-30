package tgw.evolution.client.gui.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.init.EvolutionTexts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CriterionGrid {
    private static final CriterionGrid EMPTY = new CriterionGrid();
    public static CriteriaDetail detailLevel = CriteriaDetail.DEFAULT;
    public static boolean requiresShift;
    public final int numRows;
    private final List<String> cellContents;
    private final int[] cellWidths;
    private final int fontHeight;
    private final int numColumns;
    public List<Column> columns;
    public int height;
    public int width;

    private CriterionGrid() {
        this.cellContents = Collections.emptyList();
        this.cellWidths = new int[0];
        this.fontHeight = 0;
        this.numColumns = 0;
        this.numRows = 0;
        this.columns = Collections.emptyList();
        this.width = 0;
        this.height = 0;
    }

    public CriterionGrid(List<String> cellContents, int[] cellWidths, int fontHeight, int numColumns) {
        this.cellContents = cellContents;
        this.cellWidths = cellWidths;
        this.fontHeight = fontHeight;
        this.numColumns = numColumns;
        this.numRows = (int) Math.ceil((double) cellContents.size() / numColumns);
    }

    // Of all the possible grids whose aspect ratio is less than the maximum, this method returns the one with the smallest number of rows.
    // If there is no such grid, this method returns a single-column grid.
    public static CriterionGrid findOptimalCriterionGrid(Advancement advancement, AdvancementProgress progress, int maxWidth, FontRenderer renderer) {
        if (progress == null || detailLevel == CriteriaDetail.OFF) {
            return EMPTY;
        }
        Map<String, Criterion> criteria = advancement.getCriteria();
        if (criteria.size() <= 1) {
            return EMPTY;
        }
        int numUnobtained = advancement.getRequirementCount();
        List<String> cellContents = new ArrayList<>();
        for (String criterion : criteria.keySet()) {
            if (progress.getCriterionProgress(criterion).isObtained()) {
                if (detailLevel.showObtained()) {
                    //noinspection ObjectAllocationInLoop
                    ITextComponent text = new StringTextComponent(" + ");
                    text.getStyle().setColor(TextFormatting.GREEN);
                    ITextComponent text2 = getCriteriaTranslated(criterion);
                    text2.getStyle().setColor(TextFormatting.WHITE);
                    text.appendSibling(text2);
                    cellContents.add(text.getFormattedText());
                }
                numUnobtained--;
            }
            else {
                if (detailLevel.showUnobtained()) {
                    //noinspection ObjectAllocationInLoop
                    ITextComponent text = new StringTextComponent(" x ");
                    text.getStyle().setColor(TextFormatting.DARK_RED);
                    ITextComponent text2 = getCriteriaTranslated(criterion);
                    text2.getStyle().setColor(TextFormatting.WHITE);
                    text.appendSibling(text2);
                    cellContents.add(text.getFormattedText());
                }
            }
        }
        if (!detailLevel.showUnobtained() && numUnobtained > 0) {
            ITextComponent text = new StringTextComponent(" x ");
            text.getStyle().setColor(TextFormatting.DARK_RED);
            ITextComponent text2 = EvolutionTexts.remaining(numUnobtained);
            text2.getStyle().setColor(TextFormatting.WHITE);
            text2.getStyle().setItalic(true);
            text.appendSibling(text2);
            cellContents.add(text.getFormattedText());
        }
        int[] cellWidths = new int[cellContents.size()];
        for (int i = 0; i < cellWidths.length; i++) {
            cellWidths[i] = renderer.getStringWidth(cellContents.get(i));
        }
        int numCols = 0;
        CriterionGrid prevGrid = null;
        CriterionGrid currGrid = null;
        do {
            numCols++;
            //noinspection ObjectAllocationInLoop
            CriterionGrid newGrid = new CriterionGrid(cellContents, cellWidths, renderer.FONT_HEIGHT, numCols);
            if (prevGrid != null && newGrid.numRows == prevGrid.numRows) {
                // We increased the width without decreasing the height, which is pointless.
                continue;
            }
            newGrid.init();
            prevGrid = currGrid;
            currGrid = newGrid;
        } while (numCols <= cellContents.size() && currGrid.width <= maxWidth);
        return prevGrid != null ? prevGrid : currGrid;
    }

    private static ITextComponent getCriteriaTranslated(String criterion) {
        int endIndex = criterion.indexOf(':');
        String type = criterion.substring(0, endIndex == -1 ? 0 : endIndex);
        if (type.isEmpty()) {
            return new StringTextComponent(criterion);
        }
        switch (type) {
            case "item":
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(criterion.substring(criterion.indexOf(':') + 1)));
                return new TranslationTextComponent(item.getTranslationKey());
            case "entity":
                EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(criterion.substring(criterion.indexOf(':') + 1)));
                return entity.getName();
            case "biome":
                Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(criterion.substring(criterion.indexOf(':') + 1)));
                return biome.getDisplayName();
            case "minecraft":
                return new StringTextComponent(criterion);
        }
        throw new IllegalStateException("Unknown type: " + type);
    }

    public void init() {
        this.columns = new ArrayList<>();
        this.width = 0;
        for (int c = 0; c < this.numColumns; c++) {
            //noinspection ObjectAllocationInLoop
            List<String> column = new ArrayList<>();
            int columnWidth = 0;
            for (int r = 0; r < this.numRows; r++) {
                int cellIndex = c * this.numRows + r;
                if (cellIndex >= this.cellContents.size()) {
                    break;
                }
                String str = this.cellContents.get(cellIndex);
                column.add(str);
                columnWidth = Math.max(columnWidth, this.cellWidths[cellIndex]);
            }
            //noinspection ObjectAllocationInLoop
            this.columns.add(new Column(column, columnWidth));
            this.width += columnWidth;
        }
        this.height = this.numRows * this.fontHeight;
    }

    public static class Column {
        public final List<String> cells;
        public final int width;

        public Column(List<String> cells, int width) {
            this.cells = cells;
            this.width = width;
        }
    }
}
