package com.example.excavop.ui.theme

import android.opengl.GLES32
import android.util.Log
import java.nio.IntBuffer

fun checkShaderCompilation(type: Int, shader: Int) {
    val status = IntBuffer.allocate(1)
    GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, status)
    if (status[0] == 0) {
        Log.e("SHADER", "$type, ${GLES32.glGetShaderInfoLog(shader)}")
    }
}
fun checkProgramLinking(program: Int) {
    val status = IntBuffer.allocate(1)
    GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, status)
    if (status[0] == 0) {
        Log.e("PROGRAM_LINK", GLES32.glGetProgramInfoLog(program))
    }
}

open class GLProgram(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val geometryShaderCode: String? = null
) {
    protected var mProgram: Int = 0

    open fun buildProgram() {
        val vertShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val geomShader: Int? = if(geometryShaderCode == null) null else loadShader(GLES32.GL_GEOMETRY_SHADER, geometryShaderCode)

        mProgram = GLES32.glCreateProgram()
        GLES32.glAttachShader(mProgram, vertShader)
        GLES32.glAttachShader(mProgram, fragShader)
        if (geomShader != null) GLES32.glAttachShader(mProgram, geomShader)
        GLES32.glLinkProgram(mProgram)
        checkProgramLinking(mProgram)
    }

    public fun use() {
        GLES32.glUseProgram(mProgram)
    }
    private fun loadShader(type: Int, shaderCode: String): Int{
        val shader = GLES32.glCreateShader(type)
        GLES32.glShaderSource(shader, shaderCode)
        GLES32.glCompileShader(shader)
        checkShaderCompilation(type, shader)
        return shader;
    }
}
