# $1 - path to weights file
./darknet detector test gtsdb/gtsdb.data gtsdb/yolov2-tiny.cfg $1 < gtsdb/test.list > results.txt
echo "Removing previous detections..."
rm ./detection_results/*.*
rm ~/mAP/input/detection-results/*
echo "Creating new annotations for mAP test..."
python detectionsForMAP.py
echo "Copying..."
cp ./detection_results/*.txt ~/mAP/input/detection-results
rm ~/mAP/input/detection-results/*ath*
echo "Run mAP test"
python ~/mAP/main.py
