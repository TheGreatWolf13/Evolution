package tgw.evolution.client.gui;

import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public class ScissorStack {

    public static final Area TOTAL_AREA = new Area(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final OList<Area> stack = new OArrayList<>();
    private int size;

    public Area getArea() {
        if (this.size == 0) {
            return TOTAL_AREA;
        }
        return this.stack.get(this.size - 1);
    }

    public Area pop() {
        if (--this.size == 0) {
            return TOTAL_AREA;
        }
        return this.stack.get(this.size - 1);
    }

    public boolean push(int x0, int y0, int x1, int y1) {
        Area oldArea = this.size == 0 ? TOTAL_AREA : this.stack.get(this.size - 1);
        boolean changed = false;
        if (x0 > oldArea.x0) {
            changed = true;
        }
        else {
            x0 = oldArea.x0;
        }
        if (y0 > oldArea.y0) {
            changed = true;
        }
        else {
            y0 = oldArea.y0;
        }
        if (x1 < oldArea.x1) {
            changed = true;
        }
        else {
            x1 = oldArea.x1;
        }
        if (y1 < oldArea.y1) {
            changed = true;
        }
        else {
            y1 = oldArea.y1;
        }
        if (this.stack.size() > this.size) {
            Area area = this.stack.get(this.size++);
            area.set(x0, y0, x1, y1);
        }
        else {
            this.stack.add(new Area(x0, y0, x1, y1));
            ++this.size;
        }
        return changed;
    }

    public static class Area {
        private int x0;
        private int x1;
        private int y0;
        private int y1;

        protected Area(int x0, int y0, int x1, int y1) {
            this.x0 = x0;
            this.x1 = x1;
            this.y0 = y0;
            this.y1 = y1;
        }

        protected void set(int x0, int y0, int x1, int y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        public int x0() {
            return this.x0;
        }

        public int x1() {
            return this.x1;
        }

        public int y0() {
            return this.y0;
        }

        public int y1() {
            return this.y1;
        }
    }
}
