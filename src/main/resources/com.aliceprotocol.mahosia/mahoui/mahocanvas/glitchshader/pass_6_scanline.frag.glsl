#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass5;
uniform float uScanlineAmount;
uniform vec2 uRes;

void main() {
    vec2 uv = vUv;
    float scan = sin(uv.y * uRes.y * 3.14159) * 0.5 + 0.5;
    vec3 color = texture(uPass5, uv).rgb;
    color *= mix(1.0, scan, uScanlineAmount);
    fragColor = vec4(color, 1.0);
}