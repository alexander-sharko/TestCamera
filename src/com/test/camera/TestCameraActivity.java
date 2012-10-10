package com.test.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Object;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;
import android.view.View;


public class TestCameraActivity extends Activity implements 
View.OnClickListener {
    
	/** Properties **/
	protected Button mainBtn;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	public static final String PACKAGE_NAME = "com.test.camera";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/TestCamera/";
	
	public static final String IMAGE_PATH = DATA_PATH + "/ocr.jpg";
	
	// You should have the trained data file in assets folder
	// You can get them at:
	// http://code.google.com/p/tesseract-ocr/downloads/list
	public static final String lang = "eng";
	private static final String TAG = "TestCamera.java";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	
    	// Creates directories application files directory and tessdata subdirectory 
    	
    	String[] pathsToCreate = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : pathsToCreate) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}
		}
		
		// Copy trained data from app assets to tessdata folder for tess lib to use
		String traineddata = "tessdata/" + lang + ".traineddata";
		if (!(new File(DATA_PATH + traineddata)).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open(traineddata);
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH + traineddata);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
				
				Log.v(TAG, "Copied " + traineddata);
			} catch (IOException e) {
				Log.v(TAG, "Was unable to copy " + traineddata + e.toString());
			}
		}
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        mainBtn = (Button) findViewById(R.id.button1);
        mainBtn.setOnClickListener(this);
                
        startCameraActivity();
    }

	public void onClick(View v) {
		
		startCameraActivity();
	}
	
	public void startCameraActivity(){
		
		File file = new File(IMAGE_PATH);
		Uri outputFileUri = Uri.fromFile(file);
		
	 // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        
     // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	            // Image captured and saved to fileUri specified in the Intent
	            
	        	/*Bundle extras = data.getExtras();	  
	        	Bitmap image = (Bitmap) extras.get("data");
	        	
	        	/*ImageView imageView1 = (ImageView) findViewById (R.id.imageView1);
	        	imageView1.setImageBitmap(image);*/
	        	
	        	BitmapFactory.Options options = new BitmapFactory.Options();
	    		options.inSampleSize = 4;

	    		Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH, options);
	    		// Convert to ARGB_8888, required by tess
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
	    		
				TessBaseAPI baseApi = new TessBaseAPI();
				baseApi.setDebug(true);
				baseApi.init(DATA_PATH, lang);
				baseApi.setImage(bitmap);
				
				String recognizedText = baseApi.getUTF8Text();
				
				baseApi.end();
				
				Toast.makeText(this, recognizedText, Toast.LENGTH_LONG).show();
				
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            Log.v(TAG, "Image capture failed");
	        }
	    }
	}

}