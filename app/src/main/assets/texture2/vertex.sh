#version 300 es
precision mediump float;

layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 aTextureCoord;
//矩阵
uniform mat4 u_Matrix;
out vec2 vTextCoord;
void main(){
  gl_Position = u_Matrix*aPosition;
  //gl_PointSize = 10.0;
  vTextCoord = aTextureCoord;
}