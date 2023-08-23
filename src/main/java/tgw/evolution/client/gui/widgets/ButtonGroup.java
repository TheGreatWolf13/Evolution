package tgw.evolution.client.gui.widgets;

public class ButtonGroup {

    private int count;
    private int selected = -1;

    public int getSelected() {
        return this.selected;
    }

    public int register() {
        return this.count++;
    }

    public void reset() {
        this.count = 0;
    }

    public void setSelected(int index) {
        this.selected = index;
    }
}
