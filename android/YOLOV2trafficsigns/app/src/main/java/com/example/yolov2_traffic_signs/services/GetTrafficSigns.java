package com.example.yolov2_traffic_signs.services;

import androidx.annotation.NonNull;

import com.example.yolov2_traffic_signs.models.TrafficSigns;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GetTrafficSigns implements Continuation<Void, Task<List<TrafficSigns>>> {

    private static final String TAG = GetTrafficSigns.class.getSimpleName();
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    public void addToDatabase(TrafficSigns trafficSigns) {
//        db.child("signs").child("" + System.currentTimeMillis()).setValue(trafficSigns);
    }

    @Override
    public Task<List<TrafficSigns>> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<List<TrafficSigns>> tcs = new TaskCompletionSource();
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TrafficSigns> list = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for(DataSnapshot child: dataSnapshot.child("signs").getChildren()) {
                        TrafficSigns newSign = child.getValue(TrafficSigns.class);
                        list.add(newSign);
                    }
                }

                tcs.setResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return tcs.getTask();
    }
}
