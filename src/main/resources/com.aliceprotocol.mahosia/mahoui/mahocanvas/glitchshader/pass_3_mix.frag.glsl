#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass0;
uniform sampler2D uPass1;
uniform sampler2D uPass2;

uniform float uMixWeight0;
uniform float uMixWeight1;
uniform float uMixWeight2;

uniform float uTime;
uniform vec2 uRes;

float hash(vec2 p) {
    p = fract(p * vec2(443.897, 441.423));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = vUv;

    vec3 c0 = texture(uPass0, uv).rgb;
    vec3 c1 = texture(uPass1, uv).rgb;
    vec3 c2 = texture(uPass2, uv).rgb;

    float stripe = sin(uv.y * uRes.y * 1.5 + uTime * 0.8) * 0.5 + 0.5;
    float zone = floor(uv.y * uRes.y / 24.0);
    float noise = hash(vec2(zone, floor(uTime * 3.0)));

    float w0 = uMixWeight0 + noise * 0.15;
    float w1 = uMixWeight1 + (1.0 - stripe) * 0.2;
    float w2 = uMixWeight2 + stripe * 0.2;

    float total = w0 + w1 + w2;
    if (total <= 0.0) total = 1.0;

    vec3 mixed = (c0 * w0 + c1 * w1 + c2 * w2) / total;

    fragColor = vec4(mixed, 1.0);
}