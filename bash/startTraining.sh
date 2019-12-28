./darknet partial gtsdb/yolov2-tiny.cfg yolov2-tiny.weights yolov2-tiny.conv.13 13
./darknet detector train gtsdb/gtsdb.data gtsdb/yolov2-tiny.cfg yolov2-tiny.conv.13.13 > logs/log.log
