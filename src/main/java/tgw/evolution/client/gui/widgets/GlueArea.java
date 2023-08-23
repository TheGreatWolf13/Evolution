package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.util.collection.lists.OList;

public class GlueArea extends Area {

    protected final AbstractWidget glue;

    public GlueArea(int x, int y, int width, int height, int spacing, AbstractWidget glue) {
        super(x, y, width, height, spacing);
        this.glue = glue;
        glue.setParent(this);
    }

    @Override
    public boolean charTyped(char c, @Modifiers int modifiers) {
        if (this.glue.charTyped(c, modifiers)) {
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    @Override
    protected boolean childMouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (this.glue.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.childMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(@Key int key, int scancode, @Modifiers int modifiers) {
        if (this.glue.keyPressed(key, scancode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (this.glue.mouseScrolled(mouseX, mouseY, scroll)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    protected void renderEntries(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.renderEntries(matrices, mouseX, mouseY, partialTicks);
        this.glue.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        super.setScreen(screen);
        this.glue.setScreen(screen);
    }

    @Override
    public void setWidth(int width, int spacingForChildren) {
        super.setWidth(width, spacingForChildren);
        if (this.glue instanceof Area a) {
            a.setWidth(width, spacingForChildren);
        }
        else {
            this.glue.setWidth(width - spacingForChildren);
        }
    }

    @Override
    public void setX(int x, int spacingForChildren) {
        super.setX(x, spacingForChildren);
        if (this.glue instanceof Area a) {
            a.setX(x, spacingForChildren);
        }
        else {
            this.glue.setX(x + spacingForChildren);
        }
    }

    @Override
    protected void updateEntries() {
        int y = this.y + this.spacing;
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            AbstractWidget widget = list.get(i);
            widget.setY(y);
            y += widget.getHeight() + this.spacing;
        }
        int beginGlue = y;
        y = this.y + this.height;
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            AbstractWidget widget = list.get(i);
            y -= widget.getHeight() + this.spacing;
            widget.setY(y);
        }
        int endGlue = y - this.spacing;
        this.glue.setY(beginGlue);
        this.glue.setHeight(endGlue - beginGlue);
    }
}
