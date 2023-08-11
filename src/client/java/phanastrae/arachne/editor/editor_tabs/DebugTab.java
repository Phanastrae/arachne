package phanastrae.arachne.editor.editor_tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.screen.widget.PropertyEditorWidget;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.arachne.weave.WeaveInstance;
import phanastrae.arachne.weave.element.built.BuiltFace;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.List;

public class DebugTab extends EditorTab {

    TextWidget tw = null;

    public DebugTab(String id) {
        super(id);
    }

    @Override
    public List<Pair<Text, Runnable>> getActions(EditorInstance editorInstance) {
        List<Pair<Text, Runnable>> list = super.getActions(editorInstance);
        list.add(new Pair<>(Text.of("Time Physics"), () -> this.timePhysics(editorInstance)));
        return list;
    }

    @Override
    public void initScreen(EditorMainScreen screen) {
        this.tw = new TextWidget(160, 20, Text.empty(), screen.getTextRenderer());
        screen.addChild(this.tw);
        screen.positionWidget(tw, 0.5f, 0.5f);
    }

    void timePhysics(EditorInstance editorInstance) {
        WeaveInstance weaveInstance = new WeaveInstance(editorInstance.getSketchWeave().buildWeave());

        int ITERATIONS = 1000;
        long t1 = System.nanoTime();
        for(int i = 0; i < ITERATIONS; i++) {
            weaveInstance.preUpdate(MinecraftClient.getInstance().world);
            weaveInstance.update();
        }
        long t2 = System.nanoTime();
        long dt = t2 - t1;
        double sum = dt * 1E-9;
        double avg = sum / ITERATIONS;

        if(this.tw != null) {
            String s = "Average Physics Update Time : " + (long)(avg * 1E6) + "(μs)";
            tw.setMessage(Text.of(s));
        }
    }
}
