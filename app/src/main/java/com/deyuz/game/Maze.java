package com.deyuz.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/4.
 */
public class Maze {

    private String log = "----->Maze::";
    private Path path;
    private ArrayList<Brick> bricks;
    private KdTree kdTree;

    Maze() {
        path = new Path();
        kdTree = new KdTree();
    }

    Maze(int width, int height) {
        path = new Path(width, height);
        kdTree = new KdTree();
    }

    Maze(int width, int height, int nLeaf) {
        path = new Path(width, height);
        kdTree = new KdTree(nLeaf);
    }

    // 生成路径，建立kd树
    void init() {
        bricks = path.generate();
        kdTree.build(bricks, kdTree.root);
    }

    // 绘制所有砖块
    void draw(GL10 gl, float ratio) {
        for (int i = 0; i < bricks.size(); i++) {
            bricks.get(i).draw(gl, ratio);
        }
    }

    KdTree getKdTree() {
        return kdTree;
    }

    /**
     * Created by deyuz on 2017/4/2.
     */
    public static class Path {

        private String log = "----->Path::";

        // width代表横向格数
        private int width;
        // height代表纵向格数
        private int height;
        // path记录二维格
        private int path[][];
        // 并查集
        private UFset ufset;

        Path() {
            width = 0;
            height = 0;

            path = new int[height][width];
            ufset = new UFset(height * width);
        }

        Path(int width, int height) {
            this.width = width;
            this.height = height;

            path = new int[height][width];
            ufset = new UFset(height * width);
        }

        void setWidth(int width) {
            this.width = width;

            path = new int[this.height][width];
            ufset = new UFset(height * width);
        }

        void setHeight(int height) {
            this.height = height;

            path = new int[height][this.width];
            ufset = new UFset(height * width);
        }

        // 用于生成迷宫的函数
        ArrayList<Brick> generate() {
            ArrayList<Brick> bricks = new ArrayList<>();
            // 迷宫格子
            int index = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    path[i][j] = index++;
                }
            }

            // 迷宫墙
            boolean wall[][][] = new boolean[height][width][2];

            while (ufset.find(path[0][0]) != ufset.find(path[height - 1][width - 1])) {
                //* 确定一条边的起点
                int hp = new Random().nextInt(height);
                int wp = new Random().nextInt(width);
                //* 确定一条边的方向
                boolean ori = new Random().nextBoolean();

                // 若水平方向
                if (ori) {
                    // 越界处理
                    if (wp + 1 == width) {
                        continue;
                    }
                    // 左
                    int m = path[hp][wp];
                    // 右
                    int n = path[hp][wp + 1];
                    // 并
                    if (ufset.union(m, n)) {

                        wall[hp][wp + 1][1] = true;

                        path[hp][wp] = ufset.find(m);
                        path[hp][wp + 1] = ufset.find(n);
                    }
                }
                // 若竖直方向
                else {
                    // 越界处理
                    if (hp + 1 == height) {
                        continue;
                    }
                    int m = path[hp][wp];
                    int n = path[hp + 1][wp];
                    // 并
                    if (ufset.union(m, n)) {

                        wall[hp + 1][wp][0] = true;

                        path[hp][wp] = ufset.find(m);
                        path[hp + 1][wp] = ufset.find(n);
                    }
                }
            }

            for (int w = 0; w < width; w++) {
                wall[0][w][0] = true;
            }

            for (int h = 0; h < height; h++) {
                wall[h][0][1] = true;
            }

            // 场景边界，使用四个砖块封闭
            // 4块
           /* // 左
            bricks.add(new Brick(new Rect(new Vec2(-1.2f, -1.2f), new Vec2(-1.0f, 1.2f))));
            // 右
            bricks.add(new Brick(new Rect(new Vec2(1.0f, -1.2f), new Vec2(1.2f, 1.2f))));
            // 上
            bricks.add(new Brick(new Rect(new Vec2(-1.2f, 1.0f), new Vec2(1.2f, 1.2f))));
            // 下
            bricks.add(new Brick(new Rect(new Vec2(-1.2f, -1.2f), new Vec2(1.2f, -1.0f))));*/

            //仅作测试用
            /*for (int h = 0; h < 6; h++) {
                for (int w = 0; w < 1; w++) {
                    // 纵向
                    bricks.add(new Brick(new Rect(new Vec2(w, -0.5f + 1.0f / 6 * h), new Vec2(w + 0.0125f, -0.5f + 1.0f / 6 * (h + 1)))));

                    // 横向
                    bricks.add(new Brick(new Rect(new Vec2(-0.5f + 1.0f / 6 * h, 0.0f), new Vec2(-0.5f + 1.0f / 6 * (h + 1), 0.0125f))));
                }
            }*/

            //bricks.add(new Brick(new Rect(new Vec2(-0.5f, -0.5f), new Vec2(0.5f, 0.5f))));


            // 场景内砖墙
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    // 画水平方向
                    if (!wall[h][w][0]) {
                        bricks.add(new Brick(new Rect(
                                new Vec2(-1.0f + 2.0f * w / width, 1.0f - 2.0f * h / height + 0.015f),
                                new Vec2(-1.0f + 2.0f * (w + 1) / width + 0.015f, 1.0f - 2.0f * h / height)
                        )));
                    }

                    // 画竖直方向
                    if (!wall[h][w][1]) {
                        bricks.add(new Brick(new Rect(
                                new Vec2(-1.0f + 2.0f * w / width, 1.0f - 2.0f * (h + 1) / height),
                                new Vec2(-1.0f + 2.0f * w / width + 0.015f, 1.0f - 2.0f * h / height)
                        )));
                    }
                }
            }


            Log.i(log, "生成完成！砖块数目：" + bricks.size());
            return bricks;
        }

    }
}
