
uniform mat4 g_WorldViewProjectionMatrix;

uniform vec4 g_LightColor;

attribute vec3 inPosition;
attribute vec4 inColor;

varying vec4 varColor;

void main() {
    varColor = inColor * vec4(g_LightColor.xyz, 1.0);

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}