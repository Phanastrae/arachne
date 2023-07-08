#version 150

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float x = texCoord0.x - 0.5;
    float y = texCoord0.y - 0.5;
    float r2 = (x*x+y*y) * 4;
    if(r2 > 1) {
        discard;
    }
    // dark border
    float f = (1-r2*r2*r2);
    f = f * f;
    f = 0.2 + 0.8 * f;

    vec4 color = vertexColor * f;
    fragColor = color * ColorModulator;
}
