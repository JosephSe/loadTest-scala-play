package dao

import javax.inject.{Inject, Named}

import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import com.sksamuel.elastic4s.{ElasticDsl, IndexAndType}
import model.ESRecord

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Joseph Sebastian on 27/09/2016.
  */
class ESProdDAO @Inject()(cs: ClusterSetup, elasticFactory: PlayElasticFactory, @Named("prod") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport {

  private[this] lazy val client = elasticFactory(cs)

  def getById()(implicit ec: ExecutionContext): Future[Option[ESRecord]] = client execute {
    get id "" from indexAndType
  } map (_.as[ESRecord])

}
