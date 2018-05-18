package com.example.visioneh.gobang;

import android.graphics.Point;
import android.hardware.camera2.params.Face;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by visionEH on 2017/8/15.
 */

public class ChessUtil  {

    private int count=0;
    /**
     * 测试棋盘上有五颗相同棋子
     * @param piecesArray  所有棋子的坐标
     * @return
     */
    public static boolean checkFiveInLine(List<Point> piecesArray) {
        checkIsFull(piecesArray.size());
        for(Point p:piecesArray) {
            int x=p.x;
            int y=p.y;
            if(checkHorizontal(x,y,piecesArray)) return true;
            else if(checkVertical(x,y,piecesArray)) return true;
            else if(checkLeftDiagonal(x,y,piecesArray)) return true;
            else if(checkRightDiagonal(x,y,piecesArray)) return true;
        }
        return false;
    }

    public  static boolean checkIsFull(int size) {
            if(size==14*14)
                return true;
            else
                return false;
    }

    private static boolean checkRightDiagonal(int x, int y, List<Point> piecesArray) {
        int count=1;
        for(int i=1;i<5;i++) {
            if(piecesArray.contains(new Point(x-i,y+i))) {
                count++;
            } else {
                break;
            }
        }
        for(int i=1;i<5;i++) {
            if(piecesArray.contains(new Point(x+i,y-i))) {
                count++;
            } else {
                break;
            }
        }
        if(count>=5)
            return true;
        return  false;
    }

    private static boolean checkLeftDiagonal(int x, int y, List<Point> piecesArray) {
        int count=1;

        for(int i=1;i<5;i++) {
            if(piecesArray.contains(new Point(x-i,y-i))) {
                count++;
            } else {
                break;
            }
        }
        for(int i=1;i<5;i++) {
            if(piecesArray.contains(new Point(x+i,y+i))) {
                count++;
            } else {
                break;
            }
        }
        if(count>=5)
            return true;
        return  false;
    }

    private static boolean checkVertical(int x, int y, List<Point> piecesArray) {
        int count=1;
        for(int i=1;i<5;i++) {
            if(piecesArray.contains(new Point(x,y-i))) {
                count++;
            } else {
                break;
            }
        }
        for(int i=1;i<5;i++) {
            if(piecesArray.contains(new Point(x,y+i))) {
                count++;
            } else {
                break;
            }
        }
        if(count>=5)
            return true;
        return  false;
    }

    private static boolean checkHorizontal(int x, int y, List<Point> piecesArray) {
        int count=1;
        //向左
        for(int i=1;i<5;i++){
            if(piecesArray.contains(new Point(x-i,y)))
                count++;
            else
                break;
        }
        //向右
        for(int i=1;i<5;i++){
            if(piecesArray.contains(new Point(x+i,y)))
                count++;
            else
                break;
        }
        if(count>=5)
            return true;
        return  false;
    }

    /**
     * 初始化赢法数组
     * @param winArrays 三维数组，前两维表示棋子在棋盘中的坐标，第三维表示赢法编号，true表示落子，false表示不落子
     */
    public static int InitWinArray(boolean[][][] winArrays){
        int count=0;
        for(int i=0;i<ChessPanel.MAX_LINE;i++){
            for(int j=0;j<ChessPanel.MAX_LINE-4;j++){
                for(int k=0;k<5;k++){
                    winArrays[i][j+k][count]=true;
                }
                count++;
            }
        }
        for(int i=0;i<ChessPanel.MAX_LINE-4;i++){
            for(int j=0;j<ChessPanel.MAX_LINE;j++){
                for(int k=0;k<5;k++){
                    winArrays[i+k][j][count]=true;
                }
                count++;
            }
        }
        for(int i=0;i<ChessPanel.MAX_LINE-4;i++){
            for(int j=0;j<ChessPanel.MAX_LINE-4;j++){
                for(int k=0;k<5;k++){
                    winArrays[i+k][j+k][count]=true;
                }
                count++;
            }
        }
        for(int i=0;i<ChessPanel.MAX_LINE-4;i++){
            for(int j=ChessPanel.MAX_LINE-1;j>3;j--){
                for(int k=0;k<5;k++){
                    winArrays[i+k][j-k][count]=true;
                }
                count++;
            }
        }
        return count;
    }

    /**
     * 计算我方赢法统计数组
     * @param winArrays 赢法数组
     * @param mywin     我方赢法
     * @param computerWin  计算机赢法
     * @param count     赢法种类
     * @return  1表示我方胜利
     */
    public static int CountMyWin(boolean[][][] winArrays,int[] mywin,int[] computerWin,int count,Point p){
        int max=0;
        for(int k=0;k<count;k++){
            if(winArrays[p.x][p.y][k]){
                mywin[k]++;
                computerWin[k]=6;
                if(mywin[k]>max&&mywin[k]<5){
                    max=mywin[k];
                }
                if(mywin[k]==5){
                 //   return 1;
                }
            }
        }
        Log.d("finger","mymax"+max);
        return 0;
    }
    /**
     * 计算计算机赢法统计数组
     * @param winArrays 赢法数组
     * @param mywin     我方赢法
     * @param computerWin  计算机赢法
     * @param count     赢法种类
     * @param p
     * @return  1表示计算机胜利
     */
    public static int CountComputerWin(boolean[][][] winArrays,int[] mywin,int[] computerWin,int count,Point p){
        for(int k=0;k<count;k++){
            if(winArrays[p.x][p.y][k]){
                computerWin[k]++;
                mywin[k]=6;
                if(computerWin[k]==5){
                  //  return 1;
                }
            }
        }
        return 0;
    }

}
