package beam.experiment

import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.{Config, ConfigValueFactory}

/**
  * Created by dserdiuk on 11/25/17.
  */
case class ExperimentRunSandbox(projectRoot: Path, experimentBaseDir: Path, experimentDef: ExperimentDef, experimentRun: ExperimentRun, beamTplConf: Config) {
  require(Files.exists(experimentBaseDir))

  lazy val runConfig: Config = buildRunConfig

  def runDirectory = Paths.get(experimentBaseDir.toString,
    "runs", s"run.${experimentRun.name}"
  )

  def modeChoiceParametersXmlPath = Paths.get(runDirectory.toString, "modeChoiceParameters.xml")

  def runBeamScriptPath = Paths.get(runDirectory.toString, "runBeam.sh")


  def beamConfPath = {
    projectRoot.relativize(Paths.get(runDirectory.toString, "beam.conf"))
  }

  /**
    *
    * @return path to an output folder relatively to project root
    */
  def beamOutputDir = {
    projectRoot.relativize(Paths.get(runDirectory.toString, "output"))
  }

  def buildRunConfig = {
    // set critical properties
    // beam.agentsim.agents.modalBehaviors.modeChoiceParametersFile
    // beam.outputs.baseOutputDirectory
    val runConfig = ( Map(
      "beam.agentsim.simulationName" -> "output",
      "beam.outputs.baseOutputDirectory" -> beamOutputDir.getParent.toString,
      "beam.outputs.addTimestampToOutputDirectory" -> "false",
      "beam.inputDirectory" -> experimentDef.getTemplateConfigParentDirAsString
    ) ++ modeChoiceConfigIfDefined ++ experimentRun.params).foldLeft(beamTplConf) { case (prevConfig, (paramName, paramValue)) =>
        val configValue = ConfigValueFactory.fromAnyRef(paramValue)
        prevConfig.withValue(paramName, configValue)
    }
    runConfig
  }

  def modeChoiceConfigIfDefined = {
    experimentDef.header.modeChoiceTemplate match {
      case "" =>
        Map()
      case _ =>
        Map("beam.agentsim.agents.modalBehaviors.modeChoiceParametersFile" -> projectRoot.relativize(modeChoiceParametersXmlPath).toString)
    }
  }
}
