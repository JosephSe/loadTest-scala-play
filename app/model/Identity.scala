package model

import java.util.UUID

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
trait Identity[E, ID] {

  def name: String

  def of(entity: E): Option[ID]

  def set(entity: E, id: ID): E

  def clear(entity: E): E

  def next: ID
}

/*
trait MongoIdentity[E <: MongoEntity] extends Identity[E, UUID] {
  this: MongoEntity =>
  override def next: UUID = UUID.randomUUID()

  override def of(entity: E): Option[UUID] = entity.uuid

//  override def set(entity: E, id: UUID): XmlFile = entity.copy(uuid = Option(id))

}*/
