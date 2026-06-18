#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass1;
uniform float uTearAmount;
uniform float uTearFrequency;
uniform vec2 uRes;
uniform float uFrame;

float hash(vec2 p) {
    p = fract(p * vec2(114.514, 233.666));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = vUv;
    float row = floor(uv.y * uRes.y);
    float tearSeed = hash(vec2(row, floor(uFrame * 0.5)));
    float tearMask = step(0.5, hash(vec2(row, floor(uFrame * 0.25)))) * uTearFrequency * 2.0;
    vec2 tearUv = uv;
    tearUv.x += (tearSeed - 0.5) * uTearAmount * tearMask;
    tearUv = clamp(tearUv, vec2(0.0), vec2(1.0));
    fragColor = texture(uPass1, tearUv);
}