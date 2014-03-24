package cx.ath.chuacw.ProcessManager;

import java.io.IOException;
import java.io.InputStream;

public class ProcessController extends Thread  {

	private boolean mTerminateSet;
	private String[] mArgs;
	private StatusListener mStatusListener;
	
	public void terminate() {
		mTerminateSet = true;
	}
	
	ProcessController(String[] args) {
		super();
		mArgs = args;
		mTerminateSet = false;
		mStatusListener = null;
	}
	
	public void setEventListeners(StatusListener aStatusListener) {
		mStatusListener = aStatusListener;
	}
	
	@Override
	public void run() {
		ProcessBuilder cmd;
		cmd = new ProcessBuilder(mArgs);
		cmd.redirectErrorStream(true);
		Process process = null;
		try {
			process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			
			while (!mTerminateSet) {
				// don't do anything
				if (in.available()>0) {
					in.read(re); 
				} else {
					sleep(10);
				}
			}
			in.close();
			if (!mTerminateSet) process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			process.destroy(); // sends SIGTERM
			if (mStatusListener!=null) {
			  mStatusListener.onTerminated();
			}
		}
	}
	
}
