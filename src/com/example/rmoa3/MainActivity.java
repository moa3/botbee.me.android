package com.example.rmoa3;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;


public class MainActivity extends Activity {
	
	String TAG = "RMOA3";
	private WebSocketClient cc;
	private String uri = "ws://bot.ceccaldi.eu/control/";
	
	public void startListeningToWS() {
		try {

			cc = new WebSocketClient(new URI( uri ), (Draft) new Draft_76() ) {

				@Override
				public void onMessage( String message ) {
					Log.d(TAG, "got message: " + message + "\n" );
					arduinoSend(message);
				}

				@Override
				public void onOpen( ServerHandshake handshake ) {
					Log.d(TAG, "You are connected to ChatServer: " + getURI() + "\n" );
				}

				@Override
				public void onClose( int code, String reason, boolean remote ) {
					Log.d(TAG, "You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n" );
					try {
					    startWS();
				    } catch (IOException e) {
						
					} catch (InterruptedException e) {
						
					}
				}

				@Override
				public void onError( Exception ex ) {
					Log.d(TAG, "Exception occured ...\n" + ex + "\n" );
					//ex.printStackTrace();
				}
			};

			cc.connect();
		} catch ( URISyntaxException ex ) {
			Log.d(TAG, uri + "is not a valid WebSocket URI\n" );
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
		startListeningToWS();
	}

}
