package phanastrae.arachne.weave.element.sketch;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.weave.element.GTV;

public class SketchElement implements GTV {
    boolean canDelete = true;

    public boolean selected;
    public boolean highlighted;
    boolean added;

    // used for various stuff in editor
    // typically intended to reduce times from O(n^2) to O(n) e.g. running list.contains for all elements of a list.
    // should be set back to false after use
    public boolean generalTempVariable = false;

    @Nullable
    public SketchTransform parent;

    public void setParent(@Nullable SketchTransform parent) {
        if(this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
        if(parent != null) {
            parent.addChild(this);
        }
    }

    @Nullable
    public SketchTransform getParent() {
        return this.parent;
    }

    public boolean add() {
        if(this.added) {
            return false;
        } else {
            this.added = true;
            if (this.parent != null) {
                this.parent.addChild(this);
            }
            return true;
        }
    }

    public boolean remove() {
        if(!canDelete) return false;

        if(!this.added) {
            return false;
        } else {
            this.added = false;
            if (this.parent != null) {
                this.parent.removeChild(this);
            }
            return true;
        }
    }

    public boolean getAdded() {
        return this.added;
    }

    public Text getTypeName() {
        return Text.empty();
    }

    public boolean canDelete() {
        return this.canDelete;
    }

    public void setDeletable(boolean deletable) {
        this.canDelete = deletable;
    }

    @Override
    public boolean getGTV() {
        return this.generalTempVariable;
    }

    @Override
    public void setGTV(boolean b) {
        this.generalTempVariable = b;
    }
}
