#version 300 es
precision mediump float;

layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 aTextureCoord;
out vec2 vTextCoord;
void main(){
  gl_Position = aPosition;
  vTextCoord = aTextureCoord;
}