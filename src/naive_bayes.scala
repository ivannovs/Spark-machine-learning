import org.apache.spark.SparkConf
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession

import scala.util.Random

/**
 * Author : lihuichuan
 * Time   : 2020/2/9
 **/
object naive_bayes extends App {

  val conf = new SparkConf().setMaster("local").setAppName("iris")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  spark.sparkContext.setLogLevel("WARN") ///日志级别

  val file = spark.read.format("csv").load("/Users/hui/Desktop/Hadoop/spark-machine-learning/Spark-machine-learning/data/iris.data")

  import spark.implicits._

  val random = new Random()
  val data = file.map(row => {
    val label = row.getString(4) match {
      case "Iris-setosa" => 0
      case "Iris-versicolor" => 1
      case "Iris-virginica" => 2
    }

    (row.getString(0).toDouble,
      row.getString(1).toDouble,
      row.getString(2).toDouble,
      row.getString(3).toDouble,
      label,
      random.nextDouble())
  }).toDF("_c0", "_c1", "_c2", "_c3", "label", "rand").sort("rand")

  //  data.show()
  val assembler = new VectorAssembler().setInputCols(Array("_c0", "_c1", "_c2", "_c3")).setOutputCol("features")

  val dataset = assembler.transform(data)
  val Array(train, test) = dataset.randomSplit(Array(0.8, 0.2))


  //bayes
  val bayes = new NaiveBayes().setFeaturesCol("features").setLabelCol("label")
  val model = bayes.fit(train) //训练数据集进行训练
  val result = model.transform(test) //测试数据集进行测试，看看效果如何
  val evaluator = new MulticlassClassificationEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("accuracy")
  val accuracy = evaluator.evaluate(result)
  println(s"""accuracy is $accuracy""")

}
