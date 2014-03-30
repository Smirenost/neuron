package tutorials
import neuralnetwork._
import breeze.stats.distributions._
import breeze.linalg._

// This is an implementation of Recursive Auto-Encoder described in the following paper:
//     Semi-Supervised Recursive Autoencoders for Predicting Sentiment Distributions
object GMGTest extends Optimizable with Workspace {
  def f (x: NeuronVector, y: NeuronVector) : (Double, NeuronVector) = {
    val z = x concatenate y
    val mem = new SetOfMemorables
    val seed = ((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString
    facFork.enc.init(seed, mem).allocate(seed, mem)
    (L2Distance(facFork.enc(z, mem), z), facFork.enc.encode(z, mem))
  }
  def fsimple(x:NeuronVector, y:NeuronVector) : (Double, NeuronVector) = {
    (new Uniform(-1,1).sample(1)(0), x + y) //((x DOT y).sum, x + y)
  }
  val dim:Int = 3
  val wordLength = 10
  val chainLength= 5
  val lambda = 0.0
  val regCost= 0.1

  class NNModel (wL: Int) extends AutoEncoder(2*wL, lambda, regCost,
		  							new SingleLayerNeuralNetwork(wL) TIMES 
		  								new LinearNeuralNetwork((wL*1.414).toInt, wL) TIMES
		  									new SingleLayerNeuralNetwork((wL*1.414).toInt),
		  							new SingleLayerNeuralNetwork(2*wL)) with  RecursiveEncoder {
    val wordLength = wL
    type Instance <: InstanceOfNNModel
    override def create() : InstanceOfNNModel = new InstanceOfNNModel(this)    
  }
  class InstanceOfNNModel (override val NN: NNModel) extends InstanceOfAutoEncoder(NN) with InstanceOfRecursiveEncoder {
    val wordLength = NN.wordLength
    type StructureType <: NNModel
  }
  
  class Facility {
   val enc  = new RecursiveSimpleAE()(wordLength, lambda, regCost).create() //used for training 
   //val enc = new NNModel(wordLength).create()
   val input = (new SingleLayerNeuralNetwork(wordLength) TIMES new LinearNeuralNetwork(dim, wordLength)).create()
  }
  val fac = new Facility
  val facFork=new Facility

  
  var nnFork : InstanceOfNeuralNetwork = null
  val output = (new LinearNeuralNetwork(wordLength, 15)).create()
  
  def getDynamicNeuralNetwork(x:NeuronVector, 
		  					  ff: Facility = fac, 
		  					  rank: (NeuronVector, NeuronVector) => (Double, NeuronVector) = f): InstanceOfNeuralNetwork = {
    val tgmc = new GreedyMergeChain(rank)
    val inputs = (facFork.input PLUS facFork.input PLUS facFork.input PLUS facFork.input PLUS facFork.input).create()
    tgmc.loadChain(inputs(x,initMemory(inputs)), wordLength)
    tgmc.greedyMerge()// tgmc.nodes is set of trees
    val node = tgmc.nodes.iterator.next
    
    (output TIMES new RecursiveNeuralNetwork(node.t, ff.enc, ff.input)).create()
  }
  
   override def getObj(w: WeightVector, distance:DistanceFunction = L2Distance) : Double = { // doesnot compute gradient or backpropagation
    val size = xData.length
    assert (size >= 1 && size == yData.length)
    var totalCost: Double = 0.0
    val dw = new WeightVector(w.length)
    
    /*
    nn.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    nnFork.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    (0 until size).par.foreach(i => {
      val inn = getDynamicNeuralNetwork(xData(i))
      inn(xData(i),initMemory(inn))
    })
    */
    nn.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    nnFork.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    totalCost = (0 until size).par.map(i => {
      val inn = getDynamicNeuralNetwork(xData(i))
      distance(inn(xData(i), initMemory(inn)), yData(i))
    }).reduce(_+_)
    
    val regCost = nn.getDerativeOfWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, dw, size)
    totalCost/size + regCost
  }
     
  override def getObjAndGrad(w:WeightVector, distance:DistanceFunction = L2Distance) : (Double, NeuronVector) = {
    val size = xData.length
    assert(size >= 1 && size == yData.length)
    var totalCost:Double = 0.0
    /*
     * Compute objective and gradients in batch mode
     * which can be run in parallel 
     */
    
    val dw = new WeightVector(w.length)
    /*
    nn.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    nnFork.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    (0 until size).par.foreach(i => {
      val inn = getDynamicNeuralNetwork(xData(i))
      inn(xData(i),initMemory(inn))
    })
    */    
    nn.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    nnFork.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    totalCost = (0 until size).par.map(i => {
      val inn = getDynamicNeuralNetwork(xData(i))
      val mem = initMemory(inn)
      
      val x = inn(xData(i), mem); val y = yData(i)
      val z = distance.grad(x, yData(i))
      inn.backpropagate(z, mem) // update dw !
      distance(x,y)
    }).reduce(_+_)
    /*
     * End parallel loop
     */
    
    val regCost = nn.getDerativeOfWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, dw, size)
    (totalCost/size + regCost, dw/size)    
  }
  
   override def test(w:WeightVector, distance: DistanceFunction = L2Distance): Double = {
    val size = xDataTest.length
    nn.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    nnFork.setWeights(((randomGenerator.nextInt()*System.currentTimeMillis())%100000).toString, w)
    val totalCost = (0 until size).par.map(i => {
      val inn = getDynamicNeuralNetwork(xDataTest(i))
      distance(inn(xDataTest(i), initMemory(inn)), yDataTest(i))
    }).reduce(_+_)
    totalCost / size
  }
  
  
  def main(args: Array[String]): Unit = {
    
    val dataSource = scala.io.Source.fromFile("data/colorpalette/kulerData.txt").getLines.toArray
    val labelSource= scala.io.Source.fromFile("data/colorpalette/kulerData-s.txt").getLines.toArray
    val numOfSamples = dataSource.length
    val trainSize : Int = (0.6 * numOfSamples).toInt
    
    val shuffledList = scala.util.Random.shuffle((0 until numOfSamples).toList)

	
    val xDataM = new Array[NeuronVector](numOfSamples)
    val yDataM = new Array[NeuronVector](numOfSamples)

    for (i<- 0 until numOfSamples) yield {
      xDataM(i) = new NeuronVector(
	        new DenseVector(dataSource(shuffledList(i)).split("\\s+").map(_.toDouble), 0, 1, dim*chainLength))
      yDataM(i) = new NeuronVector(
	        new DenseVector(labelSource(shuffledList(i)).split("\\s+").map(_.toDouble), 0, 1, 1))
    }   
    
    xData = xDataM slice(0, trainSize)
    yData = xData //yDataM slice(0, trainSize)
    
    xDataTest = xDataM slice(trainSize, numOfSamples)
    yDataTest = xDataTest //yDataM slice(trainSize, numOfSamples)
    
    
    nn = getDynamicNeuralNetwork(xData(0), fac, fsimple) // default neural network
    nnFork=getDynamicNeuralNetwork(xData(0), facFork, fsimple)
    
    val w = getRandomWeightVector()
    
	  var time: Long = 0
	  
	  //val obj = getObj(w); println(obj)
	  /*
	  time = System.currentTimeMillis();
	  val (obj, grad) = getObjAndGrad(w)
	  println(System.currentTimeMillis() - time, obj, grad.data)
	  */
	  // gradient checking
	  /*
	  time = System.currentTimeMillis();
	  val (obj2, grad2) = getApproximateObjAndGrad(w)
	  println(System.currentTimeMillis() - time, obj2, grad2.data)
	  //println((grad2 - grad).euclideanSqrNorm)
	  */
	  	  
	  time = System.currentTimeMillis();
	  val (obj3, w2) = train(w)
	  println(System.currentTimeMillis() - time, obj3, w2.data)
	  
	  time = System.currentTimeMillis();
	  val obj4 = test(w2) // >.< change w to w2
	  println(System.currentTimeMillis() - time, obj4)
	  
  }

}