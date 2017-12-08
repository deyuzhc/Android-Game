package com.deyuz.game;

import android.app.Notification;
import android.content.Intent;
import android.os.Message;
import android.sax.RootElement;
import android.util.Log;
import android.widget.Toast;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/4.
 */
public class Ball {

    private String log = "----->Ball";

    Circle circle;
    AABB aabb;
    float ratio;


    Ball(float ratio) {
        circle = new Circle(ratio);
        this.ratio = ratio;
    }

    Ball(Vec2 o, float r, float ratio) {
        circle = new Circle(o, r, ratio);
        aabb = new AABB(new Vec2(o.x - r, (o.y - r) * ratio), new Vec2(o.x + r, (o.y + r) * ratio));
        aabb.setPos(o);

    }

    AABB getAABB() {
        return aabb;
    }

    void draw(GL10 gl, int slice) {
        circle.draw(gl, slice);
        //aabb.draw(gl, slice);
    }

    Vec2 getPos() {
        return circle.getPos();
    }

    void setPos(Vec2 pos) {
        // 更新圆位置
        circle.setPos(pos);
        // 更新包围盒值
        aabb.setPos(pos);
    }

    void roll(Vec2 speed, KdTree kdTree) {

        // 获取当前位置
        Vec2 opos = getPos();
        // 计算速度值——矢量
        Vec2 sped = speed.scale(0.01f);

        // 更新位置值
        Vec2 npos = opos.plus(sped);
        // 位置截断
        npos.clamp(0.975f);

        // 使用小球的包围盒与场景进行碰撞检测
        Rect rect = new Rect(aabb.min, aabb.max);
        if (kdTree.Intersect(rect)) {

            // 获取进入距离
            Vec2 enDis = kdTree.getEnterDis(rect);

            //enDis = enDis.scale(1.01f);

            // 更新位置
            npos = npos.plus(enDis);
        }
        setPos(npos);
    }


    boolean finish() {
        Vec2 pos = getPos();
        if (pos.x > 0.95f && pos.y < -0.95f) {
            //freeze = true;
            pos.x = -0.95f;
            pos.y = 0.95f;
            return true;
        }
        return false;
    }

}
