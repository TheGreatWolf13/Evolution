package tgw.evolution.client.gui.widgets;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.function.Function;
import java.util.function.Predicate;

public class TextWithActionList<T> extends TextList {

    private final Predicate<T> action;
    private final OList<T> list = new OArrayList<>();
    private final Function<T, Component> textMaker;

    public TextWithActionList(int x, int y, int width, Predicate<T> action, Function<T, Component> textMaker) {
        super(x, y, width);
        this.action = action;
        this.textMaker = textMaker;
    }

    @Override
    public void add(Component text) {
        throw new UnsupportedOperationException("Should not be called! Call addAction(T)");
    }

    public void addAction(@Nullable T t) {
        if (t != null) {
            super.add(this.textMaker.apply(t));
            this.list.add(t);
        }
    }

    @Override
    protected boolean click(int index) {
        return this.action.test(this.list.get(index));
    }
}
