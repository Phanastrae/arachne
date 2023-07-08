package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class DoublePropertyWidget extends GenericPropertyWidget<Double> {
    public DoublePropertyWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public Double parseInput() {
        double d;
        try {
            d = Double.parseDouble(this.getText());
        } catch (NumberFormatException e) {
            return null;
        }
        if(Double.isFinite(d)) {
            return d;
        } else {
            return null;
        }
    }

    @Override
    public boolean charValid(char c) {
        char[] valid = new char[]{'0','1','2','3','4','5','6','7','8','9','.','-','E'}; // digits, decimal point, minus sign, exponential sign
        for(char v : valid) { // TODO: can this be faster?
            if(c == v) return true;
        }
        return false;
    }

    @Override
    public boolean incrementValue(double amount) {
        Double d = this.parseInput();
        if(d == null) {
            return false;
        } else {
            this.setText(String.valueOf(d + amount / 16.0));
            return true;
        }
    }
}
