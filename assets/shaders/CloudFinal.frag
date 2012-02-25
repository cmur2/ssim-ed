
uniform sampler2D m_ColorMap; // tex unit 0

varying vec2 varTexCoord;

void main() {
    vec4 color = texture2D(m_ColorMap, varTexCoord);
    if(color == vec4(0.0, 0.0, 0.0, 1.0)) discard; 
    gl_FragColor = color;
}
