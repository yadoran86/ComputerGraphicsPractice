package kr.ac.hallym.prac03

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

const val COORDS_PER_VERTEX = 3

val eyePos = floatArrayOf(0.0f, 3.0f, 3.0f)
val lightDir = floatArrayOf(1.0f, 1.0f, 1.0f)
val lightAmbient = floatArrayOf(0.1f, 0.1f, 0.1f)
val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)

class MainGLRenderer(val context: Context):GLSurfaceView.Renderer {
    private lateinit var mGround: MyLitTexGround
    private lateinit var mHexa: MyLitHexa
    private lateinit var mCube: MyLitCube

    private var modelMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)
    private var vpMatrix = FloatArray(16)
    private var mvpMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    )

    private var startTime = SystemClock.uptimeMillis()
    private var rotYAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(vpMatrix, 0)

        mGround = MyLitTexGround(context)
        mHexa = MyLitHexa(context)
        mCube = MyLitCube(context)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, ratio, 0.001f, 1000f)

        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], 0f, 0f, 0f, 0f, 1f, 0f)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        mGround.draw(mvpMatrix, modelMatrix)

        val endTime = SystemClock.uptimeMillis()
        val angle = 0.1f * (endTime - startTime).toFloat()
        startTime = endTime
        rotYAngle += angle
        var rotYMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotYMatrix, 0, rotYAngle, 0f, 1f, 0f)

        var rotMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotMatrix, 0, 45f, 0f, 0f, 1f)
        Matrix.multiplyMM(rotMatrix, 0, rotYMatrix, 0, rotMatrix, 0)

        for(z in -5 .. 0 step 2){
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, 3f, 0f, z.toFloat())
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix,modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, 3f, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix,modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -3f, 0f, z.toFloat())
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix,modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -3f, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix,modelMatrix)
        }

    }
}

fun loadShader(type: Int, filename: String, myContext: Context): Int {

    return GLES30.glCreateShader(type).also { shader ->

        val inputStream = myContext.assets.open(filename)
        val inputBuffer = ByteArray(inputStream.available())
        inputStream.read(inputBuffer)
        val shaderCode = String(inputBuffer)

        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        val compiled = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled)
        if(compiled.get(0) == 0) {
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, compiled)
            if(compiled.get(0) > 1) {
                Log.e("Shader", "$type shader: "+ GLES30.glGetShaderInfoLog(shader))
            }
            GLES30.glDeleteShader(shader)
            Log.e("Shader", "$type shader compile error.")
        }
    }
}

fun loadBitmap(filename: String, myContext: Context): Bitmap {
    val manager = myContext.assets
    val inputStream = BufferedInputStream(manager.open(filename))
    val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
    return bitmap!!
}