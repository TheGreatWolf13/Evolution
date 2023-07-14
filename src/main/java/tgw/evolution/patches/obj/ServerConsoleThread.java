package tgw.evolution.patches.obj;

import net.minecraft.server.dedicated.DedicatedServer;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ServerConsoleThread extends Thread {

    private final Logger logger;
    private final DedicatedServer server;

    public ServerConsoleThread(DedicatedServer server, Logger logger) {
        super("Server console handler");
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            String string;
            while (!this.server.isStopped() && this.server.isRunning() && (string = bufferedReader.readLine()) != null) {
                this.server.handleConsoleInput(string, this.server.createCommandSourceStack());
            }
        }
        catch (IOException e) {
            this.logger.error("Exception handling console input", e);
        }
    }
}
