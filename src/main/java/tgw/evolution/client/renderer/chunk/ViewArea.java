package tgw.evolution.client.renderer.chunk;

import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ViewArea {

    private int camSecX;
    private int camSecY;
    private int camSecZ;
    private int height;
    public final Level level;
    private final LevelRenderer levelRenderer;
    private int renderDistance;
    public SectionRenderDispatcher.RenderSection[] sections;
    private int width;

    public ViewArea(SectionRenderDispatcher dispatcher, Level level, LevelRenderer levelRenderer, int viewDistance) {
        this.level = level;
        this.levelRenderer = levelRenderer;
        this.setViewDistance(viewDistance);
        this.createSections(dispatcher);
        this.camSecX = viewDistance + 1;
        this.camSecY = 0;
        this.camSecZ = viewDistance + 1;
    }

    private boolean containsSection(int secX, int secY, int secZ) {
        if (secY >= this.level.getMinSection() && secY <= this.level.getMaxSection()) {
            return secX >= this.camSecX - this.renderDistance && secX <= this.camSecX + this.renderDistance && secZ >= this.camSecZ - this.renderDistance && secZ <= this.camSecZ + this.renderDistance;
        }
        return false;
    }

    protected void createSections(SectionRenderDispatcher dispatcher) {
        assert Minecraft.getInstance().isSameThread() : "createChunks called from wrong thread: " + Thread.currentThread().getName();
        this.sections = new SectionRenderDispatcher.RenderSection[this.width * this.height * this.width];
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                for (int z = 0; z < this.width; ++z) {
                    int index = this.getSectionIndex(x, y, z);
                    //noinspection ObjectAllocationInLoop
                    this.sections[index] = dispatcher.new RenderSection(index, SectionPos.asLong(x, y + this.level.getMinSection(), z));
                }
            }
        }
    }

    public int getCamSecX() {
        return this.camSecX;
    }

    public int getCamSecY() {
        return this.camSecY;
    }

    public int getCamSecZ() {
        return this.camSecZ;
    }

    public int getHeight() {
        return this.height;
    }

    public int getRenderDistance() {
        return this.renderDistance;
    }

    protected @Nullable SectionRenderDispatcher.RenderSection getRenderSection(long secPos) {
        int secX = SectionPos.x(secPos);
        int secY = SectionPos.y(secPos);
        int secZ = SectionPos.z(secPos);
        return this.getRenderSection(secX, secY, secZ);
    }

    private @Nullable SectionRenderDispatcher.RenderSection getRenderSection(int secX, int secY, int secZ) {
        if (!this.containsSection(secX, secY, secZ)) {
            return null;
        }
        int i = Math.floorMod(secX, this.width);
        int j = secY - this.level.getMinSection();
        int k = Math.floorMod(secZ, this.width);
        return this.sections[this.getSectionIndex(i, j, k)];
    }

    public @Nullable SectionRenderDispatcher.RenderSection getRenderSectionAt(int posX, int posY, int posZ) {
        int y = posY - this.level.getMinBuildHeight() >> 4;
        if (y >= 0 && y < this.height) {
            return this.sections[this.getSectionIndex(Mth.positiveModulo(posX >> 4, this.width), y, Mth.positiveModulo(posZ >> 4, this.width))];
        }
        return null;
    }

    private int getSectionIndex(int x, int y, int z) {
        return (z * this.height + y) * this.width + x;
    }

    public void releaseAllBuffers() {
        for (SectionRenderDispatcher.RenderSection section : this.sections) {
            section.releaseBuffers();
        }
    }

    public void repositionCamera(double camEntityX, double camEntityY, double camEntityZ) {
        int camX = Mth.ceil(camEntityX);
        int camZ = Mth.ceil(camEntityZ);
        int width = this.width;
        int xSize = width * 16;
        int zSize = width * 16;
        int xOffset = camX - 8 - xSize / 2;
        int zOffset = camZ - 8 - zSize / 2;
        for (int x = 0; x < width; ++x) {
            int originX = xOffset + Math.floorMod(x * 16 - xOffset, xSize);
            for (int z = 0; z < width; ++z) {
                int originZ = zOffset + Math.floorMod(z * 16 - zOffset, zSize);
                for (int y = 0, height = this.height; y < height; ++y) {
                    int originY = this.level.getMinBuildHeight() + y * 16;
                    SectionRenderDispatcher.RenderSection chunk = this.sections[this.getSectionIndex(x, y, z)];
                    if (originX != chunk.getX() || originY != chunk.getY() || originZ != chunk.getZ()) {
                        chunk.setSectionNode(SectionPos.asLong(SectionPos.blockToSectionCoord(originX), SectionPos.blockToSectionCoord(originY), SectionPos.blockToSectionCoord(originZ)));
                    }
                }
            }
        }
        this.camSecX = SectionPos.blockToSectionCoord(camX);
        this.camSecY = SectionPos.blockToSectionCoord(Mth.ceil(camEntityY));
        this.camSecZ = SectionPos.blockToSectionCoord(camZ);
        this.levelRenderer.getSectionOcclusionGraph().invalidate();
    }

    public void resetVisibility() {
        for (SectionRenderDispatcher.RenderSection chunk : this.sections) {
            chunk.visibility = Visibility.OUTSIDE;
        }
    }

    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
        int i = Math.floorMod(sectionX, this.width);
        int j = Math.floorMod(sectionY - this.level.getMinSection(), this.height);
        int k = Math.floorMod(sectionZ, this.width);
        SectionRenderDispatcher.RenderSection chunk = this.sections[this.getSectionIndex(i, j, k)];
        chunk.setDirty(reRenderOnMainThread);
    }

    protected void setViewDistance(int renderDistance) {
        this.renderDistance = renderDistance;
        this.width = Mth.smallestEncompassingPowerOfTwo(renderDistance * 2 + 1);
        this.height = this.level.getSectionsCount();
    }
}
