package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

public class ScrollableArea extends Area {

    protected int heightNeeded;
    protected double oldScrollMouseY;
    protected int scrollBarHeight;
    protected int scrollHeight;
    protected int scrollSize;

    public ScrollableArea(int x, int y, int width, int height, int spacing) {
        super(x, y, width, height, spacing);
        this.heightNeeded = spacing;
        this.updateScrollSize();
        this.updateEntries();
    }

    @Override
    public <T extends AbstractWidget> T addBeginning(T t) {
        super.addBeginning(t);
        this.heightNeeded += t.getHeight() + this.spacing;
        this.updateScrollSize();
        this.updateEntries();
        return t;
    }

    @Override
    public <T extends AbstractWidget> T addEnd(T t) {
        super.addEnd(t);
        this.heightNeeded += t.getHeight() + this.spacing;
        this.updateScrollSize();
        this.updateEntries();
        return t;
    }

    @Override
    protected boolean childMouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (!this.needsScroll()) {
            OList<AbstractWidget> list = this.beginningList;
            for (int i = 0, len = list.size(); i < len; ++i) {
                if (list.get(i).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            list = this.endList;
            for (int i = 0, len = list.size(); i < len; ++i) {
                if (list.get(i).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        else {
            if (MathHelper.isMouseInArea(mouseX, mouseY, this.x + this.width - 5, this.y + this.scrollBarHeight, 5, this.scrollSize)) {
                this.focusOnParent();
                Screen screen = this.getScreen();
                if (screen != null) {
                    screen.setDragging(true);
                }
                this.oldScrollMouseY = mouseY;
                return true;
            }
            for (int i = 0, len = this.beginningList.size(); i < len; ++i) {
                if (this.beginningList.get(i).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            for (int i = 0, len = this.endList.size(); i < len; ++i) {
                if (this.endList.get(i).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void childRequestedUpdate() {
        this.updateNeededHeight();
    }

    public void clear(boolean changed) {
        this.heightNeeded = 0;
        if (changed) {
            this.scrollHeight = 0;
            this.scrollBarHeight = 0;
        }
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            AbstractWidget widget = list.get(i);
            widget.setScreen(null);
            widget.setParent(null);
        }
        list.clear();
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            AbstractWidget widget = list.get(i);
            widget.setScreen(null);
            widget.setParent(null);
        }
        list.clear();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (this.needsScroll()) {
            this.scroll(scroll < 0 ? 5 : -5);
            return true;
        }
        return false;
    }

    protected boolean needsScroll() {
        return this.heightNeeded > this.height;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dx, double dy) {
        if (this.needsScroll()) {
            mouseY = Mth.clamp(mouseY, this.y + this.scrollBarHeight, this.y + this.scrollBarHeight + this.scrollSize);
            int delta = (int) (mouseY - this.oldScrollMouseY);
            if (delta != 0) {
                this.scroll(delta);
                this.oldScrollMouseY = mouseY;
            }
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        if (this.needsScroll()) {
            int barX = this.x + this.width - 6;
            GUIUtils.drawLine(barX, this.y, barX, this.y + this.height, 7, 0x00_0000);
            GUIUtils.drawLine(barX, this.y, barX, this.y + this.height, 0x50_5050);
            GUIUtils.drawLine(barX + 6, this.y, barX + 6, this.y + this.height, 0x50_5050);
            GUIUtils.drawLine(barX, this.y - 1, barX + 6, this.y - 1, 0x50_5050);
            GUIUtils.drawLine(barX, this.y + this.height, barX + 6, this.y + this.height, 0x50_5050);
            int barY = this.y + this.scrollBarHeight;
            GUIUtils.drawLine(barX + 1, barY, barX + 1, barY + this.scrollSize, 5, 0xa0_a0a0);
            GUIUtils.drawLine(barX + 5, barY, barX + 5, barY + this.scrollSize, 0x80_8080);
            GUIUtils.drawLine(barX + 1, barY + this.scrollSize - 1, barX + 5, barY + this.scrollSize - 1, 0x80_8080);
            GUIUtils.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height + 1);
            this.renderEntries(matrices, mouseX, mouseY, partialTicks);
            GUIUtils.disableScissor();
        }
        else {
            this.renderEntries(matrices, mouseX, mouseY, partialTicks);
        }
    }

    protected void scroll(int amount) {
        int barEnd = this.height - this.scrollSize;
        this.scrollBarHeight += amount;
        if (this.scrollBarHeight < 0) {
            this.scrollBarHeight = 0;
        }
        else if (this.scrollBarHeight > barEnd) {
            this.scrollBarHeight = barEnd;
        }
        float rel = MathHelper.relativize(this.scrollBarHeight, 0, barEnd);
        this.scrollHeight = (int) (((float) this.heightNeeded / this.height - 1) * this.height * rel);
        this.updateEntries();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        this.updateScrollSize();
        this.updateEntries();
    }

    @Override
    public void setWidth(int width, int spacingForChildren) {
        this.width = width;
        if (!this.needsScroll()) {
            this.setChildWidth(width - spacingForChildren);
        }
        else {
            this.setChildWidth(width - spacingForChildren - 6);
        }
    }

    protected void setupVisibility(AbstractWidget widget, int y) {
        if (y + widget.getHeight() < this.y) {
            widget.visible = false;
            return;
        }
        if (y > this.y + this.height) {
            widget.visible = false;
            return;
        }
        widget.visible = true;
    }

    @Override
    protected void updateEntries() {
        if (this.needsScroll()) {
            int y = this.y + this.spacing - this.scrollHeight;
            OList<AbstractWidget> list = this.beginningList;
            for (int i = 0, len = list.size(); i < len; ++i) {
                AbstractWidget widget = list.get(i);
                widget.setY(y);
                this.setupVisibility(widget, y);
                y += widget.getHeight() + this.spacing;
            }
            list = this.endList;
            for (int i = list.size() - 1; i >= 0; --i) {
                AbstractWidget widget = list.get(i);
                widget.setY(y);
                this.setupVisibility(widget, y);
                y += widget.getHeight() + this.spacing;
            }
        }
        else {
            int y = this.y + this.spacing;
            OList<AbstractWidget> list = this.beginningList;
            for (int i = 0, len = list.size(); i < len; ++i) {
                AbstractWidget widget = list.get(i);
                widget.setY(y);
                widget.visible = true;
                y += widget.getHeight() + this.spacing;
            }
            y = this.y + this.height;
            list = this.endList;
            for (int i = 0, len = list.size(); i < len; ++i) {
                AbstractWidget widget = list.get(i);
                widget.visible = true;
                y -= widget.getHeight() + this.spacing;
                widget.setY(y);
            }
        }
    }

    private void updateNeededHeight() {
        int oldNeeded = this.heightNeeded;
        this.heightNeeded = this.spacing;
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            this.heightNeeded += list.get(i).getHeight() + this.spacing;
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            this.heightNeeded += list.get(i).getHeight() + this.spacing;
        }
        if (oldNeeded != this.heightNeeded) {
            if (this.needsScroll()) {
                this.updateScrollSize();
                this.scroll(0);
            }
            else {
                this.updateEntries();
            }
        }
    }

    protected void updateScrollSize() {
        if (this.needsScroll()) {
            this.scrollSize = (int) ((float) this.height / this.heightNeeded * this.height);
        }
    }
}
