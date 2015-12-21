package org.template.textclassification

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.linalg.Vector
import com.github.fommil.netlib.F2jBLAS

import scala.math._

// 1. Define parameters for Supervised Learning Model. We are
// using a Naive Bayes classifier, which gives us only one
// hyperparameter in this stage.

case class  NBAlgorithmParams(
  lambda: Double
) extends Params



// 2. Define SupervisedAlgorithm class.

class NBAlgorithm(
  val sap: NBAlgorithmParams
) extends P2LAlgorithm[PreparedData, NBModel, Query, PredictedResult] {

  // Train your model.
  def train(sc: SparkContext, pd: PreparedData): NBModel = {
    new NBModel(pd, sap.lambda)
  }

  // Prediction method for trained model.
  def predict(model: NBModel, query: Query): PredictedResult = {
    model.predict(query.text)
  }
}

class NBModel(
val pd: PreparedData,
lambda: Double
) extends Serializable {


  private val rejectWords : Array[String] = Array("unsubscribe")

  private def containsRejectWord(query : String) : Boolean = {
    val words = query.split(" ")
    val matchedIndex : Int = words.map(word => rejectWords.indexOf(word.toLowerCase)).max
    matchedIndex > -1
  }

  private val rejectPhrases : Array[String] = Array("A LERER HIPPEAU VENTURES EXPERIMENT")

  private def containsRejectPhrase(query: String) : Boolean = {
    rejectPhrases.indexOf(query.trim) > -1
  }

  private def wordCountThreshold(query : String, threshold : Integer = 4) = {
    query.split(" ").length <= threshold
  }

  private def shouldReject(query: String) : Boolean = {
    containsRejectWord(query) ||
      containsRejectPhrase(query) ||
      wordCountThreshold(query)
  }

  def predict(query : String) : PredictedResult = {
    val str : String = shouldReject(query) match {
      case true => "reject"
      case false => "no-reject"
    }

    new PredictedResult(str, 1.0)
  }
}
