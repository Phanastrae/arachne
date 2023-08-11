package phanastrae.arachne.old.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.stream.IntStream;

public class TextPropertyWidget extends TextFieldWidget {
    public TextPropertyWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            //this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            boolean alreadyFocused = this.isFocused();
            this.onClick(mouseX, mouseY);
            if(button == 0 & !alreadyFocused) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
            }
            if(button == 1) {
                this.setText("");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(incrementValue(amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }

    String blankText = "";

    public enum SelectionType {
        EMPTY,
        SINGLE,
        MULTI
    }

    public void setState(SelectionType selectionType, String text) {
        if(this.isFocused()) return;
        String newBlankText = "";
        String newText = "";
        switch(selectionType) {
            case EMPTY -> {
                newBlankText = this.getTextForEmpty().getString();
            }
            case SINGLE -> {
                if(!Objects.equals(text, "")) {
                    newText = text;
                } else {
                    newBlankText = "none";
                }
            }
            case MULTI -> {
                newBlankText = this.getTextForMulti().getString();
            }
        }
        this.blankText = newBlankText;
        this.setText(newText);
    }

    Text textForEmpty = Text.empty();

    public void setTextForEmpty(Text text) {
        this.textForEmpty = text;
    }

    public Text getTextForEmpty() {
        return this.textForEmpty;
    }

    public Text getTextForMulti() {
        return Text.literal("multiple values");
    }

    public void update() {
        if(this.getText().length() == 0) {
            this.setSuggestion(this.blankText);
        } else {
            this.setSuggestion("");
        }
        if(this.getText().isEmpty() || isInputValid()) {
            this.setEditableColor(0x00FF00);
        } else {
            this.setEditableColor(0xFF0000);
        }
    }

    public boolean isInputValid() {
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean bl = super.charTyped(chr, modifiers);
        update();
        return bl;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = super.keyPressed(keyCode, scanCode, modifiers);
        update();
        return bl;
    }

    @Override
    public void tick() {
        update();
        super.tick();
    }

    public boolean charValid(char c) {
        return true;
    }

    public boolean incrementValue(double amount) {
        return false;
    }

    @Override
    public void write(String text) {
        // strip banned characters
        IntStream chars = text.chars();
        IntStream filtered = chars.filter((i) -> charValid((char)i));
        int[] ia = filtered.toArray();
        char[] ca = new char[ia.length];
        for(int i = 0; i < ia.length; i++) {
            ca[i] = (char)ia[i];
        }
        super.write(String.valueOf(ca));
    }
}
