package tgw.evolution.inventory;

import net.minecraftforge.items.IItemHandler;

public interface IEvolutionContainer {

    void addSlot(IItemHandler handler, int index, int x, int y);

    default void addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = this.addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
    }

    default int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            this.addSlot(handler, index, x, y);
            x += dx;
            index++;
        }
        return index;
    }

    default void layoutPlayerInventorySlots(IItemHandler inv, int leftCol, int topRow) {
        topRow += 58;
        this.addSlotRange(inv, 0, leftCol, topRow, 9, 18);
        topRow -= 58;
        this.addSlotBox(inv, 9, leftCol, topRow, 9, 18, 3, 18);
    }
}
