package tgw.evolution.client.gui.advancements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum AdvancementTabType {
    ABOVE(0, 0, 28, 32, 8),
    BELOW(84, 0, 28, 32, 8),
    LEFT(0, 64, 32, 28, 5),
    RIGHT(96, 64, 32, 28, 5);

    private final int height;
    private final int max;
    private final int textureX;
    private final int textureY;
    private final int width;

    AdvancementTabType(int x, int y, int width, int height, int max) {
        this.textureX = x;
        this.textureY = y;
        this.width = width;
        this.height = height;
        this.max = max;
    }

    @Nullable
    public static AdvancementTabType getTabType(int width, int height, int index) {
        int horizontal = width / 32;
        if (index < horizontal) {
            return ABOVE;
        }
        if (index < 2 * horizontal) {
            return BELOW;
        }
        int vertical = height / 32;
        if (index < 2 * horizontal + vertical) {
            return RIGHT;
        }
        if (index < 2 * horizontal + 2 * vertical) {
            return LEFT;
        }
        return null;
    }

    public void draw(PoseStack matrices, GuiComponent gui, int x, int y, int width, int height, boolean selected, int index) {
        int i = this.textureX;
        index %= this.getMax(width, height);
        if (index > 0) {
            i += this.width;
        }
        if (x + this.width == width) {
            i += this.width;
        }
        int j = selected ? this.textureY + this.height : this.textureY;
        gui.blit(matrices, x + this.getX(index, width, height), y + this.getY(index, width, height), i, j, this.width, this.height);
    }

    public void drawIcon(int left, int top, int width, int height, int index, ItemRenderer itemRenderer, ItemStack stack) {
        int i = left + this.getX(index, width, height);
        int j = top + this.getY(index, width, height);
        switch (this) {
            case ABOVE -> {
                i += 6;
                j += 9;
            }
            case BELOW -> {
                i += 6;
                j += 6;
            }
            case LEFT -> {
                i += 10;
                j += 5;
            }
            case RIGHT -> {
                i += 6;
                j += 5;
            }
        }
        itemRenderer.renderAndDecorateItem(stack, i, j);
    }

    public int getMax() {
        return this.max;
    }

    private int getMax(int width, int height) {
        return switch (this) {
            case LEFT, RIGHT -> height / 32;
            case ABOVE, BELOW -> width / 32;
        };
    }

    public int getX(int p_192648_1_) {
        return switch (this) {
            case ABOVE, BELOW -> (this.width + 4) * p_192648_1_;
            case LEFT -> -this.width + 4;
            case RIGHT -> 248;
        };
    }

    public int getX(int index, int width, int height) {
        index %= this.getMax(width, height);
        return switch (this) {
            case ABOVE, BELOW -> (this.width + 4) * index;
            case LEFT -> -this.width + 4;
            case RIGHT -> width - 4;
        };
    }

    public int getY(int p_192653_1_) {
        return switch (this) {
            case ABOVE -> -this.height + 4;
            case BELOW -> 136;
            case LEFT, RIGHT -> this.height * p_192653_1_;
        };
    }

    public int getY(int index, int width, int height) {
        index %= this.getMax(width, height);
        return switch (this) {
            case ABOVE -> -this.height + 4;
            case BELOW -> height - 4;
            case LEFT, RIGHT -> this.height * index;
        };
    }

    public boolean isMouseOver(int left, int top, int width, int height, int index, double mouseX, double mouseY) {
        int i = left + this.getX(index, width, height);
        int j = top + this.getY(index, width, height);
        return mouseX > i && mouseX < i + this.width && mouseY > j && mouseY < j + this.height;
    }
}
