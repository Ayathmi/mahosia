#version 460 core

in vec2 vUv;
out vec4 fragColor;

uniform sampler2D uTex;
uniform float uTime;
uniform vec2 uRes;
uniform float uFrame;

uniform float uFlashAmount;
uniform float uFlashSize;
uniform float uFlashSpeed;

uniform float uVignetteAmount;
uniform float uVignetteFalloff;

uniform float uTearAmount;
uniform float uTearFrequency;

uniform float uDistortAmount;

uniform float uNoiseAmount;
uniform float uScanlineAmount;
uniform float uChromaAmount;

float hash(vec2 p) {
    p = fract(p * vec2(114.514, 233.666));
    p += dot(p, p + 19.19);
    return fract(p.x  * p.y);
}

float blockNoise(vec2 uv, float cellH) {
    float row = floor(uv.y / cellH);
    float seed = hash(vec2(row, floor(uTime * uFlashSpeed)));
    return seed;
}

void main() {
    vec2 uv = vec2 (vUv.x, 1.0 - vUv.y);

    vec2 distorted = uv - 0.5;
    float r2 = dot(distorted, distorted);
    vec2 distUv = uv + distorted * r2 * uDistortAmount;

    float row = floor(distUv.y * uRes.y);
    float tearSeed = hash(vec2(row, floor(uFrame * 0.5)));
    float tearMask = step(0.5, hash(vec2(row, floor(uFrame * 0.25)))) * uTearFrequency * 2.0;
    distUv.x += (tearSeed - 0.5) * uTearAmount * tearMask;

    vec2 sampleUv = clamp(distUv, vec2(0.0), vec2(1.0));

    float chorma = uChromaAmount;
    float r = texture(uTex, sampleUv + vec2(chorma, 0.0)).r;
    float g = texture(uTex, sampleUv).g;
    float b = texture(uTex, sampleUv + vec2(-chorma, 0.0)).b;
    vec3 color = vec3(r, g, b);

    float flashSeed = blockNoise(uv, uFlashSize);
    float flashMask = step(1.0 - uFlashAmount, flashSeed);
    float flashSign = step(0.5, flashSeed) * 2.0 - 1.0;
    color = mix(color, flashSign > 0.0 ? vec3(1.0) : vec3(0.0), flashMask * 0.5);

    float scan = sin(uv.y * uRes.y * 3.14159) * 0.5 + 0.5;
    color *= mix(1.0, scan, uScanlineAmount);

    float n = hash(uv * uRes + uTime);
    color += (n - 0.5) * uNoiseAmount;

    float dist = length(distorted) * 1.4142;
    float vig = smoothstep(1.0, 1.0 - uVignetteAmount, pow(dist, uVignetteFalloff));
    color *= vig;

    fragColor = vec4(color, 1.0);
}