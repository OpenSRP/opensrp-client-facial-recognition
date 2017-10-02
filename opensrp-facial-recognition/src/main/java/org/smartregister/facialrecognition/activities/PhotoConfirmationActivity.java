package org.smartregister.facialrecognition.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import org.smartregister.facialrecognition.R;
import org.smartregister.facialrecognition.utils.FaceConstants;
import org.smartregister.facialrecognition.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;


public class PhotoConfirmationActivity extends AppCompatActivity {

    private static String TAG = PhotoConfirmationActivity.class.getSimpleName();
    private Bitmap storedBitmap;
    private Bitmap workingBitmap;
    private Bitmap mutableBitmap;
    ImageView confirmationView;
    ImageView confirmButton;
    ImageView trashButton;
    private String entityId;
    private Rect[] rects;
    private boolean faceFlag = false;
    private boolean identifyPerson = false;
    private FacialProcessing objFace;
    private FaceData[] faceDatas;
    private int arrayPossition;
    Tools tools;
    HashMap<String, String> clientList;
    private String selectedPersonName = "";
    private Parcelable[] kiclient;

    String str_origin_class;

    byte[] data;
    int angle;
    boolean switchCamera;
    private byte[] faceVector;
    private boolean updated = false;

//    private DrawerLayout drawerLayout;
    private boolean isDrawerOpened;
    private MaterialMenuDrawable materialMenu;
    private Context context;
    private boolean cameraFront = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fr_image_face_confirmation);

        context = getApplicationContext();

        init_gui();

        init_extras();

//        process_img();

        image_proc();

        buttonJob();
    }

    private void image_proc() {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        Log.e(TAG, "image_proc: sW x sH "+ screenWidth +" x "+ screenHeight );
        storedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);

        Log.e(TAG, "image_proc: "+ switchCamera );
        Matrix mat = new Matrix();

        Log.e(TAG, "image_proc: wxh "+ storedBitmap.getWidth() +" x "+ storedBitmap.getHeight() );

        if (!cameraFront) {
            Log.e(TAG, "process_img: Face FRONT "+angle );
            mat.postRotate(angle == 90 ? 270 : (angle == 180 ? 180 : 0));
//            mat.postScale(-1, 1);
            storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
        } else {
            Log.e(TAG, "process_img: Face BACK" );
            mat.postRotate(angle == 90 ? 90 : (angle == 180 ? 180 : 0));
            storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
        }
        Log.e(TAG, "image_proc: wxh "+ storedBitmap.getWidth() +" x "+ storedBitmap.getHeight() );

//        confirmationView.setImageBitmap(storedBitmap);
        Bitmap scaled = Bitmap.createScaledBitmap(storedBitmap, screenWidth,screenHeight, true);
        confirmationView.setImageBitmap(scaled);
    }

    private void init_gui() {


        // Display New Photo
        confirmationView = (ImageView) findViewById(R.id.iv_confirmationView);
        trashButton = (ImageView) findViewById(R.id.iv_cancel);
        confirmButton = (ImageView) findViewById(R.id.iv_approve);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                // Handle your drawable state here
////                materialMenu.animateState(newState);
//            }
//        });
//        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
//        toolbar.setNavigationIcon(materialMenu);
    }

    /**
     * Method to get Info from previous Intent
     */
    private void init_extras() {
        Bundle extras = getIntent().getExtras();
        data = getIntent().getByteArrayExtra("org.smartregister.facialrecognition.PhotoConfirmationActivity");
        angle = extras.getInt("org.smartregister.facialrecognition.PhotoConfirmationActivity.orientation");
        cameraFront = extras.getBoolean("org.smartregister.facialrecognition.PhotoConfirmationActivity.switchCamera");
        entityId = extras.getString("org.smartregister.facialrecognition.PhotoConfirmationActivity.id");
        identifyPerson = extras.getBoolean("org.smartregister.facialrecognition.PhotoConfirmationActivity.identify");
        kiclient = extras.getParcelableArray("org.smartregister.facialrecognition.PhotoConfirmationActivity.kiclient");
        str_origin_class = extras.getString("org.smartregister.facialrecognition.PhotoConfirmationActivity.origin");
        updated = extras.getBoolean("org.smartregister.facialrecognition.PhotoConfirmationActivity.updated");
        Log.e(TAG, "init_extras: updated "+updated );
    }

    private void process_img() {

        storedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
//        Log.e(TAG, "process_img: storedBitmap "+ storedBitmap ); // 720 x 1280
//        Log.e(TAG, "process_img: storedBitmap h "+ storedBitmap.getHeight() );
//        Log.e(TAG, "process_img: storedBitmap w "+ storedBitmap.getWidth() );
        objFace = OpenCameraActivity.faceProc;

        Matrix mat = new Matrix();
        if (!cameraFront) {
            Log.e(TAG, "process_img: FALSE" );
            mat.postRotate(angle == 0 ? 270 : (angle == 180 ? 180 : 0));
            mat.postScale(-1, 1);
            storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
        } else {
            Log.e(TAG, "process_img: TRUE" );
            mat.postRotate(angle == 90 ? 90 : (angle == 180 ? 180 : 0));
            storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
        }
//        TODO : Image from gallery

//        Retrieve data from Local Storage
        clientList = OpenCameraActivity.retrieveHash(getApplicationContext());

        boolean setBitmapResult = objFace.setBitmap(storedBitmap);
        faceDatas = objFace.getFaceData();

        Log.e(TAG, "process_img: confirmationView w/h "+confirmationView.getWidth() +"/"+confirmationView.getHeight() ); // w/h 1536/1872

        /**
         * Set Height and Width
         */
        int imageViewSurfaceWidth = storedBitmap.getWidth()/2;
        int imageViewSurfaceHeight = storedBitmap.getHeight()/2;
//        int imageViewSurfaceWidth = confirmationView.getWidth();
//        int imageViewSurfaceHeight = confirmationView.getHeight();

        // Face Confirmation view purpose
        workingBitmap = Bitmap.createScaledBitmap(storedBitmap, imageViewSurfaceWidth, imageViewSurfaceHeight, false);

        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

//        mutableBitmap = storedBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Bitmap tempBitmap = Bitmap.createScaledBitmap(storedBitmap,
//                (storedBitmap.getWidth() / 20), (storedBitmap.getHeight() / 20),
//                false);

        confirmationView.setImageBitmap(mutableBitmap);
//        confirmationView.setImageBitmap(tempBitmap);

        objFace.normalizeCoordinates(imageViewSurfaceWidth, imageViewSurfaceHeight);

        // Set Bitmap Success
        if(setBitmapResult){

            // Only if Face Data Exist
            if(faceDatas != null){
//                Log.e(TAG, "onCreate: faceDatas "+faceDatas.length );
                rects = new Rect[faceDatas.length];

                for (int i = 0; i < faceDatas.length; i++) {
                    Rect rect = faceDatas[i].rect;
                    rects[i] = rect;

                    int matchRate = faceDatas[i].getRecognitionConfidence();

                    float pixelDensity = getResources().getDisplayMetrics().density; // 2.0

//                    Identify or new record
                    if (identifyPerson) {
                        String selectedPersonId = Integer.toString(faceDatas[i].getPersonId());
                        Iterator<HashMap.Entry<String, String>> iter = clientList.entrySet().iterator();
                        // Default name is the person is unknown
                        selectedPersonName = "Not Identified";
                        while (iter.hasNext()) {
                            Log.e(TAG, "process_img: Identified" );
                            HashMap.Entry<String, String> entry = iter.next();
                            if (entry.getValue().equals(selectedPersonId)) {
                                selectedPersonName = entry.getKey();
                            }
                        }

                        Toast.makeText(getApplicationContext(), selectedPersonName, Toast.LENGTH_SHORT).show();

//                        Draw Info on Image
                        Tools.drawInfo(rect, mutableBitmap, pixelDensity, selectedPersonName);

                        showDetailUser(selectedPersonName);

                    } else {

                        // Not Identifiying, do new record.
//                        Draw Info on Image
                        Log.e(TAG, "process_img: rect "+ rect.toString() ); // Rect(125, 409 - 847, 951)
                        Tools.drawRectFace(rect, mutableBitmap, pixelDensity);

                        Log.e(TAG, "onCreate: PersonId "+faceDatas[i].getPersonId() );

                        // Check Detected existing face
                        if(faceDatas[i].getPersonId() < 0){

                            arrayPossition = i;

                        } else {

                            showPersonInfo(matchRate);

                        }

//                        TODO: asign selectedPersonName to search
                        // Applied Image that came in to the view.
                        // Face only
//                        confirmationView.setImageBitmap(storedBitmap);

                        // Face and Rect
//                        confirmationView.setImageBitmap(mutableBitmap);
                        Drawable drawable = confirmationView.getDrawable();//you should call after the bitmap drawn
                        Rect bounds = drawable.getBounds();
                        int width = bounds.width();
                        int height = bounds.height();
                        int bitmapWidth = drawable.getIntrinsicWidth(); //this is the bitmap's width
                        int bitmapHeight = drawable.getIntrinsicHeight(); //this is the bitmap's height

                        Log.e(TAG, "process_img: w "+ width );
                        Log.e(TAG, "process_img: h "+ height );
                        Log.e(TAG, "process_img: bw "+ bitmapWidth );
                        Log.e(TAG, "process_img: bh "+ bitmapHeight );
                    } // end if-else mode Identify {True or False}

                } // end for count ic_faces

            } else {

                Log.e(TAG, "onCreate: faceDatas "+"Null" );
                Toast.makeText(PhotoConfirmationActivity.this, "No Face Detected", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
//                PhotoConfirmationActivity.this.finish();
            }

        } else {

            Log.e(TAG, "onCreate: SetBitmap objFace"+"Failed" );

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_confirmation, menu);
        return true;
    }

    public void showDetailUser(String selectedPersonName) {

        Log.e(TAG, "showDetailUser: " );
        Class<?> origin_class = this.getClass();

//        if(str_origin_class.equals(NativeKISmartRegisterFragment.class.getSimpleName())){
//            origin_class = NativeKISmartRegisterActivity.class;
//        } else if(str_origin_class.equals(NativeKBSmartRegisterFragment.class.getSimpleName())){
//            origin_class = NativeKBSmartRegisterActivity.class;
//        } else if(str_origin_class.equals(NativeKIAnakSmartRegisterFragment.class.getSimpleName())){
//            origin_class = NativeKIAnakSmartRegisterActivity.class;
//        } else if(str_origin_class.equals(NativeKIANCSmartRegisterFragment.class.getSimpleName())){
//            origin_class = NativeKIANCSmartRegisterActivity.class;
//        } else if(str_origin_class.equals(NativeKIPNCSmartRegisterFragment.class.getSimpleName())){
//            origin_class = NativeKIPNCSmartRegisterActivity.class;
//        }

        Intent intent = new Intent(PhotoConfirmationActivity.this, origin_class);
        intent.putExtra("org.ei.opensrp.indonesia.face.face_mode", true);
        intent.putExtra("org.ei.opensrp.indonesia.face.base_id", selectedPersonName);

        startActivity(intent);

    }

    private void showPersonInfo(int recognitionConfidence) {
        Log.e(TAG, "showPersonInfo: Similar face found " +
                Integer.toString(recognitionConfidence));

        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setTitle("Are you Sure?");
        builder.setMessage("Similar Face Found! : Confidence "+recognitionConfidence);
        builder.setNegativeButton("CANCEL", null);
        builder.show();
        confirmButton.setVisibility(View.INVISIBLE);

    }

    /**
     *
     */
    private void buttonJob() {
        // If approved then save the image and close.
        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!identifyPerson) {

                    Log.e(TAG, "onClick: class origin "+str_origin_class );

                    Tools.saveAndClose(getApplicationContext(), entityId, updated, objFace, arrayPossition, storedBitmap, str_origin_class);

                    // Back To Detail Activity
                    Intent i = new Intent();
                    setResult(2, i);
                    finish();

                } else {
                    Log.e(TAG, "onClick: not identify ");
                    // TODO: detect origin class
//                    KIDetailActivity.kiclient = (CommonPersonObjectClient) arg0.getTag();
//                    Log.e(TAG, "onClick: " + KIDetailActivity.kiclient);
//                    Intent intent = new Intent(PhotoConfirmationActivity.this,KIDetailActivity.class);
                    Log.e(TAG, "onClick: " + selectedPersonName);
//                    startActivity(intent);
                }
            }

        });

        confirmButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    confirmButton.setImageResource(R.drawable.ic_confirm_highlighted_24dp);
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    confirmButton.setImageResource(R.drawable.ic_confirm_white_24dp);
                }

                return false;
            }
        });

        // Trash the image and return back to the camera preview.
        trashButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
                PhotoConfirmationActivity.this.finish();
            }

        });

        trashButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    trashButton.setImageResource(R.drawable.ic_trash_delete_green);
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    trashButton.setImageResource(R.drawable.ic_trash_delete);
                }

                return false;
            }
        });

    }

    /*
    Save File and DB
     */
    private void saveAndClose(String entityId) {

        Log.e(TAG, "saveAndClose: updated "+ updated );

        faceVector = objFace.serializeRecogntionAlbum();

        Log.e(TAG, "saveAndClose: " + Arrays.toString(faceVector));

        int result = objFace.addPerson(arrayPossition);
        clientList.put(entityId, Integer.toString(result));

        byte[] albumBuffer = OpenCameraActivity.faceProc.serializeRecogntionAlbum();

        OpenCameraActivity.faceProc.resetAlbum();

//        Tools.WritePictureToFile(PhotoConfirmationActivity.this, storedBitmap, entityId, albumBuffer, updated);
        // TODO : change album buffer to String[]
//        Tools.WritePictureToFile(storedBitmap, entityId, albumBuffer, updated);

        PhotoConfirmationActivity.this.finish();

//        Intent resultIntent = new Intent(this, KIDetailActivity.class);
//        setResult(RESULT_OK, resultIntent);
//        startActivityForResult(resultIntent, 1);

        Log.e(TAG, "saveAndClose: "+"end" );
    }

    public void saveHash(HashMap<String, String> hashMap, android.content.Context context) {
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        Log.e(TAG, "Hash Save Size = " + hashMap.size());
        for (String s : hashMap.keySet()) {
            editor.putString(s, hashMap.get(s));
        }
        editor.apply();
    }

    public void saveAlbum() {
        byte[] albumBuffer = OpenCameraActivity.faceProc.serializeRecogntionAlbum();
//		saveCloud(albumBuffer);
        Log.e(TAG, "Size of byte Array =" + albumBuffer.length);
        SharedPreferences settings = getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(FaceConstants.ALBUM_ARRAY, Arrays.toString(albumBuffer));
        editor.apply();
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference(AllConstantsINA.FIREBASE_OPENSRP_INA)
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(mRestaurant.getPushId())
//                .child("imageUrl");
//        ref.setValue(imageEncoded);
    }

}
