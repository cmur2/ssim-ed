
// 0.0 - fading starts at center
// 0.5 - no fading
const float DIST_OFFSET = 1.0;

// 0.0 - no falloff in alpha
const float FALLOFF = 2.0;

uniform sampler2D m_ColorMap; // tex unit 0

varying vec2 varTexCoord;

void main() {
    vec4 color = texture2D(m_ColorMap, varTexCoord);

    // discard pixel if it's fully transparent black pixel which
    // is the default value of uninitialized texture pixels
    // (no rendering was performed here)
    if(color == vec4(0.0, 0.0, 0.0, 1.0)) discard;

    // implement radial alpha fading from center
    vec2 diff = max(abs(varTexCoord - 0.5) - DIST_OFFSET, 0.0); // at least 0
    float dist = max(diff.s, diff.t);
    color.a *= clamp(pow(1.0 - FALLOFF * dist, 2.0), 0.0, 1.0);

    gl_FragColor = color;
}
