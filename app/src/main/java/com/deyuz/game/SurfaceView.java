package com.deyuz.game;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/3/20.
 */
public class SurfaceView extends GLSurfaceView {


    private String log = "----->SurfaceView";
    private mRender mRender;
    private Maze maze;
    private Ball ball;
    private Sensor sensor;
    private KdTree kdTree;
    private float ratio;

    public SurfaceView(Context context, Maze maze, Ball ball, Sensor sensor, KdTree kdTree) {
        super(context);

        // 待渲染物体
        this.maze = maze;
        this.ball = ball;

        this.sensor = sensor;
        this.kdTree = kdTree;

        //创建渲染器
        mRender = new mRender();

        //设置渲染器
        this.setRenderer(mRender);
        //设置渲染模型
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    //内部类--渲染器
    class mRender implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {

            GLES20.glClearColor(0.65f, 1.0f, 1.0f, 0.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int w, int h) {

            //Log.i(log, "1\t" + Thread.currentThread().getName());

            // w 1080 h 1920
            GLES20.glViewport(0, 0, w, h);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            //Log.i(log, "2\t" + Thread.currentThread().getName());

            gl.glEnable(GL10.GL_LINE_SMOOTH);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            ball.roll(sensor.getDir(), kdTree);

            ball.draw(gl, 45);

            maze.draw(gl, ratio);

            //kdTree.drawTree(gl, kdTree.root);
        }
    }

}
