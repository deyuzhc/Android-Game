package com.deyuz.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/1.
 */
public class Circle {

    // 半径
    private float r;
    // 圆心位置
    private Vec2 o;

    // 长宽比
    private float ratio = 1.0f;


    Circle(float ratio) {
        o = new Vec2();
        r = 1.0f;
        this.ratio = ratio;
    }

    Circle(Vec2 o, float r, float ratio) {
        this.o = o;
        this.r = r;
        this.ratio = ratio;
    }

    void setPos(Vec2 o) {
        this.o = o;
    }

    Vec2 getPos() {
        return o;
    }

    void setR(float r) {
        this.r = r;
    }

    float getR() {
        return r;
    }

    // 绘制圆的函数
    void draw(GL10 gl, int slice) {

        FloatBuffer vertexBuffer;//顶点坐标数据缓冲
        FloatBuffer colorBuffer;//顶点着色数据缓冲
        ByteBuffer indexBuffer;//顶点构建索引数据缓冲

        //------------------------------------------

        float[] vertices = new float[slice * 2];
        float[] colors = new float[slice * 4];
        byte indexes[] = new byte[slice * 2];

        for (int i = 0; i < slice; i++) {
            // 顶点数组
            // 位于边上的点
            if (i < slice - 1) {
                vertices[i * 2 + 0] = r * (float) Math.cos(2 * Math.PI / (slice - 1) * i) + o.x;
                vertices[i * 2 + 1] = ratio * r * (float) Math.sin(2 * Math.PI / (slice - 1) * i) + o.y;

                indexes[i * 2 + 0] = (byte) i;
                indexes[i * 2 + 1] = (byte) (slice - 1);

                // 颜色数组
                colors[i * 4 + 0] = 0.5f;//(float) i / slice; //new Random().nextFloat();
                colors[i * 4 + 1] = 0.5f;//(float) i / slice;//new Random().nextFloat();
                colors[i * 4 + 2] = 0.5f;//(float) i / slice;//new Random().nextFloat();
                colors[i * 4 + 3] = 1.0f;
            }
            // 位于圆心的点
            else {
                vertices[i * 2 + 0] = o.x;
                vertices[i * 2 + 1] = o.y;

                indexes[i * 2 + 0] = 0;
                indexes[i * 2 + 1] = (byte) (slice - 1);

                // 颜色数组
                colors[i * 4 + 0] = 1.0f; //new Random().nextFloat();
                colors[i * 4 + 1] = 1.0f;//new Random().nextFloat();
                colors[i * 4 + 2] = 1.0f;//new Random().nextFloat();
                colors[i * 4 + 3] = 1.0f;
            }
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);//创建顶点坐标数据缓冲
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        vertexBuffer = vbb.asFloatBuffer();//转换为float型缓冲
        vertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        vertexBuffer.position(0);//设置缓冲区起始位置


        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);//创建顶点着色数据缓冲
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        colorBuffer = cbb.asFloatBuffer();//转换为float型缓冲
        colorBuffer.put(colors);//向缓冲区中放入顶点着色数据
        colorBuffer.position(0);//设置缓冲区起始位置

        //------------------------------------------


        indexBuffer = ByteBuffer.allocateDirect(indexes.length);//创建三角形构造索引数据缓冲
        indexBuffer.put(indexes);//向缓冲区中放入三角形构造索引数据
        indexBuffer.position(0);//设置缓冲区起始位置

        //------------------------------------------

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);//启用顶点坐标数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);//启用顶点颜色数组

        //为画笔指定顶点坐标数据
        gl.glVertexPointer
                (
                        2,                //每个顶点的坐标数量为2  xy
                        GL10.GL_FLOAT,    //顶点坐标值的类型为 GL_FLOAT
                        0,                //连续顶点坐标数据之间的间隔
                        vertexBuffer    //顶点坐标数据
                );

        //为画笔指定顶点着色数据
        gl.glColorPointer
                (
                        4,                //设置颜色的组成成分，必须为4—RGBA
                        GL10.GL_FLOAT,    //顶点颜色值的类型为 GL_FLOAT
                        0,                //连续顶点着色数据之间的间隔
                        colorBuffer        //顶点着色数据
                );

        //绘制图形
        gl.glDrawElements
                (
                        GL10.GL_TRIANGLE_STRIP,        //以三角形方式填充
                        2 * slice,                //顶点个数
                        GL10.GL_UNSIGNED_BYTE,    //索引值的尺寸
                        indexBuffer                //索引值数据
                );
    }
}
