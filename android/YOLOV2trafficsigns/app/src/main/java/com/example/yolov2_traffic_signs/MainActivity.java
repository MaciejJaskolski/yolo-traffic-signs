package com.example.yolov2_traffic_signs;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import com.example.yolov2_traffic_signs.comparators.DetectionComparator;
import com.example.yolov2_traffic_signs.models.CellRecognition;
import com.example.yolov2_traffic_signs.models.Detection;
import com.example.yolov2_traffic_signs.models.TrafficSigns;
import com.example.yolov2_traffic_signs.services.GetTrafficSigns;
import com.example.yolov2_traffic_signs.services.LocationService;
import com.example.yolov2_traffic_signs.utils.ArgMax;
import com.example.yolov2_traffic_signs.utils.Softmax;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import org.apache.commons.math3.analysis.function.Sigmoid;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TensorFlowInferenceInterface inferenceInterface;

    private CameraView camera;
    private List<String> labels;

    private TextView txtCurrentLocation;
    private Location currentLocation;

    private TextView txtCurrentDetection;

    private Button switchToMaps;

    Intent intentLocation;
    LocationService locationService;

    private String detectionText;

    private final String OUTPUT_NAME = "output";
    private final int CLASS_NUMBER = 43;
    private final int PIXEL_DEPTH = 3;
    private final int GRID_SIZE = 13;
    private final String INPUT_NAME = "input";
    private final int IMAGE_MEAN = 128;
    private final float IMAGE_STD = 128.0f;
    private final int INPUT_SIZE = 416;
    private final int NUM_OF_BBOX = 5;
    private final float THRESHOLD = 0.2f;
    private final String MODEL_NAME = "yolov2-tiny.pb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        camera = findViewById(R.id.camera);
        labels = readLabels();

        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);
        txtCurrentDetection = findViewById(R.id.txtCurrentDetection);
        switchToMaps = findViewById(R.id.btn_switch_view_to_maps);

        camera.setLifecycleOwner(this);

        intentLocation = new Intent(getBaseContext(), LocationService.class);
        bindService(intentLocation, serviceConnectionLocation, Context.BIND_AUTO_CREATE);

        listenToButton();

        camera.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {
                try {
                    FirebaseVisionImage image = getImageFromFrame(frame);
                    Bitmap bitmap = Bitmap.createScaledBitmap(image.getBitmap(), INPUT_SIZE, INPUT_SIZE, true);
                    long start = System.currentTimeMillis();
                    List<Detection> results = classifyImage(processFrame(bitmap));
                    long end = System.currentTimeMillis();
                    Log.d(TAG, (end - start) + "");
                    List<TrafficSigns> list = new ArrayList<>();
                    currentLocation = locationService.getCurrentLocation();
                    detectionText = "";
                    for (Detection r : results) {
                        list.add(new TrafficSigns(r.getId(), currentLocation.getLatitude(), currentLocation.getLongitude(), 0.0));
                        detectionText += r.getTitle() + " " + String.format("%.0f%%", r.getConfidence() * 100) + "\n";
                    }
                    if (list.isEmpty()) {
                        detectionText = "Nothing has been detected!";
                    }
                    GetTrafficSigns db = new GetTrafficSigns();
                    for (TrafficSigns s : list) {
                        db.addToDatabase(s);
                    }
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            if (currentLocation != null) {
                                txtCurrentLocation.setText(
                                        "Lat: " + String.format("%.3f", currentLocation.getLatitude()) + "," +
                                                " Lng: " + String.format("%.3f", currentLocation.getLongitude()));
                            }
                            txtCurrentDetection.setText(detectionText);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private ServiceConnection serviceConnectionLocation = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
        }
    };

    private void listenToButton() {
        switchToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
                    mapIntent.putExtra("currentLocation", new double[]{currentLocation.getLatitude(), currentLocation.getLongitude()});
                    startActivity(mapIntent);
                }
            }
        });
    }

    private float[] processFrame(Bitmap bitmap) {
        if (inferenceInterface == null) {
            inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "file:///android_asset/" + MODEL_NAME);
        }
        int outputSize = getOutputSize(inferenceInterface.graphOperation(OUTPUT_NAME));
        final float[] output = new float[outputSize];
        inferenceInterface.feed(INPUT_NAME, processBitmap(bitmap), 1, INPUT_SIZE, INPUT_SIZE, PIXEL_DEPTH);
        inferenceInterface.run(new String[]{OUTPUT_NAME});
        inferenceInterface.fetch(OUTPUT_NAME, output);
        return output;
    }

    private int getOutputSize(final Operation operation) {
        return (int) (operation.output(0).shape().size(3) * Math.pow(GRID_SIZE, 2));
    }

    private List<Detection> classifyImage(float[] tfOutput) {
        int numClass = (int) (tfOutput.length / (Math.pow(GRID_SIZE,2) * NUM_OF_BBOX) - 5);
        CellRecognition[][][] cellRecognitions = new CellRecognition[GRID_SIZE][GRID_SIZE][5];
        PriorityQueue<Detection> recognitions = new PriorityQueue<>(5, new DetectionComparator());
        int offset = 0;
        for (int y=0;y<GRID_SIZE;++y) {
            for (int x=0;x<GRID_SIZE;++x) {
                for (int b=0;b<NUM_OF_BBOX;++b) {
                    cellRecognitions[x][y][b] = getClassess(tfOutput, numClass, offset);
                    getTopDetections(cellRecognitions[x][y][b], recognitions);
                    offset = offset + numClass + 5;
                }
            }
        }
        List<Detection> detectionList = new ArrayList<>();
        while(recognitions.size() > 0) {
            Detection bestDetection = recognitions.poll();
            if (detectionList.size() < 5) {
                detectionList.add(bestDetection);
            }
        }
        return detectionList;
    }

    private void getTopDetections(final CellRecognition cell, final PriorityQueue<Detection> queue) {
        for (int i =0 ; i< cell.getClasses().length; i++) {
            Log.e(TAG, cell.getClasses().length + "");
            Pair<Integer, Double> argMax = new ArgMax(new Softmax(cell.getClasses()).getSoftmax()).getMaxIndex();
            double confidenceInClass = argMax.second * cell.getConfidence();
            if (confidenceInClass > THRESHOLD) {
                queue.add(new Detection(argMax.first, labels.get(argMax.first), (float) confidenceInClass));
            }
        }
    }

    private CellRecognition getClassess(final float[] tfOutput, int numClass, int offset) {
        CellRecognition model = new CellRecognition();
        Sigmoid sigmoid = new Sigmoid();
        model.setConfidence(sigmoid.value(tfOutput[offset + 4]));
        model.setClasses(new double[numClass]);
        for (int i = 0;i<numClass;i++) {
            model.getClasses()[i] = tfOutput[i + offset + 5];
        }
        return model;
    }

    /**
     * Based upon https://github.com/tensorflow/examples/blob/master/lite/examples/object_detection/android/app/src/main/java/org/tensorflow/lite/examples/detection/tflite/TFLiteObjectDetectionAPIModel.java
     * Line 168
     * This method noramlizes pixels in bitmap for tensorflow
     * @param bitmap - input bitmap
     * @return scaled pixels in image where mean is 0
     */
    private float[] processBitmap(final Bitmap bitmap) {
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        float[] floatValues = new float[INPUT_SIZE * INPUT_SIZE * PIXEL_DEPTH];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0,  0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xff) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xff) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 2] = ((val & 0xff) - IMAGE_MEAN) / IMAGE_STD;
        }
        return floatValues;
    }

    private List<String> readLabels() {
        List<String> labels = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("labels.txt")));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                labels.add(mLine);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error while reading labels.txt" + e);
                }
            }
        }
        return labels;
    }

    private FirebaseVisionImage getImageFromFrame(final Frame frame) {
        byte[] data = frame.getData();
        FirebaseVisionImageMetadata imageMetadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(degreesToFirebaseRotation(frame.getRotation()))
                .setWidth(frame.getSize().getWidth())
                .setHeight(frame.getSize().getHeight())
                .build();
        return FirebaseVisionImage.fromByteArray(data, imageMetadata);
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0: return FirebaseVisionImageMetadata.ROTATION_0;
            case 90: return FirebaseVisionImageMetadata.ROTATION_90;
            case 180: return FirebaseVisionImageMetadata.ROTATION_180;
            case 270: return  FirebaseVisionImageMetadata.ROTATION_270;
            default: {
                return 0;
            }
        }
    }
}
