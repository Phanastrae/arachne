package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class IntegerPropertyWidget<E extends GTV> extends GenericPropertyWidget<Integer, E> {

    int min = Integer.MIN_VALUE;
    int max = Integer.MAX_VALUE;

    public IntegerPropertyWidget(Function<E, Integer> getter, BiConsumer<E, Integer> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height) {
        super(getter, setter, name, editorInstance, textRenderer, width, height);
    }

    public IntegerPropertyWidget(Function<E, Integer> getter, BiConsumer<E, Integer> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height, boolean sendUpdate) {
        super(getter, setter, name, editorInstance, textRenderer, width, height, sendUpdate);
    }

    @Override
    public String varToString(Integer var) {
        return var == null ? "" : Integer.toString(clamp(var));
    }

    @Override
    public Integer stringToVar(String string) {
        if(string == null) return null;

        try {
            return Integer.parseInt(string);
        } catch(NumberFormatException e){
            return null;
        }
    }

    final static char[] valid = new char[]{'0','1','2','3','4','5','6','7','8','9','-'}; // digits, minus sign
    final static char[] validPos = new char[]{'0','1','2','3','4','5','6','7','8','9'}; // digits

    @Override
    public boolean charValid(char c) {
        if(max < min) return false;

        for(char v : min < 0 ? valid : validPos) { // TODO: can this be faster?
            if(c == v) return true;
        }
        return false;
    }

    @Override
    public Integer incrementBy(Integer var, double amount) {
        int a = (int)Math.round(amount);
        if(a == 0) a = (int)Math.signum(amount);
        return var == null ? null : clamp(var + a);
    }

    @Override
    public boolean isStringValid(String string) {
        try {
            int i = Integer.parseInt(string);
            return min <= i && i <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setBounds(int min, int max) {
        this.setMin(min);
        this.setMax(max);
    }

    public int clamp(int i) {
        if(i < min) return min;
        if(i > max) return max;
        return i;
    }
}
