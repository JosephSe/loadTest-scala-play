package util

import java.util.UUID

import reactivemongo.bson._

/**
  * Created by Joseph Sebastian on 27/01/2016.
  */

package object CustomBSONHandlers {
  implicit object BSONUUIDHandler extends BSONHandler[BSONString, UUID] {
    override def read(bson: BSONString): UUID = UUID.fromString(bson.toString)

    override def write(t: UUID): BSONString = BSONString(t.toString)

  }
}