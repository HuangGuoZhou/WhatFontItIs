package com.advl.whatfontis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jibble.simpleftp.SimpleFTP;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

	private Button btn_examples, btn_take_picture, btn_saved_pictures;
	public ImageView imgview_font_photo;
	public Bitmap camera_bitmap;
	public static String deviceId;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int IMAGE_MAX_SIZE = 800;	
	private Uri fileUri;
	private String _imageUri;
	private Uri mCapturedImageURI;
	public Uri imageUri;
	public String TAG = "WHATFONTIS";
	private Uri mImageCaptureUri;
	private ImageView mImageView;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView txt_1 = (TextView) findViewById(R.id.textView1);
		btn_examples = (Button) findViewById(R.id.button_examples);
		btn_take_picture = (Button) findViewById(R.id.button_camera);
		btn_saved_pictures = (Button) findViewById(R.id.button_saved);
		imgview_font_photo = (ImageView) findViewById(R.id.imageView_font_photo);

		/*
		 * in caz ca vrei sa schimbi fontul la aplicatie asa se face Typeface
		 * tf_regular = Typeface.createFromAsset(getAssets(), "ZagRegular.otf");
		 * Typeface tf_bold = Typeface.createFromAsset(getAssets(),
		 * "ZagBold.otf"); txt_1.setTypeface(tf_bold);
		 */

		// ------------- get UNIQUE device ID
		deviceId = String.valueOf(System.currentTimeMillis()); // --------
																// backup for
																// tablets etc
		try {
			final TelephonyManager tm = (TelephonyManager) getBaseContext()
					.getSystemService(Main.TELEPHONY_SERVICE);
			final String tmDevice, tmSerial, tmPhone, androidId;
			tmDevice = "" + tm.getDeviceId();
			tmSerial = "" + tm.getSimSerialNumber();
			androidId = ""
					+ android.provider.Settings.Secure.getString(
							getContentResolver(),
							android.provider.Settings.Secure.ANDROID_ID);
			UUID deviceUuid = new UUID(androidId.hashCode(),
					((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
			deviceId = deviceUuid.toString();
		} catch (Exception e) {
			Log.v("Attention", "Nu am putut sa iau deviceid-ul!");
		}
		// ------------- END get UNIQUE device ID
		
		
		

		btn_take_picture.setOnClickListener(new View.OnClickListener() {
			

			public void onClick(View v) {									
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				

				intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				

			//	File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Images/testfile.jpg");
				Uri imageUri = getOutputMediaFileUri(1);
				
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

				
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		});
		
		
		
		
		
		

		btn_examples.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent examples_intent = new Intent(Main.this, Example.class);				
				startActivity(examples_intent);
				overridePendingTransition(R.anim.fadein, R.anim.fadeout);

			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		
		
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	
	        	
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            Toast.makeText(Main.this, "Image capture failed. Are you using a 'special device' ?", 1500).show();
	        }
	    }
	    

		

		// imgview_font_photo.setImageBitmap(bm);

		new SendToFTPOperation().execute("");

	}

	
	
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/Images/", "WhatTheFont");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("WhatTheFont", "failed to create directory");
	            return null;
	        }
	    }

	    
	    
	    // Create a media file name	    
	    File mediaFile;
	    if (type == 1){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	         deviceId + ".jpg");
	    }  else {
	        return null;
	    }

	    return mediaFile;
	}
	
	
	
	
	
	
	
	//------------------------------- FTP --------------------------------------
		
	private class SendToFTPOperation extends AsyncTask<String, Void, String> {

		public ProgressDialog dialog;

		@Override
		protected String doInBackground(String... params) {

	
			
			//------------ RESIZE
			camera_bitmap = resizeFile(new File(Environment.getExternalStorageDirectory()
					.getPath() + "/Images/WhatTheFont/"+deviceId+".jpg"));
									
			
			String path = Environment.getExternalStorageDirectory().getPath() + "/Images/WhatTheFont/";
	        OutputStream fOut = null;
	        File file = new File(path, "r"+deviceId+".jpg");
	            try {
					fOut = new FileOutputStream(file);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

	            camera_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
	            try {
					fOut.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            try {
					fOut.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            
	            
	            //crop
	            
	            doCrop();
	            
	            

	       try {
			MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			

			Log.v("Starting Transfer","Starting the transfer...");

			SimpleFTP ftp = new SimpleFTP();
			// Connect to an FTP server on port 21.
			try {
				ftp.connect("alfa3051.alfahosting-server.de", 21, "web1247",
						"Kd8fcTtU");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Set binary mode.
			try {
				ftp.bin();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Change to a new working directory on the FTP server.
			try {
				ftp.cwd("/html/dan/android/whatfontis/");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Upload some files.
			try {
				ftp.stor(new File(Environment.getExternalStorageDirectory()
						.getPath() + "/Images/WhatTheFont/r"+deviceId+".jpg"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Quit from the FTP server.
			try {
				ftp.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// --------------------------- STEP TWO

			Intent step_two_intent = new Intent(Main.this, StepTwo.class);
			step_two_intent.putExtra("deviceId", deviceId);
			startActivity(step_two_intent);
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			Toast.makeText(getApplicationContext(),
					"Picture uploaded successfully", Toast.LENGTH_SHORT).show();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {

			dialog = ProgressDialog.show(Main.this, "",
					"Processing. Please wait...", true);
			dialog.show();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			// Things to be done while execution of l ong running operation is
			// in
			// progress. For example updating ProgessDialog
		}
	}
	
	
	
	//------------------ RESIZE FILE
	private Bitmap resizeFile(File f){
	    Bitmap b = null;
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        FileInputStream fis = new FileInputStream(f);
	        BitmapFactory.decodeStream(fis, null, o);
	        fis.close();

	        int scale = 1;
	        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
	            scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        fis = new FileInputStream(f);
	        b = BitmapFactory.decodeStream(fis, null, o2);
	        fis.close();
	    } catch (IOException e) {
	    }
	    return b;
	}
	
	
	
	//--------------- MENU 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}

	
	
	
	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:

			break;

		case R.id.home:

			break;

		case R.id.exit:
			finish();
			break;

		case R.id.share:
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			String shareBody = "Psychologies https://market.android.com/details?id=com.advl.psychologies";

			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Women News ");
			sharingIntent
					.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "Alege metoda"));
			break;

		case R.id.info:

			break;

		case R.id.more:

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://search?q=pub:ADVL"));
			startActivity(intent);

			break;

		}
		return true;
	}
	
	
	 private void doCrop() {
			final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
	    	
	    	Intent intent = new Intent("com.android.camera.action.CROP");
	        intent.setType("image/*");
	        
	        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
	        
	        int size = list.size();
	        
	        Log.v("Size list is ",String.valueOf(size));
	        
	        if (size == 0) {	        
	        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
	        	
	            return;
	        } else {
	        	intent.setData(mImageCaptureUri);
	            
	            intent.putExtra("outputX", 200);
	            intent.putExtra("outputY", 200);
	            intent.putExtra("aspectX", 1);
	            intent.putExtra("aspectY", 1);
	            intent.putExtra("scale", true);
	            intent.putExtra("return-data", true);
	            
	        	if (size == 1) {
	        		Intent i 		= new Intent(intent);
		        	ResolveInfo res	= list.get(0);

		        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

		        	startActivityForResult(i, CROP_FROM_CAMERA);
	        	} else {
			        for (ResolveInfo res : list) {
			        	final CropOption co = new CropOption();

			        	co.title 	= getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
			        	co.icon		= getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
			        	co.appIntent= new Intent(intent);

			        	co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

			            cropOptions.add(co);
			        }

			        CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        builder.setTitle("Crop Image");
			        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			            public void onClick( DialogInterface dialog, int item ) {
			                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
			            }
			        });

			        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
			            public void onCancel( DialogInterface dialog ) {

			                if (mImageCaptureUri != null ) {
			                    getContentResolver().delete(mImageCaptureUri, null, null );
			                    mImageCaptureUri = null;
			                }
			            }
			        } );

			        AlertDialog alert = builder.create();

			        alert.show();
	        	}
	        }
		}

}