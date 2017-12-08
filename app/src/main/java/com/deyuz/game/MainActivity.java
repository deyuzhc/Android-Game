package com.deyuz.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;
import java.util.zip.Inflater;

public class MainActivity extends Activity {

    private String log = "----->MainActivity";
    private SurfaceView surfaceView;
    private SensorManager mSensorManager;
    private com.deyuz.game.Sensor sensor;
    // 总布局
    private RelativeLayout container;
    private Thread fThread;
    // 用于计时的线程
    private TimeThread timeThread;
    private Handler handler;
    // 用于显示时间的控件
    private TextView timeView;
    private Maze maze;
    private Ball ball;
    // 单局游戏最大时间
    private int totalTime;
    // 迷宫宽度
    private int width;
    // 迷宫高度
    private int height;
    // 连胜次数
    private int nWin;
    // 一局游戏剩余时间
    private static int timeLeft;
    // 当前关卡序号
    private int nRound;
    // 胜利次数
    private int numWin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActivity();

        //surfaceView = createSurfaceView();
        container.addView(createSurfaceView());
        timeView = addTimeView(container, totalTime + "");

        // 监听是否完成
        new Monitor().execute();

        timeThread = new TimeThread();
        timeThread.start();

        // Handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                Button quitbt, goonbt;

                switch (msg.what) {
                    // 显示剩余时间
                    case -1:
                        timeView.setText(msg.obj.toString());
                        timeLeft = totalTime - (int) msg.obj;
                        Log.i(log, msg.obj.toString() + "");
                        break;
                    // 失败
                    case 0:
                        nWin = 0;
                        nRound++;
                        // 终止计时线程
                        timeThread.fin();
                        // 清空容器
                        container.removeAllViews();

                        View lose = inflater.inflate(R.layout.lose, null);
                        container.removeAllViews();
                        container.addView(lose, params);

                        goonbt = (Button) lose.findViewById(R.id.losegoon);
                        goonbt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                container.removeAllViews();

                                surfaceView = createSurfaceView();
                                container.addView(surfaceView);

                                timeView = addTimeView(container, "30");

                                // 开启计时线程
                                timeThread = new TimeThread();
                                timeThread.start();
                            }
                        });

                        quitbt = (Button) lose.findViewById(R.id.losequit);
                        quitbt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });

                        break;
                    // 成功
                    case 1:
                        nWin++;
                        numWin++;
                        nRound++;
                        // 终止计时线程
                        timeThread.fin();
                        // 清空容器
                        container.removeAllViews();

                        Log.i(log, "成功");
                        final View win = inflater.inflate(R.layout.win, null);
                        container.removeAllViews();
                        container.addView(win, params);
                        //setContentView(R.layout.win);

                        goonbt = (Button) win.findViewById(R.id.wingoon);
                        goonbt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                container.removeAllViews();

                                surfaceView = createSurfaceView();
                                container.addView(surfaceView);

                                timeView = addTimeView(container, "30");

                                // 开启计时线程
                                timeThread = new TimeThread();
                                timeThread.start();
                            }
                        });

                        TextView result = (TextView) win.findViewById(R.id.grade);
                        result.setText("当前第" + (nRound - 1) + "局，胜率"
                                + (float) numWin / (nRound - 1) + "，用时：" + timeLeft + "秒");

                        quitbt = (Button) win.findViewById(R.id.winquit);
                        quitbt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });

                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        sensor.regist();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensor.unregist();
    }

    private void initActivity() {
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 传感器管理器
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // 传感器
        sensor = new com.deyuz.game.Sensor(mSensorManager);

        // 主页面
        setContentView(R.layout.container);

        timeLeft = 0;
        nRound = 1;
        numWin = 0;

        totalTime = 20;

        width = 5;
        height = 5;

        container = (RelativeLayout) findViewById(R.id.activity_main);
    }

    SurfaceView createSurfaceView() {
        // 迷宫
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        /*int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        Log.i(log, width + "\t" + height);*/
        //--------------
        // 宽度增加
        if (nWin != 0 && width < 16 && nWin % 2 == 0) {
            width++;
        }
        // 高度增加
        if (nWin != 0 && height < 35 && nWin % 2 == 1) {
            height++;
        }
        maze = new Maze(width, height, 10);
        //--------------
        maze.init();
        //--------------
        /*float ox = new Random().nextFloat() - 0.5f;
        float oy = new Random().nextFloat() - 0.5f;*/
        // 小球
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        ball = new Ball(new Vec2(-0.95f, 0.95f), 0.04f, (float) width / height);
        // 画布
        SurfaceView mSurfaceView = new SurfaceView(this, maze, ball, sensor, maze.getKdTree());

        return mSurfaceView;
    }

    View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.norm:
                    Monitor mNorm = new Monitor();

                    mNorm.execute();
                    break;
                case R.id.fog:
                    fThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    fThread.start();
                    break;
                case R.id.exit:
                    break;
                default:
                    break;
            }
        }
    };

    class TimeThread extends Thread {
        private boolean fin;

        @Override
        public void run() {

            int i = totalTime;
            while (i > 0) {
                if (fin) {
                    return;
                }
                try {
                    Thread.currentThread().sleep(1000);
                    i--;
                    // 发送当前剩余时间
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = i;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 发送超时消息
            Message msg = new Message();
            msg.what = 0;
            handler.sendMessage(msg);
        }

        public void fin() {
            fin = true;
        }
    }

    class Monitor extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (ball.finish()) {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    TextView addTimeView(RelativeLayout layout, String string) {
        TextView timeView = new TextView(MainActivity.this);
        timeView.setText(string);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        layout.addView(timeView, params);

        return timeView;
    }

    // 横向
    void setOri() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    // 竖向
    void setVer() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void kdtest() {
        ArrayList<Brick> list = new ArrayList<>();
        Brick brick;
        for (int i = 0; i < 234; i++) {
            Vec2 min = new Vec2(new Random().nextFloat(), new Random().nextFloat());
            Vec2 max = new Vec2(new Random().nextFloat(), new Random().nextFloat());

            AABB aabb = new AABB(min, max);

            brick = new Brick(aabb);

            list.add(brick);
        }

        System.err.println("-------------->" + list.size());
        KdTree kdTree = new KdTree(10);
        kdTree.build(list, kdTree.root);

        if (kdTree.root.isLeaf) {
            System.err.println("之前，叶子！");
        } else {
            System.err.println("之前，非叶子！");
        }

        Vec2 pos = new Vec2(new Random().nextFloat(), new Random().nextFloat());
        kdTree.Intersect(pos, 1.0f);

        if (kdTree.root.isLeaf) {
            System.err.println("之后，叶子！");
        } else {
            System.err.println("之后，非叶子！");
        }
    }
}
