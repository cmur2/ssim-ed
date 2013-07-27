
uniform vec4 m_Color;
uniform samplerCube m_SkyBox;

varying vec3 varVertex; // in body (world) coords.

void main() {
    //gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //gl_FragColor = m_Color;
    gl_FragColor = textureCube(m_SkyBox, (varVertex));
}