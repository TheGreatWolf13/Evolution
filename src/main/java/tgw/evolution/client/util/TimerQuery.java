package tgw.evolution.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.*;

public class TimerQuery {

    private int nextQueryName;

    public static @Nullable TimerQuery getInstance() {
        return TimerQueryLazyLoader.INSTANCE;
    }

    public void beginProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.nextQueryName != 0) {
            throw new IllegalStateException("Current profile not ended");
        }
        this.nextQueryName = GL32C.glGenQueries();
        GL32C.glBeginQuery(GL33.GL_TIME_ELAPSED, this.nextQueryName);
    }

    public FrameProfile endProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.nextQueryName == 0) {
            throw new IllegalStateException("endProfile called before beginProfile");
        }
        GL32C.glEndQuery(GL33.GL_TIME_ELAPSED);
        FrameProfile frameProfile = new FrameProfile(this.nextQueryName);
        this.nextQueryName = 0;
        return frameProfile;
    }

    static final class TimerQueryLazyLoader {

        static final @Nullable TimerQuery INSTANCE = instantiate();

        private TimerQueryLazyLoader() {
        }

        private static @Nullable TimerQuery instantiate() {
            if (!GL.getCapabilities().GL_ARB_timer_query) {
                return null;
            }
            return new TimerQuery();
        }
    }

    public static class FrameProfile {
        private static final long NO_RESULT = 0L;
        private static final long CANCELLED_RESULT = -1L;
        private final int queryName;
        private long result;

        FrameProfile(int i) {
            this.queryName = i;
        }

        public void cancel() {
            RenderSystem.assertOnRenderThread();
            if (this.result != NO_RESULT) {
                return;
            }
            this.result = CANCELLED_RESULT;
            GL32C.glDeleteQueries(this.queryName);
        }

        public long get() {
            RenderSystem.assertOnRenderThread();
            if (this.result == NO_RESULT) {
                this.result = ARBTimerQuery.glGetQueryObjecti64(this.queryName, GL15.GL_QUERY_RESULT);
                GL32C.glDeleteQueries(this.queryName);
            }
            return this.result;
        }

        public boolean isDone() {
            RenderSystem.assertOnRenderThread();
            if (this.result != NO_RESULT) {
                return true;
            }
            if (1 == GL32C.glGetQueryObjecti(this.queryName, GL15.GL_QUERY_RESULT_AVAILABLE)) {
                this.result = ARBTimerQuery.glGetQueryObjecti64(this.queryName, GL15.GL_QUERY_RESULT);
                GL32C.glDeleteQueries(this.queryName);
                return true;
            }
            return false;
        }
    }
}
