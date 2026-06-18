#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform float uTime;
uniform vec2 uRes;

void main() {
    vec2 uv = vec2(vUv.x, 1.0 - vUv.y);
    vec4 texColor = texture(uTexture, uv);

    float wave = 0.03 * sin(uv.y * 40.0 + uTime * 4.0);
    vec2 waveUv = vec2(uv.x + wave, uv.y);

    fragColor = texture(uTexture, waveUv) * texColor;
}