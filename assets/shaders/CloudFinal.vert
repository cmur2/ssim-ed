
uniform mat4  g_WorldViewProjectionMatrix;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec2 varTexCoord;

void main() { 
    varTexCoord = inTexCoord;
    
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0); 
}
