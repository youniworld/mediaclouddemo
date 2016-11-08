package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vlee78.android.media.MediaSdk;
import com.vlee78.android.media.MediaView;

public class ActivityMedia extends Activity implements View.OnClickListener
{
	private MediaView mMediaView;
	private MediaView mPreviewView;
	private TextView mStartBtn;
	private TextView mCloseBtn;
	private TextView mFacingBtn;
	private TextView mRecordBtn;
	private TextView mBeautifyBtn;
	private String mUrl = "";
	private String uid = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		uid = getIntent().getStringExtra("uid");

		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setBackgroundColor(0xffffffff);
		this.setContentView(frameLayout);
		FrameLayout.LayoutParams params = null;
		
		mPreviewView = new MediaView(this);
		mPreviewView.bind(101);
		params = new FrameLayout.LayoutParams(240, 320, Gravity.BOTTOM|Gravity.END);
		params.bottomMargin = 200;
		frameLayout.addView(mPreviewView, params);
		
		mMediaView = new MediaView(this);
		mMediaView.bind(100);
		params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		frameLayout.addView(mMediaView, params);
			
		mStartBtn = new TextView(this);
		mStartBtn.setBackgroundColor(0xffffffff);
		mStartBtn.setTextColor(0xff000000);
		mStartBtn.setText("start");
		mStartBtn.setGravity(Gravity.CENTER);
		params = new FrameLayout.LayoutParams(200, 120, Gravity.TOP|Gravity.START);
		frameLayout.addView(mStartBtn, params);
		mStartBtn.setOnClickListener(this);
		
		mCloseBtn = new TextView(this);
		mCloseBtn.setBackgroundColor(0xffffffff);
		mCloseBtn.setTextColor(0xff000000);
		mCloseBtn.setText("close");
		mCloseBtn.setGravity(Gravity.CENTER);
		params = new FrameLayout.LayoutParams(200, 120, Gravity.TOP|Gravity.END);
		frameLayout.addView(mCloseBtn, params);
		mCloseBtn.setOnClickListener(this);
		
		mFacingBtn = new TextView(this);
		mFacingBtn.setBackgroundColor(0xffffffff);
		mFacingBtn.setTextColor(0xff000000);
		mFacingBtn.setText("close");
		mFacingBtn.setGravity(Gravity.CENTER);
		params = new FrameLayout.LayoutParams(200, 120, Gravity.BOTTOM|Gravity.START);
		frameLayout.addView(mFacingBtn, params);
		mFacingBtn.setOnClickListener(this);
		
		mRecordBtn = new TextView(this);
		mRecordBtn.setBackgroundColor(0xffffffff);
		mRecordBtn.setTextColor(0xff000000);
		mRecordBtn.setText("record");
		mRecordBtn.setGravity(Gravity.CENTER);
		params = new FrameLayout.LayoutParams(200, 120, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
		frameLayout.addView(mRecordBtn, params);
		mRecordBtn.setOnClickListener(this);
		
		mBeautifyBtn = new TextView(this);
		mBeautifyBtn.setBackgroundColor(0xffffffff);
		mBeautifyBtn.setTextColor(0xff000000);
		mBeautifyBtn.setText("normal");
		mBeautifyBtn.setGravity(Gravity.CENTER);
		params = new FrameLayout.LayoutParams(200, 120, Gravity.BOTTOM|Gravity.END);
		frameLayout.addView(mBeautifyBtn, params);
		mBeautifyBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) 
	{
		if(v==mStartBtn)
		{
			//mUrl = "rtmp://101.201.146.134/hulu/seaworld";
			mUrl = "hpsp://b6bc4badacf044a4ab0c08a8f052d0fe:" + uid;

			MediaSdk.open(6, mUrl, 100, 101);
			MediaSdk.setCameraFront(true);
			MediaSdk.setBeautify(false);
			MediaSdk.setPushRecord(false);

			mFacingBtn.setText(MediaSdk.getCameraFront() ? "front" : "back");
			mBeautifyBtn.setText(MediaSdk.getBeautify() ? "beautify" : "normal");
			mRecordBtn.setText(MediaSdk.getPushRecord() ? "recording" : "record");
		}
		else if(v==mCloseBtn)
		{
			MediaSdk.close(mUrl);
			finish();
		}
		else if(v==mFacingBtn)
		{
			MediaSdk.setCameraFront(!MediaSdk.getCameraFront());
			mFacingBtn.setText(MediaSdk.getCameraFront() ? "front" : "back");
		}
		else if(v==mBeautifyBtn)
		{
			MediaSdk.setBeautify(!MediaSdk.getBeautify());
			mBeautifyBtn.setText(MediaSdk.getBeautify() ? "beautify" : "normal");
		}
		else if(v==mRecordBtn)
		{
			MediaSdk.setPushRecord(!MediaSdk.getPushRecord());
			mRecordBtn.setText(MediaSdk.getPushRecord() ? "recording" : "record");
		}
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		MediaSdk.close(mUrl);
	}
}
