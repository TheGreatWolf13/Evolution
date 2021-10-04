package tgw.evolution.client.gui.advancements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
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

    public void draw(MatrixStack matrices, AbstractGui gui, int x, int y, int width, int height, boolean selected, int index) {
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

    public void drawIcon(int left, int top, int width, int height, int index, ItemRenderer renderItem, ItemStack stack) {
        int i = left + this.getX(index, width, height);
        int j = top + this.getY(index, width, height);
        switch (this) {
            case ABOVE: {
                i += 6;
                j += 9;
                break;
            }
            case BELOW: {
                i += 6;
                j += 6;
                break;
            }
            case LEFT: {
                i += 10;
                j += 5;
                break;
            }
            case RIGHT: {
                i += 6;
                j += 5;
                break;
            }
        }
        renderItem.renderAndDecorateItem(null, stack, i, j);
    }

    public int getMax() {
        return this.max;
    }

    private int getMax(int width, int height) {
        switch (this) {
            case LEFT:
            case RIGHT: {
                return height / 32;
            }
            case ABOVE:
            case BELOW: {
                return width / 32;
            }
            default: {
                return this.max;
            }
        }
    }

    public int getX(int p_192648_1_) {
        switch (this) {
            case ABOVE:
            case BELOW: {
                return (this.width + 4) * p_192648_1_;
            }
            case LEFT: {
                return -this.width + 4;
            }
            case RIGHT: {
                return 248;
            }
            default: {
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
            }
        }
    }

    public int getX(int index, int width, int height) {
        index %= this.getMax(width, height);
        switch (this) {
            case ABOVE:
            case BELOW: {
                return (this.width + 4) * index;
            }
            case LEFT: {
                return -this.width + 4;
            }
            case RIGHT: {
                return width - 4;
            }
            default: {
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
            }
        }
    }

    public int getY(int p_192653_1_) {
        switch (this) {
            case ABOVE: {
                return -this.height + 4;
            }
            case BELOW: {
                return 136;
            }
            case LEFT:
            case RIGHT: {
                return this.height * p_192653_1_;
            }
            default: {
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
            }
        }
    }

    public int getY(int index, int width, int height) {
        index %= this.getMax(width, height);
        switch (this) {
            case ABOVE: {
                return -this.height + 4;
            }
            case BELOW: {
                return height - 4;
            }
            case LEFT:
            case RIGHT: {
                return this.height * index;
            }
            default: {
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
            }
        }
    }

    public boolean isMouseOver(int left, int top, int width, int height, int index, double mouseX, double mouseY) {
        int i = left + this.getX(index, width, height);
        int j = top + this.getY(index, width, height);
        return mouseX > i && mouseX < i + this.width && mouseY > j && mouseY < j + this.height;
    }
}
