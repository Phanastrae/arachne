package phanastrae.old.link_type;

import net.minecraft.util.math.Vec3d;
import phanastrae.old.Node;

public class StringLink extends Link {

    public double idealLength;
    public double strengthConstant;
    public boolean pullOnly = true;

    public StringLink(Node node1, Node node2) {
        this(1/16f, 2000, node1, node2); // TODO: adjust defaults?
    }

    public StringLink(double idealLength, double strengthConstant, Node node1, Node node2) {
        super(node1, node2);
        this.idealLength = idealLength;
        this.strengthConstant = strengthConstant;
    }

    @Override
    public void tickLink(double dt) {
        Vec3d node1to2 = node2.pos.subtract(node1.pos);
        double currentLength = node1to2.length();
        double lengthDifference = currentLength - this.idealLength;
        if(currentLength == 0) return;
        if(lengthDifference <= 0 && this.pullOnly) return;

        // scale to length and multiply by constant
        node1to2 = node1to2.multiply(this.strengthConstant * lengthDifference/currentLength); //TODO: excessive values can cause instability, find way to fix this

        node1.addForce(node1to2);
        node2.addForce(node1to2.multiply(-1));
    }
}