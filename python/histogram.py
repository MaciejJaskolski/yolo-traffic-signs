# -*- coding: utf-8 -*-

import os

labels = [
	'speed_limit_30',
	'speed_limit_50',
	'speed_limit_70' ,
	'speed_limit_80' ,
	'speed_limit_100',
	'speed_limit_120' ,
	'no_overtaking',
	'no_overtaking_trucks',
	'priority_road' ,
	'give_way',
	'keep_right'
]

import pandas as pd
import matplotlib.pyplot as plt

TRAIN_DIR = './data/aug/train'
VALID_DIR = './data/aug/val'
TEST_DIR = './data/aug/test'

def getPathList(directory):
	items = []
	for item in os.listdir(directory):
		if item.find("txt")  != -1:
			items.append(os.path.join(directory, item))
	return items

train_annotations = getPathList(TRAIN_DIR)
train_data = pd.DataFrame(columns=['classId'])

for item in train_annotations:
	with open(item) as f:
		lines = [line.rstrip('\n') for line in f]
		for l in lines:
			train_data = train_data.append({'classId': l.partition(" ")[0]}, ignore_index=True)

plt.hist([train_data['classId']], bins=86)
plt.xticks(train_data['classId'], labels, rotation='vertical')
plt.xticks(range(11))
plt.title(u"Dystrybucja przykładów w wzmocnionym ciągu uczącym")
plt.show()

