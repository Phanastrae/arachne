package phanastrae.arachne.networking.screen_handler;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class SketchSlot extends Slot {
    public SketchSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
