#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass0;
uniform float uDistortAmount;

void main() {
    vec2 uv = vUv;
    vec2 cc = uv - 0.5;
    float r2 = dot(cc, cc);
    vec2 distUv = uv + cc * r2 * uDistortAmount;
    fragColor = texture(uPass0, distUv);
}