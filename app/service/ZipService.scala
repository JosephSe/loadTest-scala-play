package service

import java.util.UUID

import dao.CRUDService
import model.ZipFile

/**
  * Created by Joseph Sebastian on 21/01/2016.
  */
trait ZipService extends CRUDService[ZipFile, UUID]

class ZipMongoService {

}
