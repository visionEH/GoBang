package com.example.visioneh.gobang;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visioneh.gobang.socket.ConnectedThread;
import com.example.visioneh.gobang.socket.SocketManager;
import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private ChessPanel mpanel;
    private TextView undo;
    private TextView keep;
    private ImageView who;
    private int choose=-1;

    private ConnectedThread mConnectedThread;
    private BluetoothSocket mSocket;

    private Timer timer=new Timer();
    private TextView tv_time;
    private Date date=new Date(0);
    private long count=0;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        InitWidge();
        InitDialog();
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        choose=bundle.getInt("choose");
        switch (choose){
            case 1:
                mpanel.setComputer(true);
                undo.setVisibility(View.GONE);
                break;
            case 2:
                break;
            case 3://判断是否存档，是则还原棋盘
                if(GetIfKeep()){
                    GetChesses(mpanel.GetWhiteArrays(),mpanel.GetBlackArrays());
                }
                break;
            case 4:
                undo.setVisibility(View.GONE);
                String address=bundle.getString("address");
                boolean IsStart=bundle.getBoolean("isStart");
                if(address!=null){
                    mSocket = SocketManager.getmBleSocketHm(address);
                    manageClientSocket(IsStart);
                    mpanel.setBluetooth(true);
                    mpanel.setAdress(address);
                    mpanel.setIsStart(IsStart);
                    mpanel.setCallBack(this);
                }
        }
        timer.schedule(task,1000,1000);

    }
    private TimerTask task=new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    count+=1000;
                    date.setTime(count);
                    DateFormat format1 = new SimpleDateFormat("mm:ss");

                    tv_time.setText("时间已过   "+format1.format(date).toString());
                }
            });
        }
    };
    /**
     * 开启连接线程
     * @param isStart
     */
    public void manageClientSocket(boolean isStart) {
        mConnectedThread = new ConnectedThread(mSocket, mpanel, isStart);
        mConnectedThread.start();
    }

    /**
     * 发送消息
     * @param temp
     */
    public void onCommand(String temp) {
        mConnectedThread.write(temp.getBytes());
    }

    public void InitWidge(){
        mpanel= (ChessPanel) findViewById(R.id.chesspanel);
        undo=(TextView)findViewById(R.id.undo);
        keep=(TextView)findViewById(R.id.keep);
        who=(ImageView)findViewById(R.id.who);
        tv_time=(TextView)findViewById(R.id.timer);
        undo.setOnClickListener(this);
        keep.setOnClickListener(this);
        mpanel.setResultlistener(new ChessPanel.Resultlistener() {
            @Override
            public void ShowResult(String result) {
                StyledDialog.buildIosAlertVertical("结束", result + ",再来一次吗?", new MyDialogListener(){
                    @Override
                    public void onFirst() {
                        Bundle bundle=new Bundle();
                        bundle.putInt("choose",choose);
                        Intent intent=new Intent(MainActivity.this,MainActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onSecond() {

                    }

                    @Override
                    public void onThird() {
                        super.onThird();
                        finish();
                    }
                }).setBtnText("确定","取消","回到主界面")
                        .show();
            }

            @Override
            public void PutChess(boolean tag) {
                if(tag)
                    who.setImageResource(R.drawable.stone_white);
                else
                    who.setImageResource(R.drawable.stone_black);
            }
        });
    }
    public void InitDialog(){
         StyledDialog.init(getApplicationContext());
         MyActyManager.getInstance().setCurrentActivity(this);
    }
    /**
     * 设置是否已经存档
     * @param op
     */
    public void SetIfKeep(boolean op){
        SharedPreferences sp=getSharedPreferences("Judge",MODE_PRIVATE);
        SharedPreferences.Editor editor= sp.edit();
        editor.putBoolean("keep",op);
        editor.commit();
    }

    /**
     *
     * @return  返回是否存档
     */
    public boolean GetIfKeep(){
        boolean ans;
        SharedPreferences sp=getSharedPreferences("Judge",MODE_PRIVATE);
        ans=sp.getBoolean("keep",false);
        return ans;
    }

    /**
     * 将黑白子坐标存入数据库中，tag为0表示白字，tag为1表示黑子
     * @param white
     * @param black
     */
    public void KeepChesses(List<Point> white,List<Point> black){
        DBhelper dBhelper=new DBhelper(getApplicationContext(),"chess.db",null,1);
        SQLiteDatabase sqlite=dBhelper.getWritableDatabase();
        for(int i=0;i<white.size();i++){
            Point point=white.get(i);
            sqlite.execSQL("insert into Chess values(NULL,?,?,?)",new Object[]{point.x,point.y,0});
        }
        for(int i=0;i<black.size();i++){
            Point point=black.get(i);
            sqlite.execSQL("insert into Chess values(NULL,?,?,?)",new Object[]{point.x,point.y,1});
        }
        dBhelper.close();
    }

    /**
     * 从数据库中获取相应棋子坐标
     * @param white
     * @param black
     */
    public void GetChesses(List<Point> white,List<Point> black){
        DBhelper dBhelper=new DBhelper(getApplicationContext(),"chess.db",null,1);
        SQLiteDatabase sqlite=dBhelper.getWritableDatabase();
        Cursor cursor=sqlite.rawQuery("select * from Chess where tag=?",new String[]{"0"});
        while(cursor.moveToNext()){
            int x= cursor.getInt(cursor.getColumnIndex("left"));
            int y= cursor.getInt(cursor.getColumnIndex("right"));
            Point point=new Point(x,y);
            white.add(point);
        }
        cursor.close();
        Cursor cursor1=sqlite.rawQuery("select * from Chess where tag=?",new String[]{"1"});
        while(cursor1.moveToNext()){
            int x= cursor1.getInt(cursor.getColumnIndex("left"));
            int y= cursor1.getInt(cursor.getColumnIndex("right"));
            Point point=new Point(x,y);
            black.add(point);
        }
        cursor.close();

        dBhelper.close();
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.keep){
             SetIfKeep(true);
             KeepChesses(mpanel.GetWhiteArrays(),mpanel.GetBlackArrays());
        }
        else if(id==R.id.undo){
                mpanel.Undo();
        }
    }

}
