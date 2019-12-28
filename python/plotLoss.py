# -*- coding: utf-8 -*-

import matplotlib.pyplot as plt
import numpy
import pandas as pd

#data = pd.read_csv('./avg.txt', sep=' ', header=None)
#data.columns = ['num', 'valLoss', 'trainLoss', 'avg', 'valRate', 'rate', 'valSec', 'sec', 'valImages', 'images']

#data[['trainLoss', 'valLoss']].plot.line()
#print(data['valLoss'][0])
#data.plot(y=['trainLoss'])
#plt.show()

trainLoss = []
validLoss = []
file = open('avg.txt', 'r')
if file.mode == 'r':
	for idx, line in enumerate(file):
		if idx % 100 == 0:
			for idx, word in enumerate(line.split()):
		   		if idx == 1:
					val = word.replace(',', '')
					f = float(val)
					validLoss.append(f)
				elif idx == 2:
					val = word.replace(',', '')
					f = float(val)
					trainLoss.append(f)

print(len(trainLoss))
print(len(validLoss))

print(trainLoss[0])
print(validLoss[0])

plt.plot(trainLoss, color='black')
plt.title(u'Całkowita funkcja strat w uczeniu')
plt.axvline(x=133, label=u'wartość w 13 000')
plt.xlabel(u"1 na wykresie = 100 kroków")
plt.ylabel(u"Wartość całkowitej funkcji strat")
plt.show()

plt.plot(validLoss, color='orange')
plt.title(u'Średnia funkcja strat w uczeniu')
plt.axvline(x=133, label=u'wartość w 13 000')
plt.xlabel(u"1 na wykresie = 100 kroków")
plt.ylabel(u"Wartość średniej funkcji strat")
plt.show()
