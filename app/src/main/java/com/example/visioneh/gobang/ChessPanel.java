package com.example.visioneh.gobang;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by visionEH on 2017/8/15.
 * 自定义五子棋盘view
 */

public class ChessPanel extends View{
    private int mPanelWidth;
    private float mLineHeight;
    public static int MAX_LINE=15;
    //绘制网格
    private Paint mPaint;
    private int PaintColor=R.color.paintColor;

    private Bitmap whiteChess;
    private Bitmap blackChess;
    private static final float RATIO_PIECE=3*1.0f/4;//设置棋子的大小为棋盘格子的3/4
    private boolean IsGameOver=false;

    private boolean IsWhite=true;//判断当前该谁落子
    private List<Point> whiteList=new ArrayList<>();
    private List<Point> blackList=new ArrayList<>();

    private boolean IsComputer=false;//判断是否是人机棋局
    private boolean[][][] winArrays=new boolean[16][16][600];
    private int count=0;
    private int[] mywin=new int[600];
    private int[] computerwin=new int[600];


    private MainActivity mBleGameActivity;
    private String mAdress;
    private boolean  isMe=false;//是否轮到自己下棋
    public void setmAdress(String mAdress) {
        this.mAdress = mAdress;
    }

    public  interface Resultlistener{
        void ShowResult(String result);
        void PutChess(boolean tag);
    };
    private Resultlistener resultlistener;
    public void setResultlistener(Resultlistener resultlistener) {
        this.resultlistener = resultlistener;
    }




    public boolean isBluetooth() {
        return IsBluetooth;
    }

    private boolean IsBluetooth;
    public void setComputer(boolean computer) {
        IsComputer = computer;
        InitComputer();
    }
    public void setBluetooth(boolean bluetooth){
        IsBluetooth=bluetooth;
    }
    public ChessPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        InitPaint();
    }
    private void InitPaint() {
        mPaint=new Paint();
        mPaint.setColor(getResources().getColor(PaintColor));
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setDither(true);//防抖动
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);//线条宽度
        whiteChess= BitmapFactory.decodeResource(getResources(),R.drawable.stone_white);
        blackChess=BitmapFactory.decodeResource(getResources(),R.drawable.stone_black);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        //想把网格棋盘绘制成正方形
        //如果传入的是一个精确的值，就直接取值
        //同时也考虑到获得的widthSize与heightSize是设置的同样的值(如固定的100dp)，但也有可能是match_parent，所以在这里取最小值
        int length=Math.min(widthSize,heightSize);

        if(widthMode==MeasureSpec.UNSPECIFIED) {
            length=heightSize;
        } else if(heightMode==MeasureSpec.UNSPECIFIED) {
            length=widthSize;
        }
        //将宽和高设置为同样的值
        //在重写onMeasure方法时，必需要调用该方法存储测量好的宽高值
        setMeasuredDimension(length,length);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth=w;
        mLineHeight=mPanelWidth*1.0f/MAX_LINE;
        int chessWidth= (int) ( mLineHeight*RATIO_PIECE);
        //让棋子占据方格的3/4
        whiteChess=Bitmap.createScaledBitmap(whiteChess,chessWidth,chessWidth,false);
        blackChess=Bitmap.createScaledBitmap(blackChess,chessWidth,chessWidth,false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorder(canvas);
        drawChess(canvas);
        if(!isBluetooth()){
            CheckGameOver();
        }
    }
    public boolean CheckGameOver(){
        if(IsGameOver) return true;
        String result="";
        boolean isWhiteWon = ChessUtil.checkFiveInLine(whiteList);
        boolean isBlackWon = ChessUtil.checkFiveInLine(blackList);
        if(isWhiteWon||isBlackWon) {
            IsGameOver=true;
            result=isWhiteWon?"白棋胜利":"黑棋胜利";
            resultlistener.ShowResult(result);
            return  true;
        }
        boolean isFull=ChessUtil.checkIsFull(whiteList.size()+blackList.size());
        if(isFull) {
            result="平局";
            resultlistener.ShowResult(result);
            return true;
        }
        return false;
    }
    private void drawChess(Canvas canvas) {
        for(Point whitePoint:whiteList) {
            canvas.drawBitmap(whiteChess,(whitePoint.x+(1-RATIO_PIECE)/2)*mLineHeight,(whitePoint.y+(1-RATIO_PIECE)/2)*mLineHeight,null);
        }
        for(Point blackPoint:blackList) {
            canvas.drawBitmap(blackChess,(blackPoint.x+(1-RATIO_PIECE)/2)*mLineHeight,(blackPoint.y+(1-RATIO_PIECE)/2)*mLineHeight,null);
        }
    }
    private void drawBorder(Canvas canvas) {
        for (int i=0;i<MAX_LINE;i++) {
            int startX=(int)mLineHeight/2;
            int endX=(int)(mPanelWidth-mLineHeight/2);
            int y=(int)((0.5+i)*mLineHeight);
            //首先画横线
            canvas.drawLine(startX,y,endX,y,mPaint);
            //然后再画纵线(与横线的坐标是相反的)
            canvas.drawLine(y,startX,y,endX,mPaint);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(IsGameOver)  return false;
        int action=event.getAction();
        //判断手指落下
        if(action==MotionEvent.ACTION_UP){
            int x= (int) event.getX();
            int y=(int)event.getY();
            Point point=getValidPoint(x,y);
            if(whiteList.contains(point)||blackList.contains(point)){
                //表示棋子已经存在
                return false;
            }
            if(IsComputer){//人机
                whiteList.add(point);
                ChessUtil.CountMyWin(winArrays,mywin,computerwin,count,point);
                ComputerPoint();
                invalidate();//重新绘制
                resultlistener.PutChess(true);
                return true;
            }
            if(IsBluetooth){//蓝牙对战
                  if(!isMe)   return false;
                  if(IsWhite){
                      whiteList.add(point);
                      putChess(point.x,point.y,1);//1表示白子,2表示黑子
                  }else{
                      blackList.add(point);
                      putChess(point.x,point.y,2);
                  }
                  if(CheckGameOver())
                      mBleGameActivity.onCommand(IsWhite==true?"white":"black");
                  invalidate();//重新绘制
                  return true;
            }//普通人人对战
            if(IsWhite){
                whiteList.add(point);
            }
            else{
                blackList.add(point);
            }
            IsWhite=!IsWhite;
            resultlistener.PutChess(IsWhite);
            invalidate();//重新绘制
            return true;
        }
        return true;
    }
    private Point getValidPoint(int x, int y) {
        int _x=(int)(x/mLineHeight);
        int _y=(int)(y/mLineHeight);
        return new Point(_x,_y);
    }

    private void InitComputer(){
        if(IsComputer){
           count=ChessUtil.InitWinArray(winArrays);
        }
    }
    /**
     *悔棋
     */
    public void Undo(){
         if(IsWhite){
             if(blackList.size()>0){
                 blackList.remove(blackList.size()-1);

                 IsWhite=!IsWhite;
                 resultlistener.PutChess(IsWhite);
                 invalidate();
             }
         }
         else{
             if(whiteList.size()>0){
                 whiteList.remove(whiteList.size()-1);
                 IsWhite=!IsWhite;
                 resultlistener.PutChess(IsWhite);
                 invalidate();
             }
         }
    }
    public void ComputerPoint(){
         int[][] myScore=new int[ChessPanel.MAX_LINE][ChessPanel.MAX_LINE];
         int[][] computerScore=new int[ChessPanel.MAX_LINE][ChessPanel.MAX_LINE];
         int maxScore=0;//用于记录目前棋盘上所有空闲点的最大分数
         int u=0;
         int v=0;//用于记录最大分数处点的坐标。
         int Lines=ChessPanel.MAX_LINE;
         for(int i=0;i<Lines;i++){
            for(int j=0;j<Lines;j++){
                 Point point=new Point(i,j);
                 if(!whiteList.contains(point)&&!blackList.contains(point)){
                     for(int k=0;k<count;k++){
                          if(winArrays[i][j][k]){
                               if(mywin[k]==1){
                                   myScore[i][j]+=200;
                               }else if(mywin[k]==2){
                                   myScore[i][j]+=400;
                               }else if(mywin[k]==3){
                                   myScore[i][j]+=2000;
                               }else if(mywin[k]==4){
                                   myScore[i][j]+=10000;
                               }
                              if(computerwin[k]==1){
                                  computerScore[i][j]+=200;
                              }else if(computerwin[k]==2){
                                  computerScore[i][j]+=400;
                              }else if(computerwin[k]==3){
                                  computerScore[i][j]+=2000;
                              }else if(computerwin[k]==4){
                                  computerScore[i][j]+=10000;
                              }
                          }
                     }
                     if(myScore[i][j]>maxScore){
                         maxScore=myScore[i][j];
                         u=i;v=j;
                     }else if(myScore[i][j]==maxScore){
                         if(computerScore[i][j]>computerScore[u][v]){
                             u=i;v=j;
                         }
                     }
                     if(computerScore[i][j]>maxScore){
                         maxScore=myScore[i][j];
                         u=i;v=j;
                     }else if(computerScore[i][j]==maxScore){
                         if(myScore[i][j]>myScore[u][v]){
                             u=i;v=j;
                         }
                     }
                 }
            }
        }
        blackList.add(new Point(u,v));
        ChessUtil.CountComputerWin(winArrays,mywin,computerwin,count,new Point(u,v));
    }
    public List<Point> GetWhiteArrays(){
        return whiteList;
    }
    public List<Point> GetBlackArrays(){
        return blackList;
    }

    //下棋子,并发送给对方
    private void putChess(int x, int y, int chessFlag) {
        isMe=false;
        String command = "";
        String temp = x + ";" + y + ";" + chessFlag;
        command += mAdress + ";" + temp;
        mBleGameActivity.onCommand(command + ";" + chessFlag);
    }
    //接收对方传送信息
    public void getCommand(String command) {
        Log.d("msg",command);
        if("msg;".equals(command.substring(0, 4))){
            String finalCommand = command.substring(4);
            Toast.makeText(mBleGameActivity, finalCommand, Toast.LENGTH_SHORT).show();
        }else if ("white".equals(command)) {
            resultlistener.ShowResult("白棋胜利!");
        }else if ("black".equals(command)) {
            resultlistener.ShowResult("黑棋胜利!");
        } else if ("restart".equals(command)) {
            Toast.makeText(mBleGameActivity, "对方重玩游戏!", Toast.LENGTH_SHORT).show();
           // restartGame();
        } else {
            String[] data = command.split(";");
            int x = Integer.parseInt(data[1]);
            int y = Integer.parseInt(data[2]);
            int flag = Integer.parseInt(data[3]);
            if(flag==1){
                whiteList.add(new Point(x,y));
            }else{
                blackList.add(new Point(x,y));
            }
            invalidate();
            isMe = true;
        }
    }

    public void setCallBack(MainActivity bleGameActivity) {
        this.mBleGameActivity = bleGameActivity;
    }
    public void setIsStart(boolean isStart) {
        IsWhite=isStart;
        isMe = isStart;
        if(!isMe)
            resultlistener.PutChess(false);
    }

    public void setAdress(String adress) {
        mAdress = adress;
    }
    public boolean isMe() {
        return isMe;
    }

    public interface onBluetoothListener {
        void onCommand(String temp);
    }



}
