package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class IntegerPropertyWidget extends GenericPropertyWidget<Integer> {
    public IntegerPropertyWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    int maxBound = Integer.MAX_VALUE;
    int minBound = Integer.MIN_VALUE;

    // sets min (inclusive) and max (also inclusive) accepted values
    public void setBounds(int min, int max) {
        this.minBound = min;
        this.maxBound = max;
    }

    public boolean inBounds(int i) {
        return (this.minBound <= i && i <= this.maxBound);
    }

    @Override
    public Integer parseInput() {
        int i;
        try {
            i = Integer.parseInt(this.getText());
        } catch (NumberFormatException e) {
            return null;
        }
        if(inBounds(i)) {
            return i;
        } else {
            return null;
        }
    }

    @Override
    public boolean charValid(char c) {
        char[] valid = new char[]{'0','1','2','3','4','5','6','7','8','9','-'}; // digits, minus sign
        for(char v : valid) { // TODO: can this be faster?
            if(c == v) return true;
        }
        return false;
    }

    @Override
    public boolean incrementValue(double amount) {
        Integer i = this.parseInput();
        if(i == null) {
            return false;
        }

        int j = i + (int)Math.signum(amount);

        if(!inBounds(j)) {
            return false;
        }

        this.setText(String.valueOf(j));
        return true;
    }
}
