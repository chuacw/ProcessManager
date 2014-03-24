package cx.ath.chuacw.ProcessManager;

/*
	chuacw@gmail.com
	http://chuacw.ath.cx/blogs/chuacw/default.aspx
	http://www.linkedin.com/in/cwchua
	http://github/chuacw
 
	ProcessManager that starts a process with a click button

 */

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ProcessManager extends Activity implements StatusListener {

	private ProcessState mState;
	Button mBtnStart;
	ProcessController mPC;
	Handler mHandler;
	Runnable mProcessStart;
	Runnable mProcessStop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mState = ProcessState.START;
		mPC	   = null;
		mHandler = new Handler(getMainLooper());
		mProcessStart = new Runnable() {
			@Override
			public void run() {
				startProcess();
			}
		};
		mProcessStop = new Runnable() {
			@Override
			public void run() {
				mPC.terminate();
				mPC = null;
			}
		};
		mBtnStart = (Button) findViewById(R.id.btnStart);
		mBtnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeState();
			}
		});
	}
	
	private void changeState() {
		switch (mState) {
			case STOPPING:
				// if the process is in the middle of stopping, it should be stopping in a while.
				// fall through to START and act as if starting the process
			case START:
// if it's stopping, send a SIGTERM by using process.destroy();
// then start it again
				// Start it
				changeState(ProcessState.STARTING);
				mHandler.post(mProcessStart);
				break;
			case STARTING:
				// if the process is in the middle of starting state, and the button is pressed,
				// fall through to STOP and act as if we're stopping the spawned process
			case STOP:
				changeState(ProcessState.STOPPING);
				mHandler.post(mProcessStop);
				break;
			default:
				break;
		}
	}

	private void changeState(ProcessState newState) {
		mState = newState;
		switch (newState) {
			case STARTING:
				mBtnStart.setText(R.string.Starting);
			    break;
			case STOP:
				mBtnStart.setText(R.string.Stop);
				break;
			case STOPPING:
				mBtnStart.setText(R.string.Stopping);
			    break;
			case START:
			case STOPPED:
				mBtnStart.setText(R.string.Start);
			    break;
			default:
				break;
		}
	}
	
	private void startProcess() {
		mPC = new ProcessController(new String[]{"sleep", "100000"});
		mPC.setEventListeners(this);
		mPC.start();
		changeState(ProcessState.STOP);				
	}
	
	@Override
	public void onTerminated() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				changeState(ProcessState.START);
			}
		});
	}
	
}
