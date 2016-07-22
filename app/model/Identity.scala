package model

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
