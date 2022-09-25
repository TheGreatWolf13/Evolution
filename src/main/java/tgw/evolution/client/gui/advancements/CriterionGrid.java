package tgw.evolution.client.gui.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CriterionGrid {
    private static final CriterionGrid EMPTY = new CriterionGrid();
    public final int numRows;
    private final List<MutableComponent> cellContents;
    private final int[] cellWidths;
    private final int fontHeight;
    private final int numColumns;
    public OList<Column> columns;
    public int height;
    public int width;

    private CriterionGrid() {
        this.cellContents = Collections.emptyList();
        this.cellWidths = new int[0];
        this.fontHeight = 0;
        this.numColumns = 0;
        this.numRows = 0;
        this.columns = new OArrayList<>();
        this.width = 0;
        this.height = 0;
    }

    public CriterionGrid(List<MutableComponent> cellContents, int[] cellWidths, int fontHeight, int numColumns) {
        this.cellContents = cellContents;
        this.cellWidths = cellWidths;
        this.fontHeight = fontHeight;
        this.numColumns = numColumns;
        this.numRows = (int) Math.ceil((double) cellContents.size() / numColumns);
    }

    // Of all the possible grids whose aspect ratio is less than the maximum, this method returns the one with the smallest number of rows.
    // If there is no such grid, this method returns a single-column grid.
    public static CriterionGrid findOptimalCriterionGrid(Advancement advancement, AdvancementProgress progress, int maxWidth, Font font) {
        if (progress == null) {
            return EMPTY;
        }
        Map<String, Criterion> criteria = advancement.getCriteria();
        if (criteria.size() <= 1) {
            return EMPTY;
        }
        int numUnobtained = advancement.getMaxCriteraRequired();
        OList<MutableComponent> cellContents = new OArrayList<>();
        for (String criterion : criteria.keySet()) {
            if (progress.getCriterion(criterion).isDone()) {
                //noinspection ObjectAllocationInLoop
                MutableComponent text = new TextComponent(" + ").withStyle(ChatFormatting.GREEN);
                MutableComponent text2 = getCriteriaTranslated(criterion).withStyle(ChatFormatting.WHITE);
                text.append(text2);
                cellContents.add(text);
                numUnobtained--;
            }
        }
        if (numUnobtained > 0) {
            MutableComponent text = new TextComponent(" x ").withStyle(ChatFormatting.RED);
            MutableComponent text2 = EvolutionTexts.remaining(numUnobtained).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.ITALIC);
            text.append(text2);
            cellContents.add(text);
        }
        int[] cellWidths = new int[cellContents.size()];
        for (int i = 0; i < cellWidths.length; i++) {
            cellWidths[i] = font.width(cellContents.get(i));
        }
        int numCols = 0;
        CriterionGrid prevGrid = null;
        CriterionGrid currGrid = null;
        do {
            numCols++;
            //noinspection ObjectAllocationInLoop
            CriterionGrid newGrid = new CriterionGrid(cellContents, cellWidths, font.lineHeight, numCols);
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

    private static MutableComponent getCriteriaTranslated(String criterion) {
        int endIndex = criterion.indexOf(':');
        String type = criterion.substring(0, endIndex == -1 ? 0 : endIndex);
        if (type.isEmpty()) {
            return new TextComponent(criterion);
        }
        switch (type) {
            case "item":
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(criterion.substring(criterion.indexOf(':') + 1)));
                return new TranslatableComponent(item.getDescriptionId());
            case "entity":
                EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(criterion.substring(criterion.indexOf(':') + 1)));
                return (MutableComponent) entity.getDescription();
            case "biome":
                Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(criterion.substring(criterion.indexOf(':') + 1)));
                return new TranslatableComponent(Util.makeDescriptionId("biome", biome.getRegistryName()));
            case "minecraft":
                return new TextComponent(criterion);
        }
        throw new IllegalStateException("Unknown type: " + type);
    }

    public void init() {
        this.columns = new OArrayList<>();
        this.width = 0;
        for (int c = 0; c < this.numColumns; c++) {
            //noinspection ObjectAllocationInLoop
            OList<MutableComponent> column = new OArrayList<>();
            int columnWidth = 0;
            for (int r = 0; r < this.numRows; r++) {
                int cellIndex = c * this.numRows + r;
                if (cellIndex >= this.cellContents.size()) {
                    break;
                }
                MutableComponent text = this.cellContents.get(cellIndex);
                column.add(text);
                columnWidth = Math.max(columnWidth, this.cellWidths[cellIndex]);
            }
            //noinspection ObjectAllocationInLoop
            this.columns.add(new Column(column, columnWidth));
            this.width += columnWidth;
        }
        this.height = this.numRows * (this.fontHeight + 1);
    }

    public record Column(List<MutableComponent> cells, int width) {
    }
}
