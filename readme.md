### Kod aplikacji inżynierskiej "System wizyjnego rozpoznawania znaków drogowych"

# Wymagania
* git
* python 2.7
* system Linux
* kompilator C/C++
* (opcjonalnie) OpenCV
* Android Studio

# Wykorzystywane narzędzia
W celu uruchomienia aplikacji należy zainstalować lub pobrać następujące narzędzia i instalować je zgodnie z instrukcją:

* darkflow [https://github.com/thtrieu/darkflow] - wewnątrz głównego katalogu
* mAP [https://github.com/Cartucho/mAP] - wewnątrz głównego katalogu
* darknet [https://github.com/pjreddie/darknet]
* pip install -r python/requirements.txt

# Instrukcja obsługi

Należy stworzyć folder, gdzie pobrane zostaną obrazy z GTSDB. Tam należy wykonać skrypt *yoloAnnotations.py* w celu wzbogacenia ciągu, a następnie *histogram.py* w celu narysowania histogramów nowych zbiórów danych. Następnie należy użyć skryptu *getImagePath.sh* i skopiować wyniki do nowego folderu gtsdb stworzonego wewnątrz *darknet/*. Darknet należy skommpilować zgodnie z instrukcją i uruchomić skryptem *startTraining.sh*. Po skończonym treningu poleceniem *grep -E 'avg' log.log > avg.txt* wewnątrz folderu *log* zapisujemy wszystkie linijki zawierające słowo "avg" do pliku avg.txt i python'owym skryptem *plotLoss.py* rysujemy funkcję strat. Wewnątrz folderu darknet/ należy skopiować skrypt detectionsForMAP.py i go uruchomić. Wygeneruje to raport wyników wyuczonej sieci neuronwej. Następnie wybrany model należy przekonwertować z formatu .weights do .pb poleceniem *flow --model gtsdb/yolov2-tiny.cfg --load backup/yolov2-tiny.weights --savepb*. Wewnątrz Android Studio otwieramy projekt znajdujący się w repozytorium i wrzucamy plik *.pb* do folderu assets/. Gotowy program można przekompilować na wybrany model telefonu i sdk.