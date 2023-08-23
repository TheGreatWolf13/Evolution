package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

public class Area extends AbstractWidget {

    protected final OList<AbstractWidget> beginningList;
    protected final OList<AbstractWidget> endList;
    protected final int spacing;
    protected @Nullable AbstractWidget focused;

    public Area(int x, int y, int width, int height, int spacing) {
        super(x, y, width, height, EvolutionTexts.EMPTY);
        this.beginningList = new OArrayList<>();
        this.endList = new OArrayList<>();
        this.spacing = spacing;
    }

    public <T extends AbstractWidget> T addBeginning(T t) {
        t.setParent(this);
        this.beginningList.add(t);
        this.updateEntries();
        return t;
    }

    public <T extends AbstractWidget> T addEnd(T t) {
        t.setParent(this);
        this.endList.add(t);
        this.updateEntries();
        return t;
    }

    @Override
    public boolean charTyped(char c, @Modifiers int modifiers) {
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (list.get(i).charTyped(c, modifiers)) {
                return true;
            }
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (list.get(i).charTyped(c, modifiers)) {
                return true;
            }
        }
        return false;
    }

    protected boolean childMouseClicked(double mouseX, double mouseY, @MouseButton int button) {
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
        return false;
    }

    @Override
    public void childRequestedUpdate() {
        this.updateEntries();
    }

    public @Nullable AbstractWidget getFocused() {
        return this.focused;
    }

    @Override
    public boolean keyPressed(@Key int key, int scancode, @Modifiers int modifiers) {
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (list.get(i).keyPressed(key, scancode, modifiers)) {
                return true;
            }
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (list.get(i).keyPressed(key, scancode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        this.focused = null;
        if (this.active && this.visible) {
            if (MathHelper.isMouseInArea(mouseX, mouseY, this.x, this.y, this.width, this.height)) {
                return this.childMouseClicked(mouseX, mouseY, button);
            }
        }
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        this.renderEntries(matrices, mouseX, mouseY, partialTicks);
    }

    protected void renderEntries(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).render(matrices, mouseX, mouseY, partialTicks);
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).render(matrices, mouseX, mouseY, partialTicks);
        }
    }

    protected void setChildWidth(int width) {
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setWidth(width);
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setWidth(width);
        }
    }

    public void setFocusOnParent(@Nullable AbstractWidget widget) {
        this.focused = widget;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        this.updateEntries();
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        super.setScreen(screen);
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setScreen(screen);
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setScreen(screen);
        }
    }

    public void setWidth(int width, int spacingForChildren) {
        this.width = width;
        this.setChildWidth(width - spacingForChildren);
    }

    public void setX(int x, int spacingForChildren) {
        this.x = x;
        x += spacingForChildren;
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setX(x);
        }
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setX(x);
        }
    }

    protected void updateEntries() {
        int y = this.y + this.spacing;
        OList<AbstractWidget> list = this.beginningList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            AbstractWidget widget = list.get(i);
            widget.setY(y);
            y += widget.getHeight() + this.spacing;
        }
        y = this.y + this.height;
        list = this.endList;
        for (int i = 0, len = list.size(); i < len; ++i) {
            AbstractWidget widget = list.get(i);
            y -= widget.getHeight() + this.spacing;
            widget.setY(y);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
