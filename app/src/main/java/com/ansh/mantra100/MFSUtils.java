package com.ansh.mantra100;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

public class MFSUtils implements MFS100Event {
    private MFSListener mfsListener;
    private MFS100 mfs100 = null;
    private FingerData lastCapFingerData = null;
    private Activity mActivity;
    private Dialog showFingerDialog;
    private de.hdodenhof.circleimageview.CircleImageView showFinger;
    private TextView quality;
    private int timeout = 10000;

    public MFSUtils(Activity mActivity, MFSListener mfsListener) {
        this.mActivity = mActivity;
        this.mfsListener = mfsListener;
        if (mfs100 == null) {
            mfs100 = new MFS100(this);
            mfs100.SetApplicationContext(mActivity);
            initView();
        } else {
            InitScanner();
        }
    }

    private void initView() {
        showFingerDialog = new Dialog(mActivity, R.style.TransparentDialog);
        showFingerDialog.setCancelable(false);
        showFingerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        showFingerDialog.setContentView(R.layout.dialog_show_finger);
        showFinger = showFingerDialog.findViewById(R.id.show_finger);
        quality = showFingerDialog.findViewById(R.id.textview_show_finger);
    }

    private void InitScanner() {
        try {
            int ret = mfs100.Init();
            if (ret != 0) {
            } else {
                String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo()
                        + " Make: " + mfs100.GetDeviceInfo().Make()
                        + " Model: " + mfs100.GetDeviceInfo().Model()
                        + "\nCertificate: " + mfs100.GetCertification();
            }
        } catch (Exception ex) {
            Toast.makeText(mActivity, "Init failed, unhandled exception", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        int ret = 0;
        if (!hasPermission) {
            mfsListener.onDeviceStatusMsg(Html.fromHtml("Device :  <font color=#848484>Permission denied</font>"));
            return;
        }
        if (vid == 1204 || vid == 11279) {
            if (pid == 34323) {
                ret = mfs100.LoadFirmware();
                if (ret != 0) {
                    mfsListener.onDeviceStatusMsg(Html.fromHtml("Device : <font color=#848484>" + mfs100.GetErrorMsg(ret) + "</font>"));
                } else {
                    mfsListener.onDeviceStatusMsg(Html.fromHtml("Device : <font color=#848484>Permission denied</font>"));
                }
            } else if (pid == 4101) {
                ret = mfs100.Init();
                if (ret != 0) {
                    mfsListener.onDeviceStatusMsg(Html.fromHtml("Device : <font color=#848484>" + mfs100.GetErrorMsg(ret) + "</font>"));
                } else {
                    mfsListener.onDeviceConnected();
                    mfsListener.onDeviceStatusMsg(Html.fromHtml("Device : <font color=#848484>Device Connected.</font>"));
                }
            }
        }
    }

    @Override
    public void OnDeviceDetached() {
        if (showFingerDialog.isShowing())
            showFingerDialog.dismiss();
        removeScanner();
        mfsListener.onDeviceDisconnected();
        mfsListener.onDeviceStatusMsg(Html.fromHtml("Device : <font color=#848484>Please connect the device!</font>"));
    }

    @Override
    public void OnHostCheckFailed(String s) {
        try {
            Toast.makeText(mActivity, s, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {

        }
    }


    public void stop() {
        if (mfs100 != null) {
            mfs100.Dispose();
        }
    }

    public void removeScanner() {
        if (showFingerDialog.isShowing())
            showFingerDialog.dismiss();
        try {
            int ret = mfs100.UnInit();
            if (ret != 0) {
            } else {
                lastCapFingerData = null;
            }
        } catch (Exception e) {
            Log.e("removeScanner.EX", e.toString());
        }
    }


    public void startCapture() {
        showFinger.setImageResource(R.drawable.icon_fingerprint);
        quality.setText("Quality : 0");
        showFinger.refreshDrawableState();
        quality.refreshDrawableState();
        showFingerDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final FingerData fingerData = new FingerData();
                    int ret = mfs100.AutoCapture(fingerData, timeout, true);
                    if (ret != 0) {
                    } else {
                        lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                fingerData.FingerImage().length);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFinger.setImageBitmap(bitmap);
                                showFinger.refreshDrawableState();
                                quality.setText(Html.fromHtml("Quality : <font color='#00ff00'>" + fingerData.Quality() + "%</font>"));
                                quality.refreshDrawableState();
                            }
                        });
                        if (showFingerDialog.isShowing())
                            showFingerDialog.dismiss();
                        mfsListener.onCaptureResult(mfs100, fingerData);
                    }
                } catch (Exception ex) {
                    // SetTextOnUIThread("Error");
                } finally {
                    //   isCaptureRunning = false;
                }
            }
        }).start();
    }


    public interface MFSListener {
        void onCaptureResult(MFS100 mfs100, FingerData fingerData);

        void onDeviceStatusMsg(Spanned status);

        void onDeviceConnected();

        void onDeviceDisconnected();
    }
}
