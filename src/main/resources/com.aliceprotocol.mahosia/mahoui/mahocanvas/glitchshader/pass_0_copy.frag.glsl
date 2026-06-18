#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uTex;

void main() {
    vec2 uv = vec2(vUv.x, 1.0 - vUv.y);
    fragColor = texture(uTex, uv);
}