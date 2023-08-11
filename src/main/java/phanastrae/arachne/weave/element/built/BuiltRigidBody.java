package phanastrae.arachne.weave.element.built;

import phanastrae.arachne.weave.element.sketch.SketchTransform;

public class BuiltRigidBody {
    public final int id;

    public BuiltRigidBody(SketchTransform rigidBody) {
        this.id = rigidBody.id;
    }
}
