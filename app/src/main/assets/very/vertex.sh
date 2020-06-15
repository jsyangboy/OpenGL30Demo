#version 300 es
precision mediump float;
layout(location = 0) in ver4 aColor;
layout(location = 1) in ver4 aPosition;
out ver4 vColor;
void main(){
  vColor = aColor;
  gl_Position = aPosition;
}