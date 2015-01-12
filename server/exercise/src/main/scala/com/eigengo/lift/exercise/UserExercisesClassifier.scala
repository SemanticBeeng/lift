package com.eigengo.lift.exercise

import akka.actor.{Props, Actor}
import com.eigengo.lift.exercise.UserExerciseClassifier._
import UserExercises._

import scala.util.Random

/**
 * Companion object for the classifier
 */
object UserExerciseClassifier {
  val props: Props = Props[UserExerciseClassifier]

  /**
   * Muscle group information
   *
   * @param key the key
   * @param title the title
   * @param exercises the suggested exercises
   */
  case class MuscleGroup(key: String, title: String, exercises: List[String])

  val supportedMuscleGroups = List(
    MuscleGroup(key = "legs",  title = "Legs",  exercises = List("squat", "extension", "curl")),
    MuscleGroup(key = "core",  title = "Core",  exercises = List("crunch", "side bend", "cable crunch")),
    MuscleGroup(key = "back",  title = "Back",  exercises = List("pull up", "row", "deadlift", "fly")),
    MuscleGroup(key = "arms",  title = "Arms",  exercises = List("biceps curl", "triceps press down")),
    MuscleGroup(key = "chest", title = "Chest", exercises = List("chest press", "butterfly", "cable cross-over"))
  )


  /**
   * Model version and other metadata
   * @param version the model version
   */
  case class ModelMetadata(version: Int)

  /**
   * The MD companion
   */
  object ModelMetadata {
    /** Special user-classified metadata */
    val user = ModelMetadata(-1231344)
  }

  /**
   * ADT holding the classification result
   */
  sealed trait ClassifiedExercise

  /**
   * Known exercise with the given confidence, name and optional intensity
   * @param metadata the model metadata
   * @param confidence the confidence
   * @param exercise the exercise
   */
  case class FullyClassifiedExercise(metadata: ModelMetadata, confidence: Double, exercise: Exercise) extends ClassifiedExercise

  /**
    * Unknown exercise
   * @param metadata the model
   */
  case class UnclassifiedExercise(metadata: ModelMetadata) extends ClassifiedExercise

  /**
    * No exercise: ideally, a rest between sets, or just plain old not working out
   * @param metadata the model
   */
  case class NoExercise(metadata: ModelMetadata) extends ClassifiedExercise

  /**
   * The user has tapped the input device
   */
  case object Tap extends ClassifiedExercise
}

/**
 * Match the received exercise data using the given model
 */
class UserExerciseClassifier extends Actor {
  val exercises =
    Map(
      "arms" → List("Biceps curl", "Triceps press"),
      "chest" → List("Chest press", "Butterfly", "Cable cross-over")
    )
  val metadata = ModelMetadata(2)

  private def randomExercise(sessionProps: SessionProps): ClassifiedExercise = {
    val mgk = Random.shuffle(sessionProps.muscleGroupKeys).head
    exercises.get(mgk).fold[ClassifiedExercise](UnclassifiedExercise(metadata))(es ⇒ FullyClassifiedExercise(metadata, 1.0, Exercise(Random.shuffle(es).head, None)))
  }

  override def receive: Receive = {
    case ClassifyExerciseEvt(sessionProps, sdwls) =>
      sdwls.foreach { sdwl ⇒ sdwl.data}
        sdwls.foreach { sdwl ⇒

          sdwl.data.foreach {
            case AccelerometerData(sr, values) ⇒
              val xs = values.map(_.x)
              val ys = values.map(_.y)
              val zs = values.map(_.z)
              println(s"****** X: (${xs.min}, ${xs.max}), Y: (${ys.min}, ${ys.max}), Z: (${zs.min}, ${zs.max})")
          }
        }
      sender() ! randomExercise(sessionProps)
  }

}
