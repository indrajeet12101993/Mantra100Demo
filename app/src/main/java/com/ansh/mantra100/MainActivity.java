package com.ansh.mantra100;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MFSUtils.MFSListener {

    private ImageView iv_dp;
    private Button btn_mark_attendance;
    private TextView tv_dev, tv_date, tv_loc;
    private ArrayList<byte[]> Verify_Template;
    private Context context;
    private String deviceLocation = "";
    private boolean isLocationAvaliable = false;
    private boolean isDeviceActive = false;
    // private DBUtils con;
    private ProgressDialog authenticationDialog;
    private StringBuilder loc;
    private Handler autoDismissHandler;
    private Runnable autoDismissRunnable;

    private MFSUtils mfsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    public void initView() {
        initializationControls();
    }


    protected void initListener() {
        btn_mark_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDeviceActive && isLocationAvaliable) {
                    mfsUtils.startCapture();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        mfsUtils = new MFSUtils(MainActivity.this, this);
    }

    public void initializationControls() {
        autoDismissHandler = new Handler();
        iv_dp = findViewById(R.id.iv_dp);
        tv_date = findViewById(R.id.tv_date);
        btn_mark_attendance = findViewById(R.id.btn_mark_attendance);

        tv_dev = findViewById(R.id.tv_dev);
        tv_loc = findViewById(R.id.tv_loc);
        tv_loc.setText(Html.fromHtml("LocationInfo : <font color=#848484>Loading...</font>"));

        tv_dev.setText(Html.fromHtml("Device : <font color=#848484>Please connect the device!</font>"));
        context = getApplicationContext();
        iv_dp = findViewById(R.id.iv_dp);
        Verify_Template = new ArrayList<byte[]>();
        authenticationDialog = new ProgressDialog(this);
        authenticationDialog.setMessage("Authenticating...");
        authenticationDialog.setCancelable(false);
        tv_date.setText(Html.fromHtml("Date : <font color=#848484>" + "12/02/2019" + "</font>"));


        //add fingure pirnt tempate
        makeVerifyTemplate();
    }


    protected void onStop() {
        mfsUtils.removeScanner();
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        mfsUtils.stop();
        autoDismissHandler.removeCallbacks(autoDismissRunnable);
        super.onDestroy();
    }


    public void makeVerifyTemplate() {
        //todo add fingure print data in Verify_tempalte
        for (int j = 1; j <= 3; j++) {
         //   Verify_Template.add(Base64.decode(c.getString(j), Base64.DEFAULT));
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    @Override
    public void onCaptureResult(MFS100 mfs100, FingerData fingerData) {
        if ((mfs100.MatchISO(Verify_Template.get(0), fingerData.ISOTemplate())) > 3000) {
            Toast.makeText(context, "Capture Successfull", Toast.LENGTH_SHORT).show();
        } else if ((mfs100.MatchISO(Verify_Template.get(1), fingerData.ISOTemplate())) > 3000) {

            Toast.makeText(context, "Capture Successfull", Toast.LENGTH_SHORT).show();
        } else if ((mfs100.MatchISO(Verify_Template.get(2), fingerData.ISOTemplate())) > 3000) {
            Toast.makeText(context, "Capture Successfull", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unauthorized Access", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeviceStatusMsg(Spanned status) {
        tv_dev.setText(status);
    }

    @Override
    public void onDeviceConnected() {
        isDeviceActive = true;
        if (isLocationAvaliable) {
            btn_mark_attendance.setBackgroundColor(getResources().getColor(R.color.mark_att_enabled));
        }
    }

    @Override
    public void onDeviceDisconnected() {
        this.isDeviceActive = false;
        btn_mark_attendance.setBackgroundColor(getResources().getColor(R.color.mark_att_disabled));
    }
}
