package com.deyuz.game;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/1.
 */
public class AABB {
    // 左下角
    Vec2 min;
    // 右上角
    Vec2 max;

    private String log = "----->AABB";

    AABB() {
        min = new Vec2();
        max = new Vec2();

    }

    AABB(Vec2 min, Vec2 max) {
        this.min = new Vec2(min.x < max.x ? min.x : max.x, min.y < max.y ? min.y : max.y);
        // 取大
        this.max = new Vec2(min.x >= max.x ? min.x : max.x, min.y >= max.y ? min.y : max.y);


    }

    AABB(Rect rect) {
        min = rect.min;
        max = rect.max;

    }

    // 求交函数
    boolean Intersect(Circle circle) {
        return false;
    }

    boolean Intersect(Vec2 pos) {
        if (pos.x < max.x && pos.x > min.x && pos.y < max.y && pos.y > min.y) {
            return true;
        }
        return false;
    }

    boolean Intersect(Vec2 pos, float r) {
        Vec2 boxMin = new Vec2(pos.x - r, pos.y - r);
        Vec2 boxMax = new Vec2(pos.x + r, pos.y + r);

        if (max.x < boxMin.x || max.y < boxMin.y || min.x > boxMax.x || min.y > boxMax.y) {
            return false;
        }

        return true;
    }

    boolean Intersect(Rect rect) {
        if (max.x < rect.min.x || max.y < rect.min.y || min.x > rect.max.x || min.y > rect.max.y) {
            return false;
        }
        return true;
    }

    void setPos(Vec2 pos) {
        float dx = (max.x - min.x) / 2;
        float dy = (max.y - min.y) / 2;

        min.x = pos.x - dx;
        min.y = pos.y - dy;

        max.x = pos.x + dx;
        max.y = pos.y + dy;

    }

    // 此函数仅作测试用
    void draw(GL10 gl, float ratio) {
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

            color[i * 4 + 0] = 1.0f;
            color[i * 4 + 1] = 0.0f;
            color[i * 4 + 2] = 0.0f;
            color[i * 4 + 3] = 0.5f;
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

    // 获取进入图元的深度
    Vec2 getEnterDis(Rect rect) {

        Vec2 enterDis = new Vec2();

        Vec2 vList[] = new Vec2[4];

        vList[0] = new Vec2((rect.min.x + rect.max.x) / 2, rect.max.y);
        vList[1] = new Vec2(rect.max.x, (rect.min.y + rect.max.y) / 2);
        vList[2] = new Vec2((rect.min.x + rect.max.x) / 2, rect.min.y);
        vList[3] = new Vec2(rect.min.x, (rect.min.y + rect.max.y) / 2);

        //********************
        //*****菱形包围盒
        //********************
        // 上入
        if (vList[0].x > min.x && vList[0].x < max.x && vList[0].y > min.y && vList[0].y < max.y) {
            enterDis = new Vec2(0.0f, min.y - vList[0].y);
            //Log.i(log, "下");
            return enterDis;
        }
        // 右入
        if (vList[1].x > min.x && vList[1].x < max.x && vList[1].y > min.y && vList[1].y < max.y) {
            enterDis = new Vec2(min.x - vList[1].x, 0.0f);
            //Log.i(log, "左");
            return enterDis;
        }
        // 下入
        if (vList[2].x > min.x && vList[2].x < max.x && vList[2].y > min.y && vList[2].y < max.y) {
            enterDis = new Vec2(0.0f, max.y - vList[2].y);
            //Log.i(log, "上");
            return enterDis;
        }
        // 左入
        if (vList[3].x > min.x && vList[3].x < max.x && vList[3].y > min.y && vList[3].y < max.y) {
            enterDis = new Vec2(max.x - vList[3].x, 0.0f);
            //Log.i(log, "右");
            return enterDis;
        }

        //********************
        //*****包围盒穿透
        //********************
        // 右
        if (vList[0].x < min.x && vList[1].x > max.x && vList[1].y > min.y && vList[1].y < max.y) {
            enterDis = new Vec2(min.x - vList[1].x, 0.0f);
            Log.i(log, "A");
            return enterDis;
        }
        // 下
        if (vList[1].y > max.y && vList[2].y < min.y && vList[2].x < max.x && vList[2].x > min.x) {
            enterDis = new Vec2(0.0f, max.y - vList[2].y);
            Log.i(log, "B");
            return enterDis;
        }
        // 左
        if (vList[3].x < min.x && vList[0].x > max.x && vList[1].y > min.y && vList[1].y < max.y) {
            enterDis = new Vec2(max.x - vList[3].x, 0.0f);
            Log.i(log, "C");
            return enterDis;
        }
        // 上
        if (vList[0].y > max.y && vList[1].y < min.y && vList[0].x > min.x && vList[0].x < max.x) {
            enterDis = new Vec2(0.0f, min.y - vList[0].y);
            Log.i(log, "D");
            return enterDis;
        }


        // 上    1
        /*if (rect.min.y < max.y && rect.max.y > max.y && rect.min.x > min.x && rect.max.x < max.x) {
            enterDis = new Vec2(0.0f, max.y - rect.min.y);
            Log.i(log, "上");
            return enterDis;
        }
        // 上右   2
        if (rect.min.x < max.x && rect.max.x > max.x && rect.min.y < max.y && rect.max.y > max.y) {
            enterDis = new Vec2(max.x - rect.min.x, max.y - rect.min.y);
            Log.i(log, "上右");
            return enterDis;
        }
        // 右    3
        if (rect.min.x < max.x && rect.max.x > max.x && rect.min.y > min.y && rect.max.y < max.y) {
            enterDis = new Vec2(max.x - rect.min.x, 0.0f);
            Log.i(log, "右");
            return enterDis;
        }
        // 右下   4
        if (rect.min.x < max.x && rect.max.x > max.x && rect.max.y > min.y && rect.min.y < min.y) {
            enterDis = new Vec2(max.x - rect.min.x, min.y - rect.max.y);
            Log.i(log, "右下");
            return enterDis;
        }
        // 下    5
        if (rect.min.x > min.x && rect.max.x < max.x && rect.min.y < min.y && rect.max.y > min.y) {
            enterDis = new Vec2(0.0f, min.y - rect.max.y);
            Log.i(log, "下");
            return enterDis;
        }
        // 左下   6
        if (rect.min.x < min.x && rect.max.x > min.x && rect.min.y < min.y && rect.max.y > min.y) {
            enterDis = new Vec2(min.x - rect.max.x, min.y - rect.max.y);
            Log.i(log, "左下");
            return enterDis;
        }
        // 左    7
        if (rect.min.x < min.x && rect.max.x > min.x && rect.min.y > min.y && rect.max.y < max.y) {
            enterDis = new Vec2(min.x - rect.max.x, 0.0f);
            Log.i(log, "左");
            return enterDis;
        }
        // 左上   8
        if (rect.min.x < min.x && rect.max.x > min.x && rect.min.y < max.y && rect.max.y > max.y) {
            enterDis = new Vec2(min.x - rect.max.x, max.y - rect.min.y);
            Log.i(log, "左上");
            return enterDis;
        }*/

        return enterDis;

    }
}
