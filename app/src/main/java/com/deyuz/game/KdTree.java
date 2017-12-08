package com.deyuz.game;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by deyuz on 2017/4/1.
 */
public class KdTree {

    String log = "---->KdTree";

    // 代表坐标轴
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    // 用于记录碰撞时进入深度值的内部变量
    private Vec2 enterDis;

    // 叶子的容量
    int LeafSize;
    // 树根
    Node root;
    // 当前节点
    Node ptr;

    // 仅作测试用
    ArrayList<Brick> nList;

    KdTree() {
        root = new Node(false);
        root.level = 0;
        LeafSize = 5;

        nList = new ArrayList<>();
    }

    KdTree(int leafSize) {
        root = new Node(false);
        root.level = 0;
        LeafSize = leafSize;

        nList = new ArrayList<>();
    }

    // 建树函数
    void build(ArrayList<Brick> list, Node node) {
        // 树的层数
        int level = node.level;
        // 无需建树
        if (list.size() < LeafSize || level > 16) {

            Log.i(log, "建立叶子，大小->" + list.size());

            // 叶子节点设置
            node.isLeaf = true;
            node.addPrimitive(list);

            return;
        }
        // 需要建树
        else {
            // 计算方差
            final float devX = getDev(list, X);
            final float devY = getDev(list, Y);

            // 分割轴，axis==X时，为竖向切割
            int axis = devX > devY ? X : Y;
            // 分割距离
            float split;

            // 排序器
            Comparator<Brick> comparator = new Comparator<Brick>() {
                @Override
                public int compare(Brick o1, Brick o2) {
                    float diff = devX > devY ? o1.aabb.max.x - o2.aabb.max.x : o1.aabb.max.y - o2.aabb.max.y;
                    return (int) (diff * 1e10);
                }
            };
            // 排序
            Collections.sort(list, comparator);
            // 数组中间位置
            int nsplit = (list.size() + 1) / 2;
            split = devX > devY ? list.get(nsplit).aabb.max.x : list.get(nsplit).aabb.max.y;

            // 左数组与右数组
            ArrayList<Brick> arrLeft = new ArrayList<>();
            ArrayList<Brick> arrRight = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                Brick brick = list.get(i);
                // 分割轴为X
                if (devX > devY) {
                    // 左子
                    if (brick.aabb.max.x <= split) {
                        arrLeft.add(brick);
                    }
                    // 右子
                    if (brick.aabb.max.x >= split) {
                        arrRight.add(brick);
                    }
                }
                // 分割轴为Y
                else {
                    // 左子   下
                    if (brick.aabb.min.y <= split) {
                        arrLeft.add(brick);
                    }
                    // 右子   上
                    if (brick.aabb.max.y >= split) {
                        arrRight.add(brick);
                    }
                }
            }

            // 中间节点设置
            node.isLeaf = false;
            node.axis = axis;
            node.split = split;

            Log.i(log, "分割节点级别:" + node.level + (axis == X ? "纵切" : "横切") + " 分割值为：" + split);

            // 左子与右子
            Node nleft = new Node(false);
            Node nright = new Node(false);

            nleft.level = level + 1;
            nright.level = level + 1;

            node.left = nleft;
            node.right = nright;

            // 左子递归
            Log.i(log, "左子分割：");
            build(arrLeft, nleft);
            // 右子递归
            Log.i(log, "右子分割：");
            build(arrRight, nright);
        }
    }

    // 求交函数-顶点
    boolean Intersect(Vec2 pos) {

        Node node = root;
        // 分割轴
        int axis;
        // 分割值
        float split;
        // 叶子父节点
        //Node parent;

        // 查找叶子
        while (!node.isLeaf) {
            axis = node.axis;
            split = node.split;
            //parent = node;

            if (axis == X) {
                node = pos.x > split ? node.right : node.left;
            } else if (axis == Y) {
                node = pos.y > split ? node.right : node.left;
            } else {
                Log.i(log, "分割轴取值错误，检查相应代码！（顶点求交）");
            }
        }

        // 相交标记
        boolean intersect;

        // 找到叶子
        for (int i = 0; i < node.list.size(); i++) {
            intersect = node.list.get(i).Intersect(pos);
            if (intersect) {

                //Log.i(log, "相交");
                return true;
            }
        }
        //Log.i(log, "不相交");
        return false;
    }

    // 求交函数-圆
    boolean Intersect(Vec2 pos, float r) {
        Node node = root;
        int axis;
        float split;

        // 用于存储分割轴
        Stack<Node> stack = new Stack<>();

        while (!node.isLeaf) {
            axis = node.axis;
            split = node.split;

            // 压入中间节点
            stack.push(node);

            if (axis == X) {
                node = pos.x > split ? node.right : node.left;
            } else if (axis == Y) {
                node = pos.y > split ? node.right : node.left;
            } else {
                Log.i(log, "分割轴取值错误，检查相应代码！（圆求交）");
            }
        }

        boolean intersect;

        // 查询当前叶子
        for (int i = 0; i < node.list.size(); i++) {
            intersect = node.list.get(i).Intersect(pos, r);
            if (intersect) {
                return true;
            }
        }
        Node tmpNode;
        for (int i = 0; i < stack.size(); i++) {
            tmpNode = stack.pop();
            // 判断分割轴
            if (tmpNode.axis == X) {
                // 判断左右子
                if (pos.x < tmpNode.split) {
                    // 判断相交
                    if (pos.x + r <= tmpNode.split) {
                        return false;
                    } else {
                        // 右侧判交
                        if (Intersect(tmpNode, pos, r)) {
                            return true;
                        }
                    }
                } else {
                    if (pos.x - r >= tmpNode.split) {
                        return false;
                    } else {
                        // 左侧判交
                        if (Intersect(tmpNode, pos, r)) {
                            return true;
                        }
                    }
                }
            } else if (tmpNode.axis == Y) {
                // 判断左右子
                if (pos.y < tmpNode.split) {
                    // 判断相交
                    if (pos.y + r <= tmpNode.split) {
                        return false;
                    } else {
                        // 上侧判交
                        if (Intersect(tmpNode, pos, r)) {
                            return true;
                        }
                    }
                } else {
                    if (pos.y - r >= tmpNode.split) {
                        return false;
                    } else {
                        // 下侧判交
                        if (Intersect(tmpNode, pos, r)) {
                            return true;
                        }
                    }
                }
            } else {
                Log.i(log, "分割轴错误，检查相应代码！");
            }
        }

        return false;
    }

    //求交函数-长方形的包围盒
    boolean Intersect(Rect rect) {

        Node node = root;
        int axis;
        float split;
        Vec2 pos = new Vec2((rect.min.x + rect.max.x) / 2, (rect.min.y + rect.max.y) / 2);

        // 用于存储分割轴
        Stack<Node> stack = new Stack<>();

        // 若是中间节点
        while (!node.isLeaf) {
            //Log.i(log, "求交判断，节点：" + node.split);
            axis = node.axis;
            split = node.split;

            // 压入非叶子节点
            stack.push(node);

            if (axis == X) {
                node = pos.x > split ? node.right : node.left;
            } else if (axis == Y) {
                node = pos.y > split ? node.right : node.left;
            } else {
                Log.i(log, "分割轴取值错误，检查相应代码！（圆求交）");
            }
        }

        boolean intersect;

        ptr = node;
        // 查询当前叶子
        for (int i = 0; i < node.list.size(); i++) {
            intersect = node.list.get(i).Intersect(rect);
            if (intersect) {
                //Log.i(log, "快速检测相交");
                return true;
            }
        }

        Node tmpNode;
        for (int i = 0; i < stack.size(); i++) {
            tmpNode = stack.pop();
            // 判断分割轴
            if (tmpNode.axis == X) {
                // 判断左右子
                if (pos.x < tmpNode.split) {
                    // 判断相交
                    if (rect.max.x <= tmpNode.split) {
                        return false;
                    } else {
                        // 右侧判交
                        if (Intersect(tmpNode.right, rect.min, rect.max)) {
                            return true;
                        }
                    }
                } else {
                    if (rect.min.x >= tmpNode.split) {
                        return false;
                    } else {
                        // 左侧判交
                        if (Intersect(tmpNode.left, rect.min, rect.max)) {
                            return true;
                        }
                    }
                }
            } else if (tmpNode.axis == Y) {
                // 判断左右子
                if (pos.y < tmpNode.split) {
                    // 判断相交
                    if (rect.max.y <= tmpNode.split) {
                        return false;
                    } else {
                        // 上侧判交
                        if (Intersect(tmpNode.right, rect.min, rect.max)) {
                            return true;
                        }
                    }
                } else {
                    if (rect.min.y >= tmpNode.split) {
                        return false;
                    } else {
                        // 下侧判交
                        if (Intersect(tmpNode.left, rect.min, rect.max)) {
                            return true;
                        }
                    }
                }
            } else {
                Log.i(log, "分割轴错误，检查相应代码！");
            }
        }

        return false;
    }

    // 求交函数-长方形和kd树中指定节点
    boolean Intersect(Node node, Vec2 min, Vec2 max) {
        if (node.isLeaf) {
            ptr = node;
            for (int i = 0; i < node.list.size(); i++) {
                if (node.list.get(i).Intersect(new Rect(min, max))) {
                    return true;
                }
            }
            return false;
        } else {
            Node left = node.left;
            Node right = node.right;

            boolean interleft = Intersect(left, min, max);
            boolean interright = Intersect(right, min, max);

            if (interleft || interright) {
                return true;
            } else {
                return false;
            }
        }
    }

    void drawTree(GL10 gl, Node node) {
        /*Log.i(log, "树大小为：" + nList.size());
        for (int i = 0; i < nList.size(); i++) {
            draw(gl, nList.get(i).rect.min, nList.get(i).rect.max, new Vec3(1.0f, 0.0f, 0.0f));
        }*/
        //Log.i(log, "树大小为：" + nList.size());
        if (node.isLeaf) {
            return;
        } else {
            // 画此边
            int axis = node.axis;
            float split = node.split;
            if (axis == X) {
                draw(gl, new Vec2(split - 0.00325f, -1.0f), new Vec2(split + 0.00325f, 1.0f), new Vec3(1, 0, 0));
            } else if (axis == Y) {
                draw(gl, new Vec2(-1.0f, split - 0.00325f), new Vec2(1.0f, split + 0.00325f), new Vec3(0, 0, 1));
            } else {
            }

            // 递归
            Node cLeft = node.left;
            drawTree(gl, cLeft);

            Node cRight = node.right;
            drawTree(gl, cRight);
        }

    }

    // 仅作测试用
    void draw(GL10 gl, Vec2 min, Vec2 max, Vec3 c) {
        FloatBuffer vBuffer;
        FloatBuffer cBuffer;
        ByteBuffer iBuffer;

        // 更新 min 和 max
        min.x = min.x < max.x ? min.x : max.x;
        max.x = min.x > max.x ? min.x : max.x;

        min.y = min.y < max.y ? min.y : max.y;
        max.y = min.y > max.y ? min.y : max.y;

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


    // 求交函数-圆和Kd树中指定节点
    boolean Intersect(Node node, Vec2 pos, float r) {
        if (node.isLeaf) {
            for (int i = 0; i < node.list.size(); i++) {
                if (node.list.get(i).Intersect(pos, r)) {
                    return true;
                }
            }
            return false;
        } else {
            Node left = node.left;
            Node right = node.right;

            boolean interleft = Intersect(left, pos, r);
            boolean interright = Intersect(right, pos, r);

            if (interleft || interright) {
                return true;
            } else {
                return false;
            }
        }
    }

    // 计算方差，axis取值为{x=0,y=1,z=2}
    float getDev(ArrayList<Brick> list, int axis) {
        float dev = 0.0f;
        switch (axis) {
            case X:
                float aveX = 0.0f;
                for (int i = 0; i < list.size(); i++) {
                    aveX += list.get(i).aabb.max.x;
                }
                aveX /= list.size();
                for (int i = 0; i < list.size(); i++) {
                    dev += Math.pow(list.get(i).aabb.max.x - aveX, 2);
                }
                break;
            case Y:
                float aveY = 0.0f;
                for (int i = 0; i < list.size(); i++) {
                    aveY += list.get(i).aabb.max.y;
                }
                aveY /= list.size();
                for (int i = 0; i < list.size(); i++) {
                    dev += Math.pow(list.get(i).aabb.max.y - aveY, 2);
                }
                break;
            case Z:
                break;
            default:
                Log.i(log, "Kd树分割轴出现错误值，检查相应代码！");
                break;
        }
        return dev;
    }

    // 获得当前点所在的叶子
    Node findLeaf(Vec2 pos) {

        Node node = root;
        // 分割轴
        int axis;
        // 分割值
        float split;
        // 叶子父节点
        //Node parent;

        // 查找叶子
        while (!node.isLeaf) {
            axis = node.axis;
            split = node.split;
            //parent = node;

            if (axis == X) {
                node = pos.x > split ? node.right : node.left;
            } else if (axis == Y) {
                node = pos.y > split ? node.right : node.left;
            } else {
                Log.i(log, "分割轴取值错误，检查相应代码！（顶点求交）");
            }
        }
        return node;
    }

    // 获得包围盒进入深度
    Vec2 getEnterDis(Rect rect) {
        enterDis = new Vec2();

        for (int i = 0; i < ptr.list.size(); i++) {
            Vec2 edis = ptr.list.get(i).getEnterDis(rect);
            enterDis = enterDis.plus(edis);
        }

        return enterDis;
    }

    // 树的节点
    class Node {

        // 节点类型
        boolean isLeaf;

        // 左子指针
        Node left;
        // 右子指针
        Node right;

        int level;

        //----------------------
        //*** 用于节点 ***

        // 分割轴
        int axis;
        // 分割值
        float split;
        //----------------------
        //*** 用于叶子 ***

        // 存储图元的动态数组
        ArrayList<Brick> list;

        //----------------------

        // 默认构造函数
        Node(boolean isLeaf) {
            list = new ArrayList<Brick>();
        }

        // 非叶子节点
        Node(int axis, float split) {
            this.axis = axis;
            this.split = split;

            isLeaf = false;
            list = new ArrayList<Brick>();
        }


        // 叶子节点
        Node(ArrayList<Brick> list) {
            this.list = list;

            isLeaf = true;
            list = new ArrayList<Brick>();
        }

        // 使用时慎防叶子超过额定容量
        // 向叶子中添加图元
        void addPrimitive(ArrayList<Brick> partitial) {
            for (int i = 0; i < partitial.size(); i++) {
                this.list.add(partitial.get(i));
            }

            if (this.list.size() > LeafSize) {
                Log.i(log, "叶子超过最大容量，叶子额定大小为 " + LeafSize + " 当前叶子大小为 " + this.list.size());
            }
        }

    }
}
