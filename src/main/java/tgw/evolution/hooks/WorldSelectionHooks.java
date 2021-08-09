package tgw.evolution.hooks;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.WorldSelectionList;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import org.apache.commons.lang3.StringUtils;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Metric;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class WorldSelectionHooks {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    private WorldSelectionHooks() {
    }

    /**
     * Hooks from {@link SaveFormat#getSaveList()}, replacing to method.
     */
    @EvolutionHook
    public static List<WorldSummary> getSaveList(SaveFormat save) throws AnvilConverterException {
        if (!Files.isDirectory(save.func_215781_c())) {
            throw new AnvilConverterException(new TranslationTextComponent("selectWorld.load_folder_access").getString());
        }
        List<WorldSummary> list = Lists.newArrayList();
        File[] potentialWorlds = save.func_215781_c().toFile().listFiles();
        for (File worldFolder : potentialWorlds) {
            if (worldFolder.isDirectory()) {
                String folderName = worldFolder.getName();
                WorldInfo worldInfo = save.getWorldInfo(folderName);
                if (worldInfo != null && (worldInfo.getSaveVersion() == 19_132 || worldInfo.getSaveVersion() == 19_133)) {
                    boolean requiresConversion = worldInfo.getSaveVersion() != 19_133;
                    String displayName = worldInfo.getWorldName();
                    if (StringUtils.isEmpty(displayName)) {
                        displayName = folderName;
                    }
                    //noinspection ObjectAllocationInLoop
                    list.add(new WorldSummary(worldInfo,
                                              folderName,
                                              displayName,
                                              MathHelper.calculateSizeOnDisk(worldFolder.toPath()),
                                              requiresConversion));
                }
            }
        }
        return list;
    }

    /**
     * Hooks from {@link WorldSelectionList.Entry#render(int, int, int, int, int, int, int, boolean, float)}, replacing the method.
     */
    @EvolutionHook
    public static void render(WorldSelectionList.Entry entry,
                              WorldSummary summary,
                              DynamicTexture texture,
                              ResourceLocation icon,
                              WorldSelectionScreen screen,
                              int index,
                              int rowY,
                              int rowX,
                              int width,
                              int height,
                              int mouseX,
                              int mouseY,
                              boolean isMouseOver,
                              float partialTicks) {
        String firstLine = summary.getDisplayName();
        String secondLine = summary.getFileName() + " (" + DATE_FORMAT.format(new Date(summary.getLastTimePlayed())) + ")";
        if (StringUtils.isEmpty(firstLine)) {
            firstLine = I18n.format("selectWorld.world") + " " + (index + 1);
        }
        String thirdLine = "";
        if (summary.requiresConversion()) {
            thirdLine = I18n.format("selectWorld.conversion") + " " + thirdLine;
        }
        else {
            thirdLine = I18n.format("gameMode." + summary.getEnumGameType().getName());
            if (summary.isHardcoreModeEnabled()) {
                thirdLine = TextFormatting.DARK_RED + I18n.format("gameMode.hardcore") + TextFormatting.RESET;
            }
            if (summary.getCheatsEnabled()) {
                thirdLine = thirdLine + ", " + I18n.format("selectWorld.cheats");
            }
            String version = summary.getVersionName().getFormattedText();
            if (summary.markVersionInList()) {
                if (summary.askToOpenWorld()) {
                    thirdLine = thirdLine + ", " + I18n.format("selectWorld.version") + " " + TextFormatting.RED + version + TextFormatting.RESET;
                }
                else {
                    thirdLine = thirdLine + ", " + I18n.format("selectWorld.version") + " " + TextFormatting.ITALIC + version + TextFormatting.RESET;
                }
            }
            else {
                thirdLine = thirdLine + ", " + I18n.format("selectWorld.version") + " " + version;
            }
            thirdLine = thirdLine + " (" + Metric.bytes(summary.getSizeOnDisk(), 1) + ")";
        }
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        fontRenderer.drawString(firstLine, rowX + 32 + 3, rowY + 1, 16_777_215);
        fontRenderer.drawString(secondLine, rowX + 32 + 3, rowY + 9 + 3, 8_421_504);
        fontRenderer.drawString(thirdLine, rowX + 32 + 3, rowY + 9 + 9 + 3, 8_421_504);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //noinspection VariableNotUsedInsideIf
        Minecraft.getInstance().getTextureManager().bindTexture(texture != null ? icon : EvolutionResources.UNKNOWN_SERVER);
        GlStateManager.enableBlend();
        AbstractGui.blit(rowX, rowY, 0.0F, 0.0F, 32, 32, 32, 32);
        GlStateManager.disableBlend();
        if (Minecraft.getInstance().gameSettings.touchscreen || isMouseOver) {
            Minecraft.getInstance().getTextureManager().bindTexture(EvolutionResources.GUI_WORLD_SELECTION);
            AbstractGui.fill(rowX, rowY, rowX + 32, rowY + 32, -1_601_138_544);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int j = mouseX - rowX;
            int i = j < 32 ? 32 : 0;
            if (summary.markVersionInList()) {
                AbstractGui.blit(rowX, rowY, 32.0F, i, 32, 32, 256, 256);
                if (summary.func_202842_n()) {
                    AbstractGui.blit(rowX, rowY, 96.0F, i, 32, 32, 256, 256);
                    if (j < 32) {
                        ITextComponent unsupportedText = new TranslationTextComponent("selectWorld.tooltip.unsupported",
                                                                                      summary.getVersionName()).applyTextStyle(TextFormatting.RED);
                        screen.setVersionTooltip(fontRenderer.wrapFormattedStringToWidth(unsupportedText.getFormattedText(), 175));
                    }
                }
                else if (summary.askToOpenWorld()) {
                    AbstractGui.blit(rowX, rowY, 96.0F, i, 32, 32, 256, 256);
                    if (j < 32) {
                        screen.setVersionTooltip(TextFormatting.RED +
                                                 I18n.format("selectWorld.tooltip.fromNewerVersion1") +
                                                 "\n" +
                                                 TextFormatting.RED +
                                                 I18n.format("selectWorld.tooltip.fromNewerVersion2"));
                    }
                }
                else if (!SharedConstants.getVersion().isStable()) {
                    AbstractGui.blit(rowX, rowY, 64.0F, i, 32, 32, 256, 256);
                    if (j < 32) {
                        screen.setVersionTooltip(TextFormatting.GOLD +
                                                 I18n.format("selectWorld.tooltip.snapshot1") +
                                                 "\n" +
                                                 TextFormatting.GOLD +
                                                 I18n.format("selectWorld.tooltip.snapshot2"));
                    }
                }
            }
            else {
                AbstractGui.blit(rowX, rowY, 0.0F, i, 32, 32, 256, 256);
            }
        }
    }
}
