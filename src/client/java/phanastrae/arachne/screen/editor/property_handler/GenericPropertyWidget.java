package phanastrae.arachne.screen.editor.property_handler;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class GenericPropertyWidget<T> extends TextPropertyWidget {
    public GenericPropertyWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public void setConditionalChangedListeners(@Nullable Consumer<T> valid, @Nullable Consumer<String> invalid) {
        this.setChangedListener((b) -> this.conditionalChangedListener(valid, invalid));
    }

    public void conditionalChangedListener(@Nullable Consumer<T> valid, @Nullable Consumer<String> invalid) {
        String str = this.getText();
        T t = this.parseInput();
        if(t != null) {
            if(valid != null) {
                valid.accept(t);
            }
        } else {
            if(invalid != null) {
                invalid.accept(str);
            }
        }
    }

    public T parseInput() {
        return null;
    }

    @Override
    public boolean isInputValid() {
        return this.parseInput() != null;
    }
}
