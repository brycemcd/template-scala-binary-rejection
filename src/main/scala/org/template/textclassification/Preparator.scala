package org.template.textclassification


import io.prediction.controller.PPreparator
import io.prediction.controller.Params
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{IDF, IDFModel, HashingTF}
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import scala.collection.Map
import scala.collection.immutable.HashMap
import scala.collection.JavaConversions._
import scala.math._


// 1. Initialize Preparator parameters. Recall that for our data
// representation we are only required to input the n-gram window
// components.

case class PreparatorParams(
) extends Params

case class VectorAndTextExample(
                        vector: SparseVector,
                        text : String
                        ) extends Serializable

case class LabeledPointAndTextExample(
                                 point: LabeledPoint,
                                 text : String
                                 ) extends Serializable


// 2. Initialize your Preparator class.

class Preparator(pp: PreparatorParams) extends PPreparator[TrainingData, PreparedData] {

  // Prepare your training data.
  def prepare(sc : SparkContext, td: TrainingData): PreparedData = {
    //new PreparedData(td, pp.nGram, pp.numFeatures, pp.SPPMI, sc)
    new PreparedData(td, sc)
  }
}

//------PreparedData------------------------

// NOTE: no preparation, this is just a simple rule set that rejects based on
// some blunt criteria
class PreparedData(
  val td: TrainingData,
  @transient val sc: SparkContext
) extends Serializable { }
