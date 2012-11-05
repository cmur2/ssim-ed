
uniform float m_RelIdLimit;

varying vec4 varColor;
varying float varRelId;

void main() {
    // discard all rain drop fragments (that should be all of one rain drop line)
    // with a relative ID greater than a given threshold:
    if(varRelId > m_RelIdLimit) {
        discard;
    }

    vec4 color = varColor;
    
    gl_FragColor = color;
}