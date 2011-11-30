
uniform mat4 g_WorldViewProjectionMatrix;

attribute vec3 inPosition;

varying vec3 varVertex; // in body (world) coords.

void main(){
    varVertex = vec3(inPosition);
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}