package com.tencent.x5.utils;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.QbSdk.PreInitCallback;
import com.tencent.smtt.utils.TbsLogClient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class FirstLoadingX5Service extends Service{

	private long timerCounter;
	public static final int MSG_WEBVIEW_CONSTRUCTOR = 1;
	public static final int MSG_WEBVIEW_POLLING= 2;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		QbSdk.preInit(this); //这里必须启用非主进程的service来预热X5内核
		QbSdk.setTbsListener(new TbsListener(){

			@Override
			public void onDownloadFinish(int arg0) {
				// TODO Auto-generated method stub
				Log.d("Lyon","tbs下载完成："+arg0);
			}

			@Override
			public void onDownloadProgress(int arg0) {
				// TODO Auto-generated method stub
				Log.d("Lyon","tbs正在下载："+arg0);
				
			}

			@Override
			public void onInstallFinish(int arg0) {
				// TODO Auto-generated method stub
				Log.d("Lyon","tbs安装完成："+arg0);
			}

			@Override
			public void onCallBackErrorCode(int i, String s) {

			}

		});
		QbSdk.setTbsLogClient(new TbsLogClient(){
			@Override
			public void d(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.d("Lyon",arg0+"-->"+arg1);
			}
			
			@Override
			public void e(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.e("Lyon",arg0+"-->"+arg1);
			}
			@Override
			public void v(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.e("Lyon",arg0+"-->"+arg1);
			}
			
			@Override
			public void i(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.i("Lyon",arg0+"-->"+arg1);
			}
			
			@Override
			public void w(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.w("Lyon",arg0+"-->"+arg1);
			}
			
			
		});
		
		handler = new Handler(Looper.getMainLooper()){
			@Override
			public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
				switch(msg.what){
				case MSG_WEBVIEW_CONSTRUCTOR:

					Log.i("yuanhaizhou", "service is success");
					break;
				case MSG_WEBVIEW_POLLING:
					Log.i("yuanhaizhou", "service is loading");
					polling();//循环查询X5内核是否加载
					break;
				}
			super.handleMessage(msg);
		}
		};

		
		preinitX5WebCore();
	}

	/**
	 * X5内核在使用preinit接口之后，对于首次安装首次加载没有效果
	 * 实际上，X5webview的preinit接口只是降低了webview的冷启动时间；
	 * 因此，现阶段要想做到首次安装首次加载X5内核，必须要让X5内核提前获取到内核的加载条件
	 */
	private void preinitX5WebCore(){

		Log.i("yuanhaizhou", "service is start");

		if(!QbSdk.isTbsCoreInited()){//preinit只需要调用一次，如果已经完成了初始化，那么就直接构造view
			Log.i("yuanhaizhou", "service is preStarting");
			QbSdk.preInit(this, myCallback);//设置X5初始化完成的回调接口  第三个参数为true：如果首次加载失败则继续尝试加载；
			Log.i("yuanhaizhou", "service is preStart");
		}else{
			handler.sendEmptyMessage(MSG_WEBVIEW_CONSTRUCTOR);
		}
	}
	

	private Handler handler;
	/**
	 * 需要不断检查网络反馈信息是否到达
	 */
	private void polling(){
		Log.i("yuanhaizhou", "polling");
		Toast.makeText(this, "polling", Toast.LENGTH_SHORT).show();
		if(QbSdk.isTbsCoreInited()){
			handler.sendEmptyMessage(MSG_WEBVIEW_CONSTRUCTOR);
		}else{
			handler.sendEmptyMessageDelayed(MSG_WEBVIEW_POLLING, 500);
		}
	}
	
	private PreInitCallback myCallback=new PreInitCallback() {
		
		@Override
		public void onViewInitFinished() {//当X5webview 初始化结束后的回调
			// TODO Auto-generated method stub
			float deltaTime=(System.currentTimeMillis()-timerCounter)/1000;
			Toast.makeText(getApplicationContext(), "x5初始化使用了"+deltaTime+"秒", Toast.LENGTH_LONG).show();
			Log.i("yuanhaizhou", "x5初始化使用了"+deltaTime+"秒");
			polling();
		}
		
		@Override
		public void onCoreInitFinished() {
			// TODO Auto-generated method stub
			Log.i("yuanhaizhou","onCoreInitFinished");
			
			
		}

	
	};
	
}
