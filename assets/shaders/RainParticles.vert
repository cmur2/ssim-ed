
uniform mat4 g_WorldViewProjectionMatrix;

uniform vec4 g_LightColor;

attribute vec3 inPosition;
attribute vec4 inColor;
attribute float inTexCoord;

varying vec4 varColor;
varying float varRelId;

void main() {
    varColor = inColor * vec4(g_LightColor.xyz, 1.0);
    varRelId = inTexCoord;

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}