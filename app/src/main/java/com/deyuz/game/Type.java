package com.deyuz.game;

import android.util.Log;

/**
 * Created by deyuz on 2017/4/1.
 */

class Vec2 {
    float x, y;

    Vec2() {
        x = y = 0;
    }

    Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    Vec2(float v[]) {
        if (v.length > 1) {
            this.x = v[0];
            this.y = v[1];
        } else {
            System.err.println("----->Vec3::赋值出错，数组长度不能小于2！");
        }
    }

    Vec2 normal() {
        float m = magni();
        if (m == 0) {
            return new Vec2();
        }
        return new Vec2(x / m, y / m);
    }

    // 比较
    boolean largerThan(Vec2 vec2) {
        if (x > vec2.x && y > vec2.y) {
            return true;
        }
        return false;
    }

    void setValue(float value[]) {
        x = value[0];
        y = value[1];
    }

    Vec2 scale(float s) {
        return new Vec2(x * s, y * s);
    }

    Vec2 plus(Vec2 v) {
        return new Vec2(x + v.x, y + v.y);
    }

    Vec2 sub(Vec2 v) {
        return new Vec2(x - v.x, y - v.y);
    }

    // 截断
    void clamp(float board) {
        if (x > board) {
            x = board;
        }
        if (y > board) {
            y = board;
        }
        if (x < -board) {
            x = -board;
        }
        if (y < -board) {
            y = -board;
        }
    }

    float magni() {
        return (float) Math.sqrt(x * x + y * y);
    }
}

class Vec3 {
    float x, y, z;

    Vec3() {
        x = y = z = 0;
    }

    Vec3(float val[]) {
        if (val.length > 2) {
            x = val[0];
            y = val[1];
            z = val[2];
        } else {
            System.err.println("----->Vec3::赋值出错，数组长度不能小于3！");
        }
    }

    Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void setValue(float value[]) {
        x = value[0];
        y = value[1];
        z = value[2];
    }

    Vec3 scale(float s) {
        return new Vec3(x * s, y * s, z * s);
    }

    Vec3 plus(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }

    void clamp(float board) {
        if (x > board) {
            x = board;
        }
        if (y > board) {
            y = board;
        }
        if (z > board) {
            z = board;
        }

        if (x < -board) {
            x = -board;
        }
        if (y < -board) {
            y = -board;
        }
        if (z < -board) {
            z = -board;
        }
    }

    float magni() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}

