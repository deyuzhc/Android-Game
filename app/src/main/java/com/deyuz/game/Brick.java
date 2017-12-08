package com.deyuz.game;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/1.
 */
// 图元类，每个图元包含一个障碍物的所有信息，包括四边形及其对应的包围盒
public class Brick {
    // 四边形
    Rect rect;
    // 包围盒
    AABB aabb;
    // 仅作测试用
    boolean highlight;

    Brick() {
        rect = new Rect();
        aabb = new AABB();
    }

    Brick(Rect rect) {
        this.rect = rect;
        this.aabb = new AABB(rect.min, rect.max);
    }

    Brick(AABB aabb) {
        this.aabb = aabb;
    }

    Brick(Rect rect, AABB aabb) {
        this.rect = rect;
        this.aabb = aabb;
    }

    // 求交函数
    boolean Intersect(Circle circle) {
        return aabb.Intersect(circle);
    }

    // 顶点
    boolean Intersect(Vec2 pos) {
        return aabb.Intersect(pos);
    }

    boolean Intersect(Vec2 pos, float r) {
        return aabb.Intersect(pos, r);
    }

    boolean Intersect(Rect rect) {
        highlight = this.aabb.Intersect(rect);
        return this.aabb.Intersect(rect);
    }

    Vec2 getEnterDis(Rect rect) {
        return aabb.getEnterDis(rect);
    }

    void draw(GL10 gl, float ratio) {
        Vec3 col = new Vec3(0.125f, 0.5f, 0.125f);
        if (highlight) {
            col = new Vec3(0.0f, 0.0f, 1.0f);
        }

        rect.draw(gl, ratio, col);
    }
}
