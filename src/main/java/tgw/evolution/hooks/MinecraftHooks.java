package tgw.evolution.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.Logger;
import tgw.evolution.client.gui.ScreenCrash;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.MethodHandler;
import tgw.evolution.util.reflection.StaticFieldHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MinecraftHooks {

    private static final StaticFieldHandler<Minecraft, Logger> LOGGER = new StaticFieldHandler<>(Minecraft.class, "field_186797_u");
    private static final MethodHandler<Minecraft, Void> DROP_TASKS = new MethodHandler<>(Minecraft.class, "func_213159_be");
    private static final FieldHandler<Minecraft, Boolean> HAS_CRASHED = new FieldHandler<>(Minecraft.class, "field_71434_R");

    private MinecraftHooks() {
    }

    public static void displayCrashScreen(Minecraft mc, CrashReport report) {
        try {
            outputReport(report);
            HAS_CRASHED.set(mc, false);
            mc.gameSettings.showDebugInfo = false;
            mc.ingameGUI.getChatGUI().clearChatMessages(true);
            mc.displayGuiScreen(new ScreenCrash(report));
        }
        catch (Throwable t) {
            LOGGER.get().error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            mc.displayCrashReport(report);
            //noinspection ConstantConditions
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    /**
     * Hooks from {@link Minecraft#freeMemory()}, replacing the method.
     */
    public static void freeMemory(Minecraft mc) {
        try {
            try {
                if (Minecraft.memoryReserve != null) {
                    Minecraft.memoryReserve = null;
                }
            }
            catch (Throwable ignored) {
            }
            mc.worldRenderer.deleteAllDisplayLists();
            try {
                System.gc();
                if (mc.isSingleplayer()) {
                    mc.getIntegratedServer().initiateShutdown(true);
                }
                mc.func_213231_b(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
            }
            catch (Throwable ignored) {
            }
            if (mc.getConnection() != null) {
                mc.getConnection().getNetworkManager().closeChannel(new StringTextComponent("Client crashed"));
            }
            if (mc.gameRenderer.isShaderActive()) {
                mc.gameRenderer.stopUseShader();
            }
            try {
                Minecraft.memoryReserve = new byte[10_485_760];
            }
            catch (Throwable ignored) {
            }
            System.gc();
        }
        catch (Throwable t) {
            LOGGER.get().error("Failed to reset state after a crash", t);
        }
    }

    private static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getInstance().isOnExecutionThread() ? "-client" : "-server";
                reportName += ".txt";
                File reportsDir = new File(Minecraft.getInstance().gameDir, "crash-reports");
                File reportFile = new File(reportsDir, reportName);
                report.saveToFile(reportFile);
            }
        }
        catch (Throwable e) {
            LOGGER.get().fatal("Failed saving report", e);
        }
        LOGGER.get()
              .fatal("Minecraft ran into a problem! " +
                     (report.getFile() != null ? "Report saved to: " + report.getFile() : "Crash report could not be saved.") +
                     "\n" +
                     report.getCompleteReport());
    }
}
