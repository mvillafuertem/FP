package fp.part1.state

import java.time.LocalDate

import fp.part1.state.GeneralState.State

object EducationalPath extends App {

  case class Education(date: LocalDate, level: String)
  case class Experience(date: LocalDate, subject: String)
  case class Skill(date: LocalDate, subject: String)
  case class Story(birthday: LocalDate,
                   schoolLevel: Option[Education],
                   experiences: Vector[Experience],
                   skills: Vector[Skill])

  def addExperience(experience: Experience): State[Story, Option[Education]] =
    State(oldState =>
      None -> oldState.copy(experiences = oldState.experiences :+ experience))
  def updateEducation(
      maybeLevel: Option[Education]): State[Story, Option[Education]] = {
    State(oldState => maybeLevel -> oldState.copy(schoolLevel = maybeLevel))
  }
  def addSkill(skill: Skill): State[Story, Option[Education]] =
    State(oldState => None -> oldState.copy(skills = oldState.skills :+ skill))

  sealed trait Event
  case class GoToSchool(level: String) extends Event
  case object FinishSchool extends Event
  case class AddExperience(subject: String) extends Event
  case class AddSkill(subject: String) extends Event

  val scenario: List[Event] = List(
    GoToSchool("First Class"),
    AddSkill("Painting"),
    AddSkill("Football"),
    GoToSchool("Second Class"),
    AddSkill("Puzzle games"),
    GoToSchool("bachelor's degree"),
    AddExperience("Waiter"),
    AddExperience("Freelancer"),
    AddExperience("Travel to London"),
    FinishSchool,
    AddExperience("Get married"),
    AddExperience("Have kids")
  )
  val initialStory: Story =
    Story(LocalDate.of(1990, 9, 22), None, Vector.empty, Vector.empty)

  def makeStory(event: Event,
                date: LocalDate): State[Story, Option[Education]] =
    event match {
      case GoToSchool(level) =>
        updateEducation(Some(Education(date, level)))
      case FinishSchool => updateEducation(None)
      case AddExperience(subject) =>
        addExperience(Experience(date, subject))
      case AddSkill(subject) => addSkill(Skill(date, subject))
    }

  def age(date: LocalDate, birthday: LocalDate): Int =
    date.getYear - birthday.getYear
  def evaluate(scenario: List[Event],
               date: LocalDate): State[Story, (Int, List[Education])] =
    for {
      education <- State
        .sequence(scenario.map(event => makeStory(event, date)))
        .map(_.flatten)
      state <- State.get
    } yield (age(date, state.birthday), education)

  println(
    s"*** evaluate scenario: ${evaluate(scenario, LocalDate.now).run(initialStory)}")

}
