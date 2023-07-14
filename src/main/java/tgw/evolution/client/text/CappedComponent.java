package tgw.evolution.client.text;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.List;

public class CappedComponent implements MutableComponent {

    protected final OList<Component> siblings = new OArrayList<>();
    private final Component base;
    private final @Nullable Component widthLimiter;
    private String cached = "";
    private @Nullable Language cachedWith;
    private @Nullable Language decomposedWith;
    private boolean isCapped;
    private Style style = Style.EMPTY;
    private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
    private int width;

    public CappedComponent(Component base, int width) {
        this(base, width, null);
    }

    public CappedComponent(Component base, int width, @Nullable Component widthLimiter) {
        this.base = base;
        this.width = width;
        this.widthLimiter = widthLimiter != null ? widthLimiter.copy() : null;
    }

    @Override
    public MutableComponent append(Component sibling) {
        this.siblings.add(sibling);
        return this;
    }

    @Override
    public MutableComponent copy() {
        CappedComponent comp = this.plainCopy();
        comp.siblings.addAll(this.siblings);
        comp.setStyle(this.style);
        return comp;
    }

    public Component getBase() {
        return this.base;
    }

    @Override
    public String getContents() {
        Language language = Language.getInstance();
        if (this.cachedWith != language) {
            Font font = Minecraft.getInstance().font;
            int actualWidth = this.width - font.width("...");
            if (this.widthLimiter != null) {
                actualWidth -= font.width(this.widthLimiter);
            }
            if (font.width(this.base) > actualWidth) {
                FormattedText text = FormattedText.composite(font.substrByWidth(this.base, actualWidth), FormattedText.of("..."));
                this.cached = text.getString();
                this.isCapped = true;
            }
            else {
                this.cached = this.base.getString();
                this.isCapped = false;
            }
            this.cachedWith = language;
        }
        return this.cached;
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        Language language = Language.getInstance();
        if (this.decomposedWith != language) {
            Font font = Minecraft.getInstance().font;
            int actualWidth = this.width - font.width("...");
            if (this.widthLimiter != null) {
                actualWidth -= font.width(this.widthLimiter);
            }
            if (font.width(this.base) > actualWidth) {
                FormattedText text = FormattedText.composite(font.substrByWidth(this.base, actualWidth), FormattedText.of("..."));
                this.visualOrderText = language.getVisualOrder(text);
                this.isCapped = true;
            }
            else {
                this.visualOrderText = language.getVisualOrder(this.base);
                this.isCapped = false;
            }
            this.decomposedWith = language;
        }
        return this.visualOrderText;
    }

    public int getWidth() {
        return this.width;
    }

    public @Nullable Component getWidthLimiter() {
        return this.widthLimiter;
    }

    public boolean isCapped() {
        return this.isCapped;
    }

    @Override
    public CappedComponent plainCopy() {
        return new CappedComponent(this.base, this.width);
    }

    @Override
    public void resetCache() {
        this.cachedWith = null;
        this.decomposedWith = null;
        for (int i = 0, l = this.siblings.size(); i < l; i++) {
            this.siblings.get(i).resetCache();
        }
    }

    @Override
    public MutableComponent setStyle(Style style) {
        this.style = style;
        return this;
    }

    public void setWidth(int width, Component parent) {
        if (this.width != width) {
            this.width = width;
            parent.resetCache();
        }
    }
}
