package com.deyuz.game;

/**
 * Created by deyuz on 2017/5/2.
 */

public class UFset {

    // 存储所有节点
    private int list[];
    // 存储所有节点对应的秩
    private int rank[];

    // length代表此集中节点数目
    UFset(int length) {
        list = new int[length];
        rank = new int[length];

        // list需要初始化，而rank不需要
        for (int i = 0; i < length; i++) {
            list[i] = i;
        }
    }

    // 未执行合并操作返回False，执行合并操作返回True
    public boolean union(int m, int n) {
        int x = find(m);
        int y = find(n);
        // 同类不合并
        if (find(m) == find(n)) {
            return false;
        }
        if (rank[x] > rank[y]) {
            list[y] = x;
        } else {
            list[x] = y;
            if (rank[x] == rank[y]) {
                rank[y]++;
            }
        }
        return true;

    }

    public int find(int x) {

        if (x == list[x]) {
            return x;
        }
        // 路径压缩
        list[x] = find(list[x]);
        return list[x];
    }

    public void output() {
        for (int i = 0; i < list.length; i++) {
            System.out.print(list[i] + "\t");
        }
        System.out.println();

        for (int i = 0; i < list.length; i++) {
            System.out.print(i + "\t");
        }
        System.out.println();
    }
}
