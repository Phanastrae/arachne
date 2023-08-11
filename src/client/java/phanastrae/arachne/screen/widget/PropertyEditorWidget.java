package phanastrae.arachne.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import phanastrae.arachne.editor.EditorInstance;
import phanastrae.arachne.editor.editor_actions.EditorAction;
import phanastrae.arachne.editor.editor_actions.ModifyVariablesAction;
import phanastrae.arachne.editor.editor_tabs.ConfigTab;
import phanastrae.arachne.weave.element.sketch.*;

import java.util.ArrayList;
import java.util.List;

public class PropertyEditorWidget extends ScrollableSubWindowWidget {

    EditorInstance editorInstance;

    @Nullable
    PropertyHandler<?> propertyHandler;

    public PropertyEditorWidget(EditorInstance editorInstance, TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(textRenderer, x, y, width, height, message);
        this.editorInstance = editorInstance;
    }

    public void onSelectionChanged() {
        if(this.propertyHandler == null) return;
        if(editorInstance.getTabHandler().getActiveTab() instanceof ConfigTab) {
        } else {
            List<SketchElement> selection = this.editorInstance.getSelectionManager().getSelection();
            this.propertyHandler.onSelectionChanged(selection);
        }
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO
    }

    public void setupTypeButtons(List<Class<? extends SketchElement>> types, @Nullable Class<? extends SketchElement> type) {
        for(Class<? extends SketchElement> c : types) {
            String str;
            boolean shorten = false;
            if(c == SketchVertex.class) {
                str = "vertex";
            } else if(c == SketchEdge.class) {
                str = "edge";
            } else if(c == SketchFace.class) {
                str = "face";
            } else if(c == SketchTransform.class) {
                str = "transform";
            } else if(c == SketchPhysicsMaterial.class) {
                str = "physicsMaterial";
                shorten = true;
            } else if(c == SketchRenderMaterial.class) {
                str = "renderMaterial";
                shorten = true;
            } else if(c == SketchSettings.class) {
                str = "settings";
            } else {
                str = null;
            }
            Text name = str == null ? Text.empty() : Text.translatable("arachne.editor.type."+str+(shorten?".short":""));
            ButtonWidget bw = ButtonWidget.builder(name, (b) -> this.setup(types, c)).width(74).build();
            if(str != null) {
                bw.setTooltip(Tooltip.of(name));
            }
            if(type == c) {
                bw.active = false;
            }
            this.children.add(bw);
        }
        addLine();

        addSeparator();
    }

    void setupPropertyHandler(@Nullable Class<? extends SketchElement> type) {
        if(type == SketchVertex.class) {
            this.propertyHandler = new PropertyHandler<>(SketchVertex.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    addPropertyWidget(DoublePropertyWidget::new, SketchVertex::getX, SketchVertex::setX, "x", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchVertex::getY, SketchVertex::setY, "y", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchVertex::getZ, SketchVertex::setZ, "z", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchVertex::getVirtualVolume, SketchVertex::setVirtualVolume, "virtualVolume", 10);
                    addBooleanPropertyWidget(SketchVertex::getIsStatic, SketchVertex::setIsStatic, "isStatic", 10);
                    addPhysicsMaterial(SketchVertex::getPhysicsMaterial, SketchVertex::setPhysicsMaterial, "physicsMaterial.short", 10, () -> this.editorInstance.getSketchWeave().getPhysicsMaterials());
                }
            };
        } else if(type == SketchEdge.class) {
            this.propertyHandler = new PropertyHandler<>(SketchEdge.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    DoublePropertyWidget<SketchEdge> dpw = (DoublePropertyWidget<SketchEdge>)addPropertyWidget(DoublePropertyWidget::new, SketchEdge::getLength, SketchEdge::setLength, "length", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchEdge::getVirtualRadius, SketchEdge::setVirtualRadius, "virtualRadius", 10);
                    addBooleanPropertyWidget(SketchEdge::getPullOnly, SketchEdge::setPullOnly, "pullOnly", 10);
                    addPhysicsMaterial(SketchEdge::getPhysicsMaterial, SketchEdge::setPhysicsMaterial, "physicsMaterial.short", 10, () -> this.editorInstance.getSketchWeave().getPhysicsMaterials());

                    addSeparator();
                    addChild(ButtonWidget.builder(Text.of("Set Length to Current"), (b) -> {
                        if(dpw.list == null || dpw.list.isEmpty()) return;
                        List<SketchEdge> edges = new ArrayList<>();
                        List<Double> oldLengths = new ArrayList<>();
                        List<Double> newLengths = new ArrayList<>();
                        for(SketchEdge edge : dpw.list) {
                            double length = edge.getCurrentActualLength();
                            if(length != edge.length) {
                                edges.add(edge);
                                oldLengths.add(edge.length);
                                newLengths.add(length);
                            }
                        }
                        if(edges.isEmpty() || newLengths.isEmpty()) return;

                        EditorAction action = new ModifyVariablesAction<>(edges, oldLengths, newLengths, SketchEdge::setLength);
                        this.editorInstance.doAction(action);
                    }).width(getInteriorWidth()).build());
                    addLine();
                }
            };
        } else if(type == SketchFace.class) {
            this.propertyHandler = new PropertyHandler<>(SketchFace.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    addBooleanPropertyWidget(SketchFace::isDoubleSided, SketchFace::setDoubleSided, "doubleSided", 10);
                    addPhysicsMaterial(SketchFace::getPhysicsMaterial, SketchFace::setPhysicsMaterial, "physicsMaterial.short", 10, () -> this.editorInstance.getSketchWeave().getPhysicsMaterials());
                    DoublePropertyWidget<SketchFace> dpw = (DoublePropertyWidget<SketchFace>)addPropertyWidget(DoublePropertyWidget::new, SketchFace::getArea, SketchFace::setArea, "area", 10);

                    addSeparator();
                    addChild(ButtonWidget.builder(Text.of("Set Area to Current"), (b) -> {
                        if(dpw.list == null || dpw.list.isEmpty()) return;
                        List<SketchFace> faces = new ArrayList<>();
                        List<Double> oldAreas = new ArrayList<>();
                        List<Double> newAreas = new ArrayList<>();
                        for(SketchFace face : dpw.list) {
                            double area = face.getCurrentActualArea();
                            if(area != face.area) {
                                faces.add(face);
                                oldAreas.add(face.area);
                                newAreas.add(area);
                            }
                        }
                        if(faces.isEmpty() || newAreas.isEmpty()) return;

                        EditorAction action = new ModifyVariablesAction<>(faces, oldAreas, newAreas, SketchFace::setArea);
                        this.editorInstance.doAction(action);
                    }).width(getInteriorWidth()).build());
                    addLine();
                }
            };
        } else if(type == SketchTransform.class) {
            this.propertyHandler = new PropertyHandler<>(SketchTransform.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getX, SketchTransform::setX, "x", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getY, SketchTransform::setY, "y", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getZ, SketchTransform::setZ, "z", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getPitchDeg, SketchTransform::setPitchDeg, "pitch", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getYawDeg, SketchTransform::setYawDeg, "yaw", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getRollDeg, SketchTransform::setRollDeg, "roll", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getSizeX, SketchTransform::setSizeX, "xScale", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getSizeY, SketchTransform::setSizeY, "yScale", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchTransform::getSizeZ, SketchTransform::setSizeZ, "zScale", 10);
                }
            };
        } else if(type == SketchPhysicsMaterial.class) {
            this.propertyHandler = new PropertyHandler<>(SketchPhysicsMaterial.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    addPropertyWidget(TextPropertyWidget::new, SketchPhysicsMaterial::getName, SketchPhysicsMaterial::setName, "name", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchPhysicsMaterial::getDensity, SketchPhysicsMaterial::setDensity, "density", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchPhysicsMaterial::getElasticModulus, SketchPhysicsMaterial::setElasticModulus, "elasticModulus", 10);
                }
            };
        } else if(type == SketchRenderMaterial.class) {
            this.propertyHandler = new PropertyHandler<>(SketchRenderMaterial.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    addPropertyWidget(TextPropertyWidget::new, SketchRenderMaterial::getName, SketchRenderMaterial::setName, "name", 10);
                    addPropertyWidget(IntegerPropertyWidget::new, SketchRenderMaterial::getR, SketchRenderMaterial::setR, "r", 10);
                    addPropertyWidget(IntegerPropertyWidget::new, SketchRenderMaterial::getG, SketchRenderMaterial::setG, "g", 10);
                    addPropertyWidget(IntegerPropertyWidget::new, SketchRenderMaterial::getB, SketchRenderMaterial::setB, "b", 10);
                    addPropertyWidget(IntegerPropertyWidget::new, SketchRenderMaterial::getA, SketchRenderMaterial::setA, "a", 10);
                    addPropertyWidget(TextPropertyWidget::new, SketchRenderMaterial::getNamespace, SketchRenderMaterial::setNamespace, "namespace", 10);
                    addPropertyWidget(TextPropertyWidget::new, SketchRenderMaterial::getPath, SketchRenderMaterial::setPath, "path", 10);
                    addBooleanPropertyWidget(SketchRenderMaterial::getUseTextureAtlas, SketchRenderMaterial::setUseTextureAtlas, "useTexAtlas", 10);
                    addChild(new TexturePreviewWidget<>(SketchRenderMaterial::getNamespace, SketchRenderMaterial::getPath, SketchRenderMaterial::getUseTextureAtlas, 128, 128));
                }
            };
        } else if(type == SketchSettings.class) {
            this.propertyHandler = new PropertyHandler<>(SketchSettings.class, this.editorInstance, this.textRenderer, this.getInteriorWidth()) {
                @Override
                public void setup() {
                    putCounterHeader();
                    addPropertyWidget(TextPropertyWidget::new, SketchSettings::getName, SketchSettings::setName, "name", 10);

                    IntegerPropertyWidget<SketchSettings> ipw1 = (IntegerPropertyWidget<SketchSettings>)addPropertyWidget(IntegerPropertyWidget::new, SketchSettings::getStepMain, SketchSettings::setStepMain, "stepMain", 10);
                    IntegerPropertyWidget<SketchSettings> ipw2 = (IntegerPropertyWidget<SketchSettings>)addPropertyWidget(IntegerPropertyWidget::new, SketchSettings::getStepWind, SketchSettings::setStepWind, "stepWind", 10);
                    ipw1.setMin(-1);
                    ipw2.setMin(-1);
                    ipw1.setMax(5);
                    ipw2.setMax(5);
                    addPropertyWidget(DoublePropertyWidget::new, SketchSettings::getWindMultiplier, SketchSettings::setWindMultiplier, "windMultiplier", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchSettings::getGravityMultiplier, SketchSettings::setGravityMultiplier, "gravityMultiplier", 10);
                    addBooleanPropertyWidget(SketchSettings::getDoCulling, SketchSettings::setDoCulling, "doCulling", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchSettings::getHeight, SketchSettings::setHeight, "height", 10);
                    addPropertyWidget(DoublePropertyWidget::new, SketchSettings::getWidth, SketchSettings::setWidth, "width", 10);
                }
            };
            ((PropertyHandler<SketchSettings>)this.propertyHandler).setList(List.of(editorInstance.getSketchWeave().getSettings()));
        } else {
            this.propertyHandler = null;
        }
    }

    public void setup(@Nullable List<Class<? extends SketchElement>> types, @Nullable Class<? extends SketchElement> type) {
        this.clearChildren();

        if(types != null) {
            this.setupTypeButtons(types, type);
        }

        this.setupPropertyHandler(type);
        if(this.propertyHandler != null) {
            onSelectionChanged();
            addLine();
            this.addChildren(this.propertyHandler.getChildren());
        }

        this.positionChildren();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocusedChild() instanceof GenericPropertyWidget) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                this.setFocused(null);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
