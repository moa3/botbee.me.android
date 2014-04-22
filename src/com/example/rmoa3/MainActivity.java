package com.example.rmoa3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class MainActivity extends Activity {
	
	String TAG = "RMOA3";
	
	public class RemoteCtrlServer extends WebSocketServer {

		public RemoteCtrlServer( int port ) throws UnknownHostException {
			super( new InetSocketAddress( port ) );
		}

		public RemoteCtrlServer( InetSocketAddress address ) {
			super( address );
		}

		@Override
		public void onOpen( WebSocket conn, ClientHandshake handshake ) {
			Log.d(TAG, "new connection: " + handshake.getResourceDescriptor() );
			Log.d(TAG, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
		}

		@Override
		public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
			Log.d(TAG, conn + " has left the room!" );
		}

		@Override
		public void onMessage( WebSocket conn, String message ) {
			arduinoSend(message);
			Log.d(TAG, conn + ": " + message );
		}

		@Override
		public void onFragment( WebSocket conn, Framedata fragment ) {
			Log.d(TAG, "received fragment: " + fragment );
		}

		@Override
		public void onError( WebSocket conn, Exception ex ) {
			Log.d(TAG, "Error");
			ex.printStackTrace();
			if( conn != null ) {
				// some errors like port binding failed may not be assignable to a specific websocket
			}
		}
	}
	
	private String sToS(String s) {
		int i;
		try {
			i = Integer.parseInt(s);	
		} catch (NumberFormatException e) {
			i = 0;
		}
		if (i > 255) {
			i = 255;
		}
		return Character.toString((char) i);
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    try {
		    startWS();
	    } catch (IOException e) {
			
		} catch (InterruptedException e) {
			
		}
	    setContentView(R.layout.fragment_main);
	}
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		EditText editText = (EditText) findViewById(R.id.edit_message);
		String message = editText.getText().toString();
		arduinoSend(message);
	}
	
	public void arduinoSend(String message) {
		message = sToS(message);
		
		// Get UsbManager from Android.
		UsbManager manager1 = (UsbManager) getSystemService(Context.USB_SERVICE);

		// Find the first available driver.
		UsbSerialDriver driver1 = UsbSerialProber.acquire(manager1);

		if (driver1 != null) {
			try {
				driver1.open();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				driver1.setBaudRate(9600);

				byte[] send = message.getBytes();
				driver1.write(send, 1000);


			} catch (IOException e) {
				// Deal with error.
			} finally {
				try {
					driver1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
		}
	}

	
	public void startWS() throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		RemoteCtrlServer s = new RemoteCtrlServer( 8887 );
		s.start();
		Log.d(TAG, "ChatServer started on port: " + s.getPort() );
	}

}
