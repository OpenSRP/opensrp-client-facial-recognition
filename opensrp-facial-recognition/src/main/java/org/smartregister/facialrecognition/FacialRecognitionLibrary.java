package org.smartregister.facialrecognition;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Window;

import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import org.smartregister.Context;

import org.smartregister.facialrecognition.domain.ProfileImage;
import org.smartregister.facialrecognition.repository.ImageRepository;
import org.smartregister.facialrecognition.util.BitmapUtil;
import org.smartregister.facialrecognition.util.FSDK;
import org.smartregister.facialrecognition.utils.FaceConstants;
import org.smartregister.facialrecognition.utils.Tools;
import org.smartregister.repository.Repository;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by wildan on 10/2/17.
 */

public class FacialRecognitionLibrary {

    private static final String TAG = FacialRecognitionLibrary.class.getSimpleName();
    private static final String ALBUM_NAME = "smart_album";
    private static Boolean isDevCompat;
    public static FacialProcessing faceProc = null;
    private static boolean activityStartedOnce = false;
    private static int confidence_value = 58;

    private static String ACTIVATION_KEY = "";

    private final Repository repository;
    private final Context context;
    private static FacialRecognitionLibrary instance;
    private ImageRepository facialRepository;

    private FacialRecognitionLibrary(Context context, Repository repository){
        this.context = context;
        this.repository = repository;
    }

    public static void init(final Context context, Repository repository){
        if (instance == null) instance = new FacialRecognitionLibrary(context, repository);

        // IF SNAPDRAGON
        int sdk_lib = 1;

        if (sdk_lib == 1){
            snapdragonSDK();
        } else if (sdk_lib == 2){
            luxandSDK();
        }

    }

    private static void luxandSDK() {
        Log.d(TAG, "luxandSDK: Library Used" );
//        Log.e(TAG, "luxandSDK: Library Used" );
        int res = FSDK.ActivateLibrary(ACTIVATION_KEY);

        if (res != FSDK.FSDKE_OK) {
            Log.e(TAG, "onCreate: FAILED" );
        } else {
            Log.e(TAG, "onCreate: SUCCESS" );
            FSDK.Initialize();
        }
    }

    private static void snapdragonSDK() {
        Log.d(TAG, "snapdragonSDK: Library Used" );
//        Log.e(TAG, "snapdragonSDK: Library Used" );
        if (isDevCompat == null) isDevCompat = FacialProcessing.isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_RECOGNITION);

        if (!activityStartedOnce) {
            activityStartedOnce = true;
            // Check if Facial Recognition feature is supported in the device
            boolean isSupported = FacialProcessing
                    .isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_RECOGNITION);
            if (isSupported) {
                Log.d(TAG, "Feature Facial Recognition is supported");
                if (faceProc == null) {
                    faceProc = FacialProcessing.getInstance();
                }

                if (faceProc != null) {
                    faceProc.setRecognitionConfidence(confidence_value);
                    faceProc.setProcessingMode(FacialProcessing.FP_MODES.FP_MODE_STILL);

                }

            } else {
                Log.e(TAG, "Feature Facial Recognition is NOT supported");
//                new AlertDialog.Builder(this)
//                        .setMessage(
//                                "Your device does NOT support Qualcomm's Facial Recognition feature. ")
//                        .setCancelable(false)
//                        .setNegativeButton("OK",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int id) {
//                                        finish();
//                                    }
//                                }).show();
            }
        }

    }

    public static FacialRecognitionLibrary getInstance(){
        if (instance == null)
            throw new IllegalStateException("Instance does not exist. Call "+ FacialRecognitionLibrary.class.getName()+".init method in the onCreate method of your Application class!.");

        return instance;
    }

    public ImageRepository facialRepository() {
        if (facialRepository == null)
            facialRepository = new ImageRepository(getRepository());

        Tools.setVectorfromAPI(context, facialRepository);
        Tools.setVectorsBuffered(context, facialRepository);
//        Tools.loadAlbum(context.applicationContext());

        BitmapUtil.loadAlbum(context.applicationContext());

        return facialRepository;
    }

    public Repository getRepository() {
        if (repository == null) Log.e(TAG, "getRepository: "+ null );
        return repository;
    }

    public static Boolean getDevCompat(){
        return isDevCompat;
    }

    public Context getContext(){
        return context;
    }

}
