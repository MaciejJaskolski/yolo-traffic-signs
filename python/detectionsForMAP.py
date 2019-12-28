from __future__ import division
import os
import sys
import pandas as pd

labelFile = open('gtsdb/gtsdb.label')
labels = [line for line in labelFile]
labelFile.close()
with open("results.txt") as f:
	lines = [line.rstrip('\n') for line in f]
	for i in range(0, len(lines)):
		if lines[i].find("Enter Image") != -1:
			idxOfLastSlash = lines[i].rfind('/')
			idxOfDot = lines[i].rfind('.')
			newFile = open("detection_results/" + lines[i][lines[i].rfind('/')+1:lines[i].find('.')] + '.txt', "w+")
			if i + 1 < len(lines) and lines[i + 1].find("Enter Image") != -1:
				continue
			for j in range(i + 1, len(lines)):
				className = ""
				location = ""
				confidence = ""
				if lines[j + 1].find("Enter Image") != -1:
					i = j + 1
					break
				if lines[j].find(":") != -1:
					className = lines[j].split(":")[0] + " "
					confidence = str(int(lines[j].split(":")[1][:2]) / 10) + " "
				if j + 1 < len(lines) and lines[j].find(":") != -1:
					location = lines[j + 1] + "\n"
				newFile.write(className + confidence + location)
			newFile.close()
