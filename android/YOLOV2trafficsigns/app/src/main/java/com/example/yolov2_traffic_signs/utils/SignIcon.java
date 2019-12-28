package com.example.yolov2_traffic_signs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.yolov2_traffic_signs.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * This util class takes a classId and returns a scaled resource file to be drawn in MapsActivity
 */
public class SignIcon {

    private final int size = 70;

    public BitmapDescriptor getIcon(Context context, int classId) {
        int resource = -1;
        switch (classId) {
            case 0: { resource = R.drawable.ic_0; break;}
            case 1: { resource = R.drawable.ic_1; break;}
            case 2: { resource = R.drawable.ic_2;break;}
            case 3: { resource = R.drawable.ic_3;break;}
            case 4: { resource = R.drawable.ic_4;break;}
            case 5: { resource = R.drawable.ic_5;break;}
            case 6: { resource = R.drawable.ic_6;break;}
            case 7: { resource = R.drawable.ic_7;break;}
            case 8: { resource = R.drawable.ic_8;break;}
            case 9: { resource = R.drawable.ic_9;break;}
            case 10: { resource = R.drawable.ic_10;break;}
            case 11: { resource = R.drawable.ic_11;break;}
            case 12: { resource = R.drawable.ic_12;break;}
            case 13: { resource = R.drawable.ic_13;break;}
            case 14: { resource = R.drawable.ic_14;break;}
            case 15: { resource = R.drawable.ic_15;break;}
            case 16: { resource = R.drawable.ic_16;break;}
            case 17: { resource = R.drawable.ic_17;break;}
            case 18: { resource = R.drawable.ic_18;break;}
            case 19: { resource = R.drawable.ic_19;break;}
            case 20: { resource = R.drawable.ic_20;break;}
            case 21: { resource = R.drawable.ic_21;break;}
            case 22: { resource = R.drawable.ic_22;break;}
            case 23: { resource = R.drawable.ic_23;break;}
            case 24: { resource = R.drawable.ic_24;break;}
            case 25: { resource = R.drawable.ic_25;break;}
            case 26: { resource = R.drawable.ic_26;break;}
            case 27: { resource = R.drawable.ic_27;break;}
            case 28: { resource = R.drawable.ic_28;break;}
            case 29: { resource = R.drawable.ic_29;break;}
            case 30: { resource = R.drawable.ic_30;break;}
            case 31: { resource = R.drawable.ic_31;break;}
            case 32: { resource = R.drawable.ic_32;break;}
            case 33: { resource = R.drawable.ic_33; break;}
            case 34: { resource = R.drawable.ic_34; break;}
            case 35: { resource = R.drawable.ic_35; break;}
            case 36: { resource = R.drawable.ic_36; break;}
            case 37: { resource = R.drawable.ic_37; break;}
            case 38: { resource = R.drawable.ic_38; break;}
            case 39: { resource = R.drawable.ic_39; break;}
            case 40: { resource = R.drawable.ic_40; break;}
            case 41: { resource = R.drawable.ic_41; break;}
            case 42: {resource = R.drawable.ic_42; break;}
        }
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), resource);
        Bitmap bitmap = Bitmap.createScaledBitmap(icon, size, size, false);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
