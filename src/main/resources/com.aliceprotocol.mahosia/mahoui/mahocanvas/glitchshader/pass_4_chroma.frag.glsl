#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass3;
uniform float uChromaAmount;

void main() {
    vec2 uv = vUv;
    float chroma = uChromaAmount;
    float r = texture(uPass3, uv + vec2( chroma, 0.0)).r;
    float g = texture(uPass3, uv).g;
    float b = texture(uPass3, uv + vec2(-chroma, 0.0)).b;
    fragColor = vec4(r, g, b, 1.0);
}