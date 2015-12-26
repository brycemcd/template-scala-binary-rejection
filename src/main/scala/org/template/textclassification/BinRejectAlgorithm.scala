package org.template.textclassification

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.linalg.Vector
import com.github.fommil.netlib.F2jBLAS
import grizzled.slf4j.Logger
import java.util.Calendar

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

  // TODO: filter non-alpha nums from getting into the db in the first place
  private def filterForAlphaNums(string: String) : String = string.replaceAll("[^a-zA-Z0-9\\s]", "")

  private def alreadyDuplicated(model: BinRejectModel, query: String) : Boolean = {
    val priorDecisions = model.pd.td
    val matches = priorDecisions.filter(d => filterForAlphaNums(d.text) == filterForAlphaNums(query))

    val shouldReject = if(matches.length > 0 && matches(0).rejectScore > 0) true else false
    logger.info(s"checking alreadyDuplicated: $shouldReject")
    shouldReject
  }

  private val rejectWords : Array[String] = Array("unsubscribe")

  private def containsRejectWord(query : String) : Boolean = {
    val words = query.split(" ")
    val matchedIndex : Int = words.map(word => rejectWords.indexOf(word.toLowerCase)).max

    matchedIndex > -1
  }

  // TODO: add Read more stories on teh Quibb homepage
  // TODO: add Support Quibb with a paid ...
  private val rejectPhrases : Array[String] = Array("A LERER HIPPEAU VENTURES EXPERIMENT")

  private def containsRejectPhrase(query: String) : Boolean = {
    rejectPhrases.indexOf(query.trim) > -1
  }

  private def wordCountThreshold(query : String) = {
    query.split(" ").length <= 3
  }

  private def shouldRejectBoolFunctions(query: String, rejectFunctions: String => Boolean*) : Boolean = {
    rejectFunctions.map{ rejectFunction =>

      val shouldReject = rejectFunction(query)

      logger.info("[" +
                  Calendar.getInstance.getTime.toString +
                  "]" +
                  " checking " +
                  rejectFunction.toString +
                  ": " +
                  shouldReject)
      shouldReject
    }.indexOf(true) > -1
  }

  def predict(model: BinRejectModel, query : String) : PredictedResult = {
    logger.info()

    val boolRejections : Boolean = shouldRejectBoolFunctions(query,
                                                              containsRejectWord,
                                                              containsRejectPhrase,
                                                              wordCountThreshold)

    val str : String = boolRejections || alreadyDuplicated(model, query)  match {
      case true => "reject"
      case false => "no-reject"
    }

    logger.info(query + " returns " + str)
    logger.info()

    new PredictedResult(str, 1.0)
  }
}
