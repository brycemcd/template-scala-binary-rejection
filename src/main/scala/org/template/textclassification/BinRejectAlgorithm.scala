package org.template.textclassification

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.linalg.Vector
import com.github.fommil.netlib.F2jBLAS
import grizzled.slf4j.Logger

import scala.math._

// FIXME: not used, but Evaluation fails without it
case class  BinRejectAlgoParams(
  lambda: Double
) extends Params

class BinRejectAlgorithm extends P2LAlgorithm[PreparedData, BinRejectModel, Query, PredictedResult] {
  @transient lazy val logger = Logger[this.type]
  // Train your model.
  def train(sc: SparkContext, pd: PreparedData): BinRejectModel = {
    new BinRejectModel(pd)
  }

  // Prediction method for trained model.
  def predict(model: BinRejectModel, query: Query): PredictedResult = {
    model.predict(model, query.text)
  }
}

// NOTE: model is just the prepared data
class BinRejectModel(val pd: PreparedData) extends Serializable {
  @transient lazy val logger = Logger[this.type]

  private def alreadyDuplicated(model: BinRejectModel, query: String) : Boolean = {
    val priorDecisions = model.pd.td
    val matches = priorDecisions.filter(d => d.text == query)

    val shouldReject = if(matches.length > 0 && matches(0).rejectScore > 0) true else false
    logger.info(s"checking alreadyDuplicated: $shouldReject")
    shouldReject
  }

  private val rejectWords : Array[String] = Array("unsubscribe")

  private def containsRejectWord(query : String) : Boolean = {
    val words = query.split(" ")
    val matchedIndex : Int = words.map(word => rejectWords.indexOf(word.toLowerCase)).max

    val shouldReject : Boolean = matchedIndex > -1
    logger.info(s"checking containsRejectWord: $shouldReject")
    shouldReject
  }

  // TODO: add Read more stories on teh Quibb homepage
  // TODO: add Support Quibb with a paid ...
  private val rejectPhrases : Array[String] = Array("A LERER HIPPEAU VENTURES EXPERIMENT")

  private def containsRejectPhrase(query: String) : Boolean = {
    val shouldReject : Boolean =rejectPhrases.indexOf(query.trim) > -1

    logger.info(s"checking containsRejectPhrase: $shouldReject")
    shouldReject
  }

  private def wordCountThreshold(query : String, threshold : Integer = 4) = {
    val shouldReject = query.split(" ").length <= threshold

    logger.info(s"checking wordCountThreshold: $shouldReject")
    shouldReject
  }

  private def shouldReject(query: String) : Boolean = {
    containsRejectWord(query) ||
      containsRejectPhrase(query) ||
      wordCountThreshold(query)
  }

  // TODO: if I've rejected a query before, reject it again
  // TODO: output logger.info when processing a query
  def predict(model: BinRejectModel, query : String) : PredictedResult = {
    logger.info()

    val str : String = shouldReject(query) || alreadyDuplicated(model, query)  match {
      case true => "reject"
      case false => "no-reject"
    }

    logger.info(query + " returns " + str)
    logger.info()

    new PredictedResult(str, 1.0)
  }
}
