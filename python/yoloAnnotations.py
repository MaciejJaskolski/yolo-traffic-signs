# -*- coding: utf-8 -*-

from __future__ import division, unicode_literals
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from PIL import Image
import os
from tqdm import tqdm

from sklearn.model_selection import train_test_split

labels = [
	'speed_limit_20',
	'speed_limit_50', 
	'speed_limit_60' ,
	'speed_limit_70' ,
	'speed_limit_80' ,
	'restriction_ends_80', 
	'speed_limit_100',
	'speed_limit_120' ,
	'no_overtaking',
	'no_overtaking_trucks',
	'priority_at_next_intersection',
	'priority_road' ,
	'give_way',
	'stop',
	'no_traffic_both_ways', 
	'no_trucks',
	'no entry',
	'danger' ,
	'bend_left', 
	'bend_right', 
	'bend' ,
	'uneven_road', 
	'slippery_road', 
	'road_narrows' ,
	'construction' ,
	'traffic_signal', 
	'pedestrian_crossing', 
	'school_crossing',
	'cycles_crossing' ,
	'snow',
	'animals', 
	'restriction_ends', 
	'go_right' ,
	'go_left' ,
	'go_straight',
	'go_right_or_straight',
	'go_left_or_straight',
	'keep_right',
	'keep_left',
	'roundabout',
	'restriction_ends_overtaking',
	'restriction_ends_overtaking_trucks'
]

data = pd.read_csv('./data/FullIJCNN2013/gt.csv', sep=';', header=None)
data.columns = ['filename', 'x1', 'y1', 'x2', 'y2', 'classId']

print(data.groupby(["classId"]).count().max(level=0))

def drawSetHist(dataset, label='label', title='title'):
	plt.hist([dataset['classId']], label=[label], bins=86)
	plt.title(title)
	plt.xlabel("Numer klasy")
	plt.xticks(dataset['classId'], labels, rotation='vertical')
	plt.tick_params(axis="x", labelsize=7)
	plt.ylabel("Liczba wystąpień")
	plt.xticks(range(43))
	plt.show()


#drawSetHist(data, 'gtsdb', 'Dystrybucja przykładów znaków na klasę w GTSDB')

data = data.groupby('classId').filter(lambda x : len(x)>39)

xtrain, rest = train_test_split(data, test_size=0.3)
xval, xtest = train_test_split(rest, test_size=0.5)

drawSetHist(xtrain, 'test', 'Dystrybucja liczby znaków w klasie w ciągu uczącym')
drawSetHist(xval, 'validation', 'Dystrybucja liczby znaków w ciągu walidującym')
drawSetHist(xtest, 'test', 'Dystrybucja liczby znaków w ciągu testowym')

#print(xtrain.groupby(["classId"]).count().max(level=0))
#print(xval.groupby(["classId"]).count().max(level=0))

TRAIN_DIR = './data/aug/train'
VALID_DIR = './data/aug/val'
TEST_DIR = './data/aug/test'
PPM_DIR = './data/FullIJCNN2013'

def createAnnotationFile(filename, w, h, x1, y1, x2, y2, classId, directory):
	annotationFile = open(os.path.join(directory, filename.replace('.ppm', '.txt')), 'w+')
	c = labels[classId] if directory == TEST_DIR else str(classId)
	centerX = str(((int(x2) + int(x1)) / 2) / int(w)) + ' '
	centerY = str(((int(y2) + int(y1)) / 2) / int(h)) + ' '
	width = str( (int(x2) - int(x1)) / int(w)) + ' '
	height = str( (int(y2) - int(y1)) / int(h)) + ' '
	annotationFile.write(c + ' ' + centerX + centerY + width + height + '\n')
	annotationFile.close()

def appendToAnnotationFile(filename, w, h, x1, y1, x2, y2, classId, directory):
	annotationFile = open(os.path.join(directory, filename.replace('.ppm', '.txt')), 'a+')
	c = labels[classId] if directory == TEST_DIR else str(classId)
	centerX = str(((int(x2) + int(x1)) / 2) / int(w)) + ' '
	centerY = str(((int(y2) + int(y1)) / 2) / int(h)) + ' '
	width = str( (int(x2) - int(x1)) / int(w)) + ' '
	height = str( (int(y2) - int(y1)) / int(h)) + ' '
	annotationFile.write(c + ' ' + centerX + centerY + width + height + '\n')
	annotationFile.close()	

def saveAnnotations(dataset, directory, desc="Saving images and annotations"):
	previousFilename = ''
	for idx, row in tqdm(dataset.iterrows(), desc=desc):	
		if row['filename'] != previousFilename:
			image = Image.open(os.path.join(PPM_DIR, row['filename']))
			createAnnotationFile(row['filename'], image.size[0], image.size[1], row['x1'], row['y1'], row['x2'], row['y2'], row['classId'], directory)
			previousFilename = row['filename']
			image.save(os.path.join(directory, row['filename'].replace('.ppm', '.png')))
		else:
			image = Image.open(os.path.join(PPM_DIR, row['filename']))
			appendToAnnotationFile(row['filename'], image.size[0], image.size[1], row['x1'], row['y1'], row['x2'], row['y2'], row['classId'], directory)
			image.save(os.path.join(directory, row['filename'].replace('.ppm', '.png')))

import numpy as np
import imgaug as ia
import imgaug.augmenters as iaa
from imgaug.augmentables.bbs import BoundingBox, BoundingBoxesOnImage
from matplotlib.image import imread
import random
from tqdm import tqdm

seq = iaa.Sequential([
    iaa.Affine(translate_px={"x": (-40, 40), "y": (-40, 40)}, rotate=(-2, 2), shear=(-10, 10)),
    iaa.GammaContrast(gamma=random.uniform(0.7, 1.3)),
	iaa.AddToHueAndSaturation((-15, 15))
])

xtrain = xtrain.groupby(['filename'])
for name, group in tqdm(xtrain, desc="create bboxes"):
	path = os.path.join(PPM_DIR, name)
	image = imread(path)
	bbs_per_img = []
	for idx, item in group.iterrows():
		bbox = BoundingBox(x1=item['x1'], y1=item['y1'], x2=item['x2'], y2=item['y2'], label=item['classId'])
		bbs_per_img.append(bbox)
	bbs = BoundingBoxesOnImage(bbs_per_img, shape=image.shape)
	for idx in tqdm(range(0, random.randrange(30, 50)), desc=name):
		image_aug, bbs_aug = seq(images=[image], bounding_boxes=bbs)
		im = Image.fromarray(image_aug[0])
		im.save(os.path.join(TRAIN_DIR, str(idx) + '_' + name.replace('ppm', 'png')))
		labelFile = open(os.path.join(TRAIN_DIR, str(idx) + '_' + name.replace('.ppm', '.txt')), 'w+')
		for b in bbs_aug.bounding_boxes:
			labelFile.write(str(b.label) + ' ' +
				str((b.x2 + b.x1)/2/1360) + ' ' +
				str((b.y2 + b.y1)/2/800) + ' ' + 
				str((b.x2 - b.x1)/1360) + ' ' +
				str((b.y2 - b.y1)/800) + '\n')
		labelFile.close()






#xtrain = xtrain.sort_values(by=['filename'])
xval = xval.sort_values(by=['filename'])
xtest = xtest.sort_values(by=['filename'])
#saveAnnotations(xtrain, TRAIN_DIR, 'Saving train images and its annotations')
saveAnnotations(xval, VALID_DIR, 'Saving valid images and its annotations')
saveAnnotations(xtest, TEST_DIR, 'Saving test images and its annotations')

file = open('gtsdb.label', 'w+')
for label in labels:
	file.write(label + '\n')
file.close()
