#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uPass6;
uniform float uNoiseAmount;
uniform float uVignetteAmount;
uniform float uVignetteFalloff;
uniform float uTime;
uniform vec2 uRes;

float hash(vec2 p) {
    p = fract(p * vec2(114.514, 233.666));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = vUv;
    vec3 color = texture(uPass6, uv).rgb;

    float n = hash(uv * uRes + uTime);
    color += (n - 0.5) * uNoiseAmount;

    vec2 cc = uv - 0.5;
    float dist = length(cc) * 1.4142;
    float vig = smoothstep(1.0, 1.0 - uVignetteAmount, pow(dist, uVignetteFalloff));
    color *= vig;

    fragColor = vec4(color, 1.0);
}