package phanastrae.arachne.util;

import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.element.sketch.SketchElement;
import phanastrae.arachne.weave.element.sketch.SketchTransform;

import java.util.List;

public class SketchUtil {

    // returns the most deeply nested common parent of all selected elements
    // returns null if elements share no parent
    @Nullable
    public static SketchTransform getCommonParent(List<SketchElement> elements) {
        if (elements == null || elements.isEmpty()) return null;
        if (elements.size() == 1) return elements.get(0).getParent();

        int lvl = Integer.MAX_VALUE;
        for (SketchElement e : elements) {
            if (e.parent == null) {
                return null;
            } else {
                int pl = e.parent.getParentLevel();
                if (pl < lvl) {
                    lvl = pl;
                }
            }
        }
        while (lvl >= 0) {
            boolean parentsShared = true;
            SketchTransform parent = null;
            for (SketchElement e : elements) {
                SketchTransform p = e.parent;
                if (p == null) {
                    return null;
                }
                int j = p.getParentLevel();
                // get the element's parent('s parent's parent's ... etc) of tier lvl
                for (int i = lvl; i < j; i++) {
                    p = p.parent;
                    if (p == null) return null;
                }
                if (parent == null) {
                    parent = p;
                } else if (parent != p) {
                    parentsShared = false;
                    break;
                }
            }
            if (parentsShared) {
                return parent;
            } else {
                lvl--;
            }
        }
        return null;
    }
}
