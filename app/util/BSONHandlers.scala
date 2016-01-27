package util

import java.util.UUID

import reactivemongo.bson._

/**
  * Created by Joseph Sebastian on 27/01/2016.
  */
trait CustomBSONHandlers extends DefaultBSONHandlers {

  implicit object BSONUUIDHandler extends BSONHandler[BSONValue, UUID] {
//    override def read(bson: BSONString): UUID =
//
//    override def write(t: UUID): BSONString = BSONString(t.toString)
    override def read(bson: BSONValue): UUID = UUID.fromString(bson.toString)

    override def write(t: UUID): BSONValue = BSONString(t.toString)
  }

}
