package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.weave.element.GTV;
import phanastrae.arachne.weave.element.sketch.SketchElement;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class DoublePropertyWidget<E extends GTV> extends GenericPropertyWidget<Double, E> {

    double min = Double.NEGATIVE_INFINITY;
    double max = Double.POSITIVE_INFINITY;

    public DoublePropertyWidget(Function<E, Double> getter, BiConsumer<E, Double> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height) {
        super(getter, setter, name, editorInstance, textRenderer, width, height);
    }

    public DoublePropertyWidget(Function<E, Double> getter, BiConsumer<E, Double> setter, String name, EditorInstance editorInstance, TextRenderer textRenderer, int width, int height, boolean sendUpdate) {
        super(getter, setter, name, editorInstance, textRenderer, width, height, sendUpdate);
    }

    @Override
    public String varToString(Double var) {
        return var == null ? "" : Double.toString(var);
    }

    @Override
    public Double stringToVar(String string) {
        if(string == null) return null;

        try {
            return Double.parseDouble(string);
        } catch(NumberFormatException e){
            return null;
        }
    }

    final static char[] valid = new char[]{'0','1','2','3','4','5','6','7','8','9','.','-','E'}; // digits, decimal point, minus sign, exponential sign

    @Override
    public boolean charValid(char c) {
        for(char v : valid) { // TODO: can this be faster?
            if(c == v) return true;
        }
        return false;
    }

    @Override
    public Double incrementBy(Double var, double amount) {
        return var == null ? null : clamp(var + amount);
    }

    @Override
    public boolean isStringValid(String string) {
        try {
            double d = Double.parseDouble(string);
            return min <= d && d <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setBounds(double min, double max) {
        this.setMin(min);
        this.setMax(max);
    }

    public double clamp(double i) {
        if(i < min) return min;
        if(i > max) return max;
        return i;
    }
}
