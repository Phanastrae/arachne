package phanastrae.arachne.screen.editor.tools;

import phanastrae.arachne.screen.editor.EditorMainScreen;

public interface ToolType {
    default void onSwitchTo(EditorMainScreen mls) {}; //TODO: make these have a default implementation?
    default void onTick(EditorMainScreen mls) {};
    default void onClick(EditorMainScreen mls) {};
    default void onRelease(EditorMainScreen mls) {};
    String getId();
}
