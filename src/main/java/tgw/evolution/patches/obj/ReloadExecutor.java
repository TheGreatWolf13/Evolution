package tgw.evolution.patches.obj;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ReloadExecutor implements Executor {

    private final AtomicInteger doneTaskCounter;
    private final Executor executor;
    private final AtomicInteger startedTaskCounter;
    
    public ReloadExecutor(AtomicInteger doneTaskCounter, Executor executor, AtomicInteger startedTaskCounter) {
        this.doneTaskCounter = doneTaskCounter;
        this.executor = executor;
        this.startedTaskCounter = startedTaskCounter;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.startedTaskCounter.incrementAndGet();
        this.executor.execute(() -> {
            command.run();
            this.doneTaskCounter.incrementAndGet();
        });
    }
}
