#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform float uTime;
uniform vec2 uRes;
uniform float uFrame;

void main() {
    vec2 uv = vUv;
    vec2 pixel = uv * uRes;
    vec2 center = uRes * 0.5;
    float dist = length(pixel - center) / max(uRes.x, uRes.y);

    float wave = 0.5 + 0.5 * sin(24.0 * dist - uTime * 4.0);
    float gridX = step(0.96, fract(uv.x * 16.0));
    float gridY = step(0.96, fract(uv.y * 16.0));
    float grid = max(gridX, gridY);

    vec3 base = vec3(uv.x, uv.y, 0.5 + 0.5 * sin(uTime));
    vec3 waveColor = vec3(wave, 0.35 + 0.65 * wave, 1.0 - wave);
    vec3 color = mix(base, waveColor, 0.45);

    color = mix(color, vec3(1.0), grid * 0.35);
    fragColor = vec4(color, 1.0);
}