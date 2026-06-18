#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass4;
uniform float uFlashAmount;
uniform float uFlashSize;
uniform float uFlashSpeed;
uniform float uTime;

float hash(vec2 p) {
    p = fract(p * vec2(114.514, 233.666));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = vUv;
    float row = floor(uv.y / max(uFlashSize, 0.001));
    float seed = hash(vec2(row, floor(uTime * uFlashSpeed)));
    float flashMask = step(1.0 - uFlashAmount, seed);
    float flashSign = step(0.5, seed) * 2.0 - 1.0;
    vec3 color = texture(uPass4, uv).rgb;
    color = mix(color, flashSign > 0.0 ? vec3(1.0) : vec3(0.0), flashMask * 0.5);
    fragColor = vec4(color, 1.0);
}