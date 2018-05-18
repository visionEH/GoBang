package com.example.visioneh.gobang;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.config.ConfigBean;

import java.io.IOException;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView computer;
    private TextView two_people;
    private TextView again;
    private TextView bluetooth;
    private ImageView x_code;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getSupportActionBar().hide();

        computer= (TextView) findViewById(R.id.btn_computer);
        two_people=(TextView)findViewById(R.id.btn_two_people);
        again=(TextView)findViewById(R.id.btn_again);
        bluetooth=(TextView)findViewById(R.id.btn_blue);
        x_code=(ImageView)findViewById(R.id.id_code);
        computer.setOnClickListener(this);
        two_people.setOnClickListener(this);
        again.setOnClickListener(this);
        bluetooth.setOnClickListener(this);
        x_code.setOnClickListener(this);

        StyledDialog.init(getApplicationContext());
        MyActyManager.getInstance().setCurrentActivity(this);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.btn_computer){//人机对战
            ShowMainActivity(1);
        }else if(id==R.id.btn_two_people){//人人对战
            ShowMainActivity(2);
        }else if(id==R.id.btn_again){//继续上次存档残局
            ShowMainActivity(3);
        }else if(id==R.id.btn_blue){//蓝牙对战
            startActivity(new Intent(StartActivity.this,BluetoothActivity.class));
            PlayerRelease();
        }else if(id==R.id.id_code){
            ViewGroup viewGroup=(ViewGroup)View.inflate(getApplicationContext(),R.layout.ic_code,null);
            final ConfigBean bean = StyledDialog.buildCustom(viewGroup, Gravity.CENTER);
            final Dialog dialog1 =   bean.show();
        }
    }
    @Override
    protected void onResume() {
            super.onResume();
            player=MediaPlayer.create(getApplicationContext(),R.raw.main_music);
            player.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void ShowMainActivity(int choose){
        Bundle bundle=new Bundle();
        bundle.putInt("choose",choose);
        Intent intent=new Intent(StartActivity.this,MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        PlayerRelease();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayerRelease();
    }

    public void PlayerRelease(){
        player.stop();
        player.release();
    }
}
