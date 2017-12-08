package com.deyuz.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/1.
 */
public class Rect {

    // 左下角
    Vec2 min;
    // 右上角
    Vec2 max;

    Rect() {
        min = new Vec2();
        max = new Vec2();
    }

    Rect(Vec2 v1, Vec2 v2) {
        // 取小
        min = new Vec2(v1.x < v2.x ? v1.x : v2.x, v1.y < v2.y ? v1.y : v2.y);
        // 取大
        max = new Vec2(v1.x >= v2.x ? v1.x : v2.x, v1.y >= v2.y ? v1.y : v2.y);
    }

    void draw(GL10 gl, float ratio, Vec3 c) {
        FloatBuffer vBuffer;
        FloatBuffer cBuffer;
        ByteBuffer iBuffer;

        float color[] = new float[4 * 4];
        float vertex[] = {
                min.x, min.y,
                max.x, min.y,
                max.x, max.y,
                min.x, max.y
        };
        byte index[] = {
                0, 1, 2,
                2, 3, 0
        };

        Random rand = new Random(10);
        for (int i = 0; i < 4; i++) {

            /*vertex[i * 2 + 0] = new Random().nextFloat();
            vertex[i * 2 + 1] = new Random().nextFloat();*/

            color[i * 4 + 0] = c.x;
            color[i * 4 + 1] = c.y;
            color[i * 4 + 2] = c.z;
            color[i * 4 + 3] = 1.0f;
        }


        ByteBuffer vbb = ByteBuffer.allocateDirect(vertex.length * 4);//创建顶点坐标数据缓冲
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        vBuffer = vbb.asFloatBuffer();//转换为float型缓冲
        vBuffer.put(vertex);//向缓冲区中放入顶点坐标数据
        vBuffer.position(0);//设置缓冲区起始位置

        ByteBuffer cbb = ByteBuffer.allocateDirect(color.length * 4);//创建顶点着色数据缓冲
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        cBuffer = cbb.asFloatBuffer();//转换为float型缓冲
        cBuffer.put(color);//向缓冲区中放入顶点着色数据
        cBuffer.position(0);//设置缓冲区起始位置

        iBuffer = ByteBuffer.allocateDirect(index.length);//创建三角形构造索引数据缓冲
        iBuffer.put(index);//向缓冲区中放入三角形构造索引数据
        iBuffer.position(0);//设置缓冲区起始位置

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);//启用顶点坐标数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);//启用顶点颜色数组

        //为画笔指定顶点坐标数据
        gl.glVertexPointer
                (
                        2,                //每个顶点的坐标数量为2  xy
                        GL10.GL_FLOAT,    //顶点坐标值的类型为 GL_FLOAT
                        0,                //连续顶点坐标数据之间的间隔
                        vBuffer    //顶点坐标数据
                );

        //为画笔指定顶点着色数据
        gl.glColorPointer
                (
                        4,                //设置颜色的组成成分，必须为4—RGBA
                        GL10.GL_FLOAT,    //顶点颜色值的类型为 GL_FLOAT
                        0,                //连续顶点着色数据之间的间隔
                        cBuffer        //顶点着色数据
                );

        //绘制图形
        gl.glDrawElements
                (
                        GL10.GL_TRIANGLE_STRIP,        //以三角形方式填充
                        6,                //顶点个数
                        GL10.GL_UNSIGNED_BYTE,    //索引值的尺寸
                        iBuffer                //索引值数据
                );

    }
}
