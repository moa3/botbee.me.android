package com.example.rmoa3;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.shokai.firmata.ArduinoFirmata;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {
	
	String TAG = "RMOA3";
	private WebSocketClient cc;
	private String uri = "ws://bot.ceccaldi.eu/control/";
	private ArduinoFirmata arduino;
	
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
	    arduino = new ArduinoFirmata(this);
	    try {
	    	arduino.connect();
	    } catch (IOException e) {
	    	Log.d(TAG, "IOException caught for arduino.connect\n" );
		} catch (InterruptedException e) {
			Log.d(TAG, "InterruptedException caught for arduino.connect\n" );
		}
	    try {
		    startWS();
	    } catch (IOException e) {
	    	Log.d(TAG, "IOException caught\n" );
		} catch (InterruptedException e) {
			Log.d(TAG, "InterruptedException caught\n" );
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
		int speed = 0;
		int direction = 0;
		int right = 0;
		int left = 0;
		try {
			JSONObject jObject = new JSONObject(message);
			speed = jObject.getInt("speed");
			direction = jObject.getInt("dir");
			Log.d(TAG, "Speed: " + speed + "\n" );
			Log.d(TAG, "Direction: " + direction + "\n" );
		} catch (JSONException e) {
			Log.d(TAG, "JSON" + message + " could not be parsed\n" );
			e.printStackTrace();
		}
		
		if(speed > 0 && speed <= 255) {
			left = right = speed;
		}
		
		if(direction > 100 && direction <= 200) { //turn right
			right = right - ((direction - 100) * right / 100);
		} else if(direction < 100 && direction >= 0) { //turn left
			left = left - ((100 - direction) * left / 100);
		}

		Log.d(TAG, "Left: " + left + "\n" );
		Log.d(TAG, "Right: " + right + "\n" );
		arduino.analogWrite(11, left);
		arduino.analogWrite(3, right);
	}

	
	public void startWS() throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		startListeningToWS();
	}

}
