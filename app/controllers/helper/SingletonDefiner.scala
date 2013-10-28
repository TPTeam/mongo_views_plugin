package controllers

trait SingletonDefiner[C <: models.ModelObj] {
	
	val singleton: models.persistance.PersistanceCompanion[C]
	
	lazy val obj = singleton

}