package kr.ac.hallym.prac03

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private  lateinit var mainSurfaceView: MainGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainSurfaceView = MainGLSurfaceView(this)
        setContentView(mainSurfaceView)
        supportActionBar?.hide()
    }
}