neuron [![Build Status](https://travis-ci.org/bobye/neuron.svg?branch=master)](https://travis-ci.org/bobye/neuron)
========

This project is _in progress_, which aims at a generic framework to experiment heterogeneous neural network structures. No release available for now.

Creator: [Jianbo Ye](http://www.personal.psu.edu/jxy198)

### How to build

```
$ sbt
sbt> compile
sbt> eclipse
```
Start _Scala IDE for Eclipse_ and import the project directory

### Features
 - template vs. module
 - algebra (PLUS, TIMES, SHARE, TENSOR) for network structures
 - multilayer perceptron
 - autoencoders (w or w/o tiled weight)
 - recursive neural network
 - metrics (L1/L2, Softmax)
 - parallel framework: atomic parameters + distributed states
 - optimization: LBFGS, SGD, SAGD, SGD with momentum
 
### Get Started
TBA

### Examples

run [UFLDL Sparse AutoEncoder exercise](http://ufldl.stanford.edu/wiki/index.php/Exercise:Sparse_Autoencoder) in parallel. It should take about 12 seconds on my PC:) 

	sbt 'run-main neuron.tutorials.ImageAutoEncoderTest'
	cd data/UFLDL/sparseae/
	matlab -r "display_sparseae.m"

 ![UFLDL SAE](https://raw.githubusercontent.com/bobye/neuron/master/data/UFLDL/sparseae/results25.png)

### Reference
* [Breeze](https://github.com/dlwh/breeze/): a set of libraries for machine learning and numerical computing
* [UFLDL Tutorial](http://ufldl.stanford.edu/wiki/index.php/UFLDL_Tutorial): a Stanford course, find solutions at [Github](https://github.com/search?q=UFLDL+Tutorial)
* [DeepLearnToolbox](https://github.com/rasmusbergpalm/DeepLearnToolbox): Matlab toolbox for deep learning
* [ScalaSTM](http://nbronson.github.io/scala-stm/):  a lightweight software transactional memory for Scala 

----
The MIT License (MIT)

Copyright (c) 2014 Jianbo Ye

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
