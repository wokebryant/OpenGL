uniform mat4 vMatrix;
attribute vec4 vPosition;

void main() {
    gl_Position = vMatrix * vPosition;
//    gl_PointSize = 1.0;
}
