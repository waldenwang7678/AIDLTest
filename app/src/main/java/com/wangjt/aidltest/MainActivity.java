package com.wangjt.aidltest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.walden.aidl.MyAidl;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MyAidl aidl;
    boolean useAIDL = false;   //进程通信, 使用或不使用 aidl ,
    IBinder binder;
    Messenger mMessenger;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (useAIDL) {
                aidl = MyAidl.Stub.asInterface(iBinder);
            } else {
                binder = iBinder;
            }
            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            aidl = null;
            binder = null;
            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
        }
    };
    private ServiceConnection conMessenger = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mMessenger = new Messenger(iBinder);
            //todo
            
            Toast.makeText(MainActivity.this, "messenger连接成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMessenger = null;
            Toast.makeText(MainActivity.this, "messenger连接失败", Toast.LENGTH_SHORT).show();
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle != null) {
                String str = bundle.getString("address");
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVIew();
    }

    private void initVIew() {
        findViewById(R.id.bindService).setOnClickListener(this);
        findViewById(R.id.unbindService).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.min).setOnClickListener(this);
        findViewById(R.id.messenger_connect).setOnClickListener(this);
        Switch swButton = (Switch) findViewById(R.id.switch1);
        swButton.setChecked(true);
        swButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    useAIDL = true;
                } else {
                    useAIDL = false;
                }
                connect();  //切换一次就连接一次
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bindService:
                connect();  //连接
                break;
            case R.id.unbindService:
                unbindService(connection);  //断开连接
                Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();
                break;
            case R.id.add:

                if (aidl != null || binder != null) {
                    if (useAIDL) {
                        try {
                            int result = aidl.add(4, 6);
                            Toast.makeText(this, result + "", Toast.LENGTH_SHORT).show();

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        int _result;
                        try {
                            _data.writeInterfaceToken("CalcServiceNoAidl");
                            _data.writeInt(50);
                            _data.writeInt(12);
                            binder.transact(0x110, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readInt();
                            Toast.makeText(this, _result + "", Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }
                } else {
                    Toast.makeText(this, "服务端未绑定或被异常杀死，请重新绑定服务端", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.min:
                if (aidl != null || binder != null) {
                    if (useAIDL) {
                        try {
                            int re = aidl.min(34, 23);
                            Toast.makeText(this, re + "", Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        android.os.Parcel _data = android.os.Parcel.obtain();
                        android.os.Parcel _reply = android.os.Parcel.obtain();
                        int _result;
                        try {
                            _data.writeInterfaceToken("CalcServiceNoAidl");
                            _data.writeInt(36);
                            _data.writeInt(12);
                            binder.transact(0x111, _data, _reply, 0);
                            _reply.readException();
                            _result = _reply.readInt();
                            Toast.makeText(this, _result + "", Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } finally {
                            _reply.recycle();
                            _data.recycle();
                        }
                    }
                } else {
                    Toast.makeText(this, "服务端未绑定或被异常杀死，请重新绑定服务端", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.messenger_connect:
                Intent intent = new Intent();
                intent.setAction("com.walden.messenger");  //连接 messenger service
                intent.setPackage("com.wangjt.aidlservice");
                bindService(intent, conMessenger, Context.BIND_AUTO_CREATE);  //连接到一个服务
                break;
        }
    }

    private void connect() {
        Intent intent = new Intent();
        if (useAIDL) {
            intent.setAction("com.walden.aidl");   //服务端提供的服务
        } else {
            intent.setAction("com.walden.aidl_no_aidl");  //不使用 AIDL
        }
        intent.setPackage("com.wangjt.aidlservice");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);  //连接到一个服务
    }
}
