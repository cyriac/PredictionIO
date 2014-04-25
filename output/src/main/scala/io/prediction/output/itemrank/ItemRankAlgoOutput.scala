package io.prediction.output.itemrank

import io.prediction.commons.Config
import io.prediction.commons.modeldata.ItemRecScore
import io.prediction.commons.settings.{ Algo, App, Engine, OfflineEval }

import scala.util.Random

import com.github.nscala_time.time.Imports._

trait ItemRankAlgoOutput {
  /** output the Seq of iids */
  def output(uid: String, iids: Seq[String]): Iterator[String]
}

object ItemRankAlgoOutput {
  val config = new Config

  def output(uid: String, iids: Seq[String])(implicit app: App, engine: Engine, algo: Algo, offlineEval: Option[OfflineEval] = None): Seq[String] = {
    val itemRecScores = offlineEval map { _ => config.getModeldataTrainingItemRecScores } getOrElse config.getModeldataItemRecScores
    val items = offlineEval map { _ => config.getAppdataTrainingItems } getOrElse { config.getAppdataItems }

    /** n should be the number of items. */
    val n = iids.size

    val allItemsForUid = itemRecScores.getByUid(uid)

    /**
     * Rank based on scores. If no recommendations found for this user,
     * simply return the original list.
     */
    val iidsSet = iids.toSet
    allItemsForUid map { allItems =>
      val iidScorePairs = allItems.iids.zip(allItems.scores)
      val ranked = iidScorePairs.filter(p => iidsSet(p._1)).sortBy(_._2).reverse.map(_._1)
      val rankedSet = ranked.toSet
      val unranked = iids.filterNot(x => rankedSet(x))
      ranked ++ unranked
    } getOrElse iids
  }
}