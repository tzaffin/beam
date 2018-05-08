// generated by tscfg 0.8.1 on Tue Apr 10 01:20:52 PDT 2018
// source: src/main/resources/beam-template.conf

package beam.sim.config

case class BeamConfig(
  beam   : BeamConfig.Beam,
  matsim : BeamConfig.Matsim
)
object BeamConfig {
  case class Beam(
    agentsim       : BeamConfig.Beam.Agentsim,
    debug          : BeamConfig.Beam.Debug,
    inputDirectory : java.lang.String,
    metrics        : BeamConfig.Beam.Metrics,
    outputs        : BeamConfig.Beam.Outputs,
    physsim        : BeamConfig.Beam.Physsim,
    routing        : BeamConfig.Beam.Routing,
    spatial        : BeamConfig.Beam.Spatial
  )
  object Beam {
    case class Agentsim(
      agents                                  : BeamConfig.Beam.Agentsim.Agents,
      numAgents                               : scala.Int,
      simulationName                          : java.lang.String,
      taz                                     : BeamConfig.Beam.Agentsim.Taz,
      thresholdForMakingParkingChoiceInMeters : scala.Int,
      thresholdForWalkingInMeters             : scala.Int,
      tuning                                  : BeamConfig.Beam.Agentsim.Tuning
    )
    object Agentsim {
      case class Agents(
        modalBehaviors : BeamConfig.Beam.Agentsim.Agents.ModalBehaviors,
        rideHailing    : BeamConfig.Beam.Agentsim.Agents.RideHailing
      )
      object Agents {
        case class ModalBehaviors(
          lccm             : BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.Lccm,
          modeChoiceClass  : java.lang.String,
          mulitnomialLogit : BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit
        )
        object ModalBehaviors {
          case class Lccm(
            paramFile : java.lang.String
          )
          object Lccm {
            def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.Lccm = {
              BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.Lccm(
                paramFile = if(c.hasPathOrNull("paramFile")) c.getString("paramFile") else "/test/input/beamville/lccm-long.csv"
              )
            }
          }
                
          case class MulitnomialLogit(
            params : BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit.Params
          )
          object MulitnomialLogit {
            case class Params(
              bike_intercept          : scala.Double,
              car_intercept           : scala.Double,
              cost                    : scala.Double,
              drive_transit_intercept : scala.Double,
              ride_hailing_intercept  : scala.Double,
              time                    : scala.Double,
              transfer                : scala.Double,
              walk_intercept          : scala.Double,
              walk_transit_intercept  : scala.Double
            )
            object Params {
              def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit.Params = {
                BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit.Params(
                  bike_intercept          = if(c.hasPathOrNull("bike_intercept")) c.getDouble("bike_intercept") else 0.0,
                  car_intercept           = if(c.hasPathOrNull("car_intercept")) c.getDouble("car_intercept") else 0.0,
                  cost                    = if(c.hasPathOrNull("cost")) c.getDouble("cost") else -1.0,
                  drive_transit_intercept = if(c.hasPathOrNull("drive_transit_intercept")) c.getDouble("drive_transit_intercept") else 0.0,
                  ride_hailing_intercept  = if(c.hasPathOrNull("ride_hailing_intercept")) c.getDouble("ride_hailing_intercept") else -2.0,
                  time                    = if(c.hasPathOrNull("time")) c.getDouble("time") else -0.0047,
                  transfer                = if(c.hasPathOrNull("transfer")) c.getDouble("transfer") else -1.4,
                  walk_intercept          = if(c.hasPathOrNull("walk_intercept")) c.getDouble("walk_intercept") else 0.0,
                  walk_transit_intercept  = if(c.hasPathOrNull("walk_transit_intercept")) c.getDouble("walk_transit_intercept") else 0.0
                )
              }
            }
                  
            def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit = {
              BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit(
                params = BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit.Params(c.getConfig("params"))
              )
            }
          }
                
          def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents.ModalBehaviors = {
            BeamConfig.Beam.Agentsim.Agents.ModalBehaviors(
              lccm             = BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.Lccm(c.getConfig("lccm")),
              modeChoiceClass  = if(c.hasPathOrNull("modeChoiceClass")) c.getString("modeChoiceClass") else "ModeChoiceMultinomialLogit",
              mulitnomialLogit = BeamConfig.Beam.Agentsim.Agents.ModalBehaviors.MulitnomialLogit(c.getConfig("mulitnomialLogit"))
            )
          }
        }
              
        case class RideHailing(
          defaultCostPerMile               : scala.Double,
          defaultCostPerMinute             : scala.Double,
          numDriversAsFractionOfPopulation : scala.Double,
          surgePricing                     : BeamConfig.Beam.Agentsim.Agents.RideHailing.SurgePricing
        )
        object RideHailing {
          case class SurgePricing(
            minimumSurgeLevel       : scala.Double,
            numberOfCategories      : scala.Int,
            priceAdjustmentStrategy : java.lang.String,
            surgeLevelAdaptionStep  : scala.Double,
            timeBinSize             : scala.Int
          )
          object SurgePricing {
            def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents.RideHailing.SurgePricing = {
              BeamConfig.Beam.Agentsim.Agents.RideHailing.SurgePricing(
                minimumSurgeLevel       = if(c.hasPathOrNull("minimumSurgeLevel")) c.getDouble("minimumSurgeLevel") else 0.1,
                numberOfCategories      = if(c.hasPathOrNull("numberOfCategories")) c.getInt("numberOfCategories") else 6,
                priceAdjustmentStrategy = if(c.hasPathOrNull("priceAdjustmentStrategy")) c.getString("priceAdjustmentStrategy") else "KEEP_PRICE_LEVEL_FIXED_AT_ONE",
                surgeLevelAdaptionStep  = if(c.hasPathOrNull("surgeLevelAdaptionStep")) c.getDouble("surgeLevelAdaptionStep") else 0.1,
                timeBinSize             = if(c.hasPathOrNull("timeBinSize")) c.getInt("timeBinSize") else 3600
              )
            }
          }

          def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents.RideHailing = {
            BeamConfig.Beam.Agentsim.Agents.RideHailing(
              defaultCostPerMile               = if(c.hasPathOrNull("defaultCostPerMile")) c.getDouble("defaultCostPerMile") else 1.25,
              defaultCostPerMinute             = if(c.hasPathOrNull("defaultCostPerMinute")) c.getDouble("defaultCostPerMinute") else 0.75,
              numDriversAsFractionOfPopulation = if(c.hasPathOrNull("numDriversAsFractionOfPopulation")) c.getDouble("numDriversAsFractionOfPopulation") else 0.5,
              surgePricing                     = BeamConfig.Beam.Agentsim.Agents.RideHailing.SurgePricing(c.getConfig("surgePricing"))
            )
          }
        }
              
        def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Agents = {
          BeamConfig.Beam.Agentsim.Agents(
            modalBehaviors = BeamConfig.Beam.Agentsim.Agents.ModalBehaviors(c.getConfig("modalBehaviors")),
            rideHailing    = BeamConfig.Beam.Agentsim.Agents.RideHailing(c.getConfig("rideHailing"))
          )
        }
      }
            
      case class Taz(
        file : java.lang.String
      )
      object Taz {
        def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Taz = {
          BeamConfig.Beam.Agentsim.Taz(
            file = if(c.hasPathOrNull("file")) c.getString("file") else "/test/input/beamville/taz-centers.csv"
          )
        }
      }
            
      case class Tuning(
        rideHailPrice   : scala.Double,
        tollPrice       : scala.Double,
        transitCapacity : scala.Double,
        transitPrice    : scala.Double
      )
      object Tuning {
        def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim.Tuning = {
          BeamConfig.Beam.Agentsim.Tuning(
            rideHailPrice   = if(c.hasPathOrNull("rideHailPrice")) c.getDouble("rideHailPrice") else 1.0,
            tollPrice       = if(c.hasPathOrNull("tollPrice")) c.getDouble("tollPrice") else 1.0,
            transitCapacity = if(c.hasPathOrNull("transitCapacity")) c.getDouble("transitCapacity") else 1.0,
            transitPrice    = if(c.hasPathOrNull("transitPrice")) c.getDouble("transitPrice") else 1.0
          )
        }
      }
            
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Agentsim = {
        BeamConfig.Beam.Agentsim(
          agents                                  = BeamConfig.Beam.Agentsim.Agents(c.getConfig("agents")),
          numAgents                               = if(c.hasPathOrNull("numAgents")) c.getInt("numAgents") else 100,
          simulationName                          = if(c.hasPathOrNull("simulationName")) c.getString("simulationName") else "beamville",
          taz                                     = BeamConfig.Beam.Agentsim.Taz(c.getConfig("taz")),
          thresholdForMakingParkingChoiceInMeters = if(c.hasPathOrNull("thresholdForMakingParkingChoiceInMeters")) c.getInt("thresholdForMakingParkingChoiceInMeters") else 100,
          thresholdForWalkingInMeters             = if(c.hasPathOrNull("thresholdForWalkingInMeters")) c.getInt("thresholdForWalkingInMeters") else 100,
          tuning                                  = BeamConfig.Beam.Agentsim.Tuning(c.getConfig("tuning"))
        )
      }
    }
          
    case class Debug(
      debugEnabled                         : scala.Boolean,
      memoryConsumptionDisplayTimeoutInSec : scala.Int,
      secondsToWaitForSkip                 : scala.Int,
      skipOverBadActors                    : scala.Boolean
    )
    object Debug {
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Debug = {
        BeamConfig.Beam.Debug(
          debugEnabled                         = c.hasPathOrNull("debugEnabled") && c.getBoolean("debugEnabled"),
          memoryConsumptionDisplayTimeoutInSec = if(c.hasPathOrNull("memoryConsumptionDisplayTimeoutInSec")) c.getInt("memoryConsumptionDisplayTimeoutInSec") else 0,
          secondsToWaitForSkip                 = if(c.hasPathOrNull("secondsToWaitForSkip")) c.getInt("secondsToWaitForSkip") else 10,
          skipOverBadActors                    = c.hasPathOrNull("skipOverBadActors") && c.getBoolean("skipOverBadActors")
        )
      }
    }
          
    case class Metrics(
      level : java.lang.String
    )
    object Metrics {
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Metrics = {
        BeamConfig.Beam.Metrics(
          level = if(c.hasPathOrNull("level")) c.getString("level") else "verbose"
        )
      }
    }
          
    case class Outputs(
      addTimestampToOutputDirectory : scala.Boolean,
      baseOutputDirectory           : java.lang.String,
      events                        : BeamConfig.Beam.Outputs.Events,
      writeEventsInterval           : scala.Int,
      writePlansInterval            : scala.Int
    )
    object Outputs {
      case class Events(
        defaultWritingLevel   : java.lang.String,
        explodeIntoFiles      : scala.Boolean,
        fileOutputFormats     : java.lang.String,
        overrideWritingLevels : java.lang.String
      )
      object Events {
        def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Outputs.Events = {
          BeamConfig.Beam.Outputs.Events(
            defaultWritingLevel   = if(c.hasPathOrNull("defaultWritingLevel")) c.getString("defaultWritingLevel") else "OFF",
            explodeIntoFiles      = c.hasPathOrNull("explodeIntoFiles") && c.getBoolean("explodeIntoFiles"),
            fileOutputFormats     = if(c.hasPathOrNull("fileOutputFormats")) c.getString("fileOutputFormats") else "csv",
            overrideWritingLevels = if(c.hasPathOrNull("overrideWritingLevels")) c.getString("overrideWritingLevels") else "beam.agentsim.events.ModeChoiceEvent:VERBOSE, beam.agentsim.events.PathTraversalEvent:VERBOSE"
          )
        }
      }
            
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Outputs = {
        BeamConfig.Beam.Outputs(
          addTimestampToOutputDirectory = !c.hasPathOrNull("addTimestampToOutputDirectory") || c.getBoolean("addTimestampToOutputDirectory"),
          baseOutputDirectory           = if(c.hasPathOrNull("baseOutputDirectory")) c.getString("baseOutputDirectory") else "/Users/critter/Documents/beam/beam-output/",
          events                        = BeamConfig.Beam.Outputs.Events(c.getConfig("events")),
          writeEventsInterval           = if(c.hasPathOrNull("writeEventsInterval")) c.getInt("writeEventsInterval") else 1,
          writePlansInterval            = if(c.hasPathOrNull("writePlansInterval")) c.getInt("writePlansInterval") else 0
        )
      }
    }
          
    case class Physsim(
      flowCapacityFactor     : scala.Double,
      linkstatsBinSize       : scala.Int,
      linkstatsWriteInterval : scala.Int,
      ptSampleSize           : scala.Double,
      storageCapacityFactor  : scala.Double,
      writeEventsInterval    : scala.Int,
      writeMATSimNetwork     : scala.Boolean,
      writePlansInterval     : scala.Int
    )
    object Physsim {
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Physsim = {
        BeamConfig.Beam.Physsim(
          flowCapacityFactor     = if(c.hasPathOrNull("flowCapacityFactor")) c.getDouble("flowCapacityFactor") else 1.0,
          linkstatsBinSize       = if(c.hasPathOrNull("linkstatsBinSize")) c.getInt("linkstatsBinSize") else 3600,
          linkstatsWriteInterval = if(c.hasPathOrNull("linkstatsWriteInterval")) c.getInt("linkstatsWriteInterval") else 1,
          ptSampleSize           = if(c.hasPathOrNull("ptSampleSize")) c.getDouble("ptSampleSize") else 1.0,
          storageCapacityFactor  = if(c.hasPathOrNull("storageCapacityFactor")) c.getDouble("storageCapacityFactor") else 1.0,
          writeEventsInterval    = if(c.hasPathOrNull("writeEventsInterval")) c.getInt("writeEventsInterval") else 0,
          writeMATSimNetwork     = c.hasPathOrNull("writeMATSimNetwork") && c.getBoolean("writeMATSimNetwork"),
          writePlansInterval     = if(c.hasPathOrNull("writePlansInterval")) c.getInt("writePlansInterval") else 0
        )
      }
    }
          
    case class Routing(
      baseDate               : java.lang.String,
      gtfs                   : BeamConfig.Beam.Routing.Gtfs,
      r5                     : BeamConfig.Beam.Routing.R5,
      transitOnStreetNetwork : scala.Boolean
    )
    object Routing {
      case class Gtfs(
        crs           : java.lang.String,
        operatorsFile : java.lang.String,
        outputDir     : java.lang.String
      )
      object Gtfs {
        def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Routing.Gtfs = {
          BeamConfig.Beam.Routing.Gtfs(
            crs           = if(c.hasPathOrNull("crs")) c.getString("crs") else "epsg:26910",
            operatorsFile = if(c.hasPathOrNull("operatorsFile")) c.getString("operatorsFile") else "src/main/resources/GTFSOperators.csv",
            outputDir     = if(c.hasPathOrNull("outputDir")) c.getString("outputDir") else "/Users/critter/Documents/beam/beam-output//gtfs"
          )
        }
      }
            
      case class R5(
        departureWindow : scala.Double,
        directory       : java.lang.String,
        numberOfSamples : scala.Int,
        osmFile         : java.lang.String,
        osmMapdbFile    : java.lang.String
      )
      object R5 {
        def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Routing.R5 = {
          BeamConfig.Beam.Routing.R5(
            departureWindow = if(c.hasPathOrNull("departureWindow")) c.getDouble("departureWindow") else 15.0,
            directory       = if(c.hasPathOrNull("directory")) c.getString("directory") else "/test/input/beamville/r5",
            numberOfSamples = if(c.hasPathOrNull("numberOfSamples")) c.getInt("numberOfSamples") else 1,
            osmFile         = if(c.hasPathOrNull("osmFile")) c.getString("osmFile") else "/test/input/beamville/r5/beamville.osm.pbf",
            osmMapdbFile    = if(c.hasPathOrNull("osmMapdbFile")) c.getString("osmMapdbFile") else "/test/input/beamville/r5/osm.mapdb"
          )
        }
      }
            
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Routing = {
        BeamConfig.Beam.Routing(
          baseDate               = if(c.hasPathOrNull("baseDate")) c.getString("baseDate") else "2016-10-17T00:00:00-07:00",
          gtfs                   = BeamConfig.Beam.Routing.Gtfs(c.getConfig("gtfs")),
          r5                     = BeamConfig.Beam.Routing.R5(c.getConfig("r5")),
          transitOnStreetNetwork = !c.hasPathOrNull("transitOnStreetNetwork") || c.getBoolean("transitOnStreetNetwork")
        )
      }
    }
          
    case class Spatial(
      boundingBoxBuffer : scala.Int,
      localCRS          : java.lang.String
    )
    object Spatial {
      def apply(c: com.typesafe.config.Config): BeamConfig.Beam.Spatial = {
        BeamConfig.Beam.Spatial(
          boundingBoxBuffer = if(c.hasPathOrNull("boundingBoxBuffer")) c.getInt("boundingBoxBuffer") else 5000,
          localCRS          = if(c.hasPathOrNull("localCRS")) c.getString("localCRS") else "epsg:32631"
        )
      }
    }
          
    def apply(c: com.typesafe.config.Config): BeamConfig.Beam = {
      BeamConfig.Beam(
        agentsim       = BeamConfig.Beam.Agentsim(c.getConfig("agentsim")),
        debug          = BeamConfig.Beam.Debug(c.getConfig("debug")),
        inputDirectory = if(c.hasPathOrNull("inputDirectory")) c.getString("inputDirectory") else "/test/input/beamville",
        metrics        = BeamConfig.Beam.Metrics(c.getConfig("metrics")),
        outputs        = BeamConfig.Beam.Outputs(c.getConfig("outputs")),
        physsim        = BeamConfig.Beam.Physsim(c.getConfig("physsim")),
        routing        = BeamConfig.Beam.Routing(c.getConfig("routing")),
        spatial        = BeamConfig.Beam.Spatial(c.getConfig("spatial"))
      )
    }
  }
        
  case class Matsim(
    modules : BeamConfig.Matsim.Modules
  )
  object Matsim {
    case class Modules(
      changeMode            : BeamConfig.Matsim.Modules.ChangeMode,
      controler             : BeamConfig.Matsim.Modules.Controler,
      global                : BeamConfig.Matsim.Modules.Global,
      households            : BeamConfig.Matsim.Modules.Households,
      network               : BeamConfig.Matsim.Modules.Network,
      parallelEventHandling : BeamConfig.Matsim.Modules.ParallelEventHandling,
      planCalcScore         : BeamConfig.Matsim.Modules.PlanCalcScore,
      plans                 : BeamConfig.Matsim.Modules.Plans,
      qsim                  : BeamConfig.Matsim.Modules.Qsim,
      strategy              : BeamConfig.Matsim.Modules.Strategy,
      transit               : BeamConfig.Matsim.Modules.Transit,
      vehicles              : BeamConfig.Matsim.Modules.Vehicles
    )
    object Modules {
      case class ChangeMode(
        modes : java.lang.String
      )
      object ChangeMode {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.ChangeMode = {
          BeamConfig.Matsim.Modules.ChangeMode(
            modes = if(c.hasPathOrNull("modes")) c.getString("modes") else "car,pt"
          )
        }
      }
            
      case class Controler(
        eventsFileFormat : java.lang.String,
        firstIteration   : scala.Int,
        lastIteration    : scala.Int,
        mobsim           : java.lang.String,
        outputDirectory  : java.lang.String,
        overwriteFiles   : java.lang.String
      )
      object Controler {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Controler = {
          BeamConfig.Matsim.Modules.Controler(
            eventsFileFormat = if(c.hasPathOrNull("eventsFileFormat")) c.getString("eventsFileFormat") else "xml",
            firstIteration   = if(c.hasPathOrNull("firstIteration")) c.getInt("firstIteration") else 0,
            lastIteration    = if(c.hasPathOrNull("lastIteration")) c.getInt("lastIteration") else 0,
            mobsim           = if(c.hasPathOrNull("mobsim")) c.getString("mobsim") else "metasim",
            outputDirectory  = if(c.hasPathOrNull("outputDirectory")) c.getString("outputDirectory") else "/Users/critter/Documents/beam/beam-output//pt-tutorial",
            overwriteFiles   = if(c.hasPathOrNull("overwriteFiles")) c.getString("overwriteFiles") else "overwriteExistingFiles"
          )
        }
      }
            
      case class Global(
        coordinateSystem : java.lang.String,
        randomSeed       : scala.Int
      )
      object Global {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Global = {
          BeamConfig.Matsim.Modules.Global(
            coordinateSystem = if(c.hasPathOrNull("coordinateSystem")) c.getString("coordinateSystem") else "Atlantis",
            randomSeed       = if(c.hasPathOrNull("randomSeed")) c.getInt("randomSeed") else 4711
          )
        }
      }
            
      case class Households(
        inputFile                    : java.lang.String,
        inputHouseholdAttributesFile : java.lang.String
      )
      object Households {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Households = {
          BeamConfig.Matsim.Modules.Households(
            inputFile                    = if(c.hasPathOrNull("inputFile")) c.getString("inputFile") else "/test/input/beamville/households.xml",
            inputHouseholdAttributesFile = if(c.hasPathOrNull("inputHouseholdAttributesFile")) c.getString("inputHouseholdAttributesFile") else "/test/input/beamville/householdAttributes.xml"
          )
        }
      }
            
      case class Network(
        inputNetworkFile : java.lang.String
      )
      object Network {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Network = {
          BeamConfig.Matsim.Modules.Network(
            inputNetworkFile = if(c.hasPathOrNull("inputNetworkFile")) c.getString("inputNetworkFile") else "/test/input/beamville/physsim-network.xml"
          )
        }
      }
            
      case class ParallelEventHandling(
        estimatedNumberOfEvents : scala.Int,
        numberOfThreads         : scala.Int,
        oneThreadPerHandler     : scala.Boolean,
        synchronizeOnSimSteps   : scala.Boolean
      )
      object ParallelEventHandling {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.ParallelEventHandling = {
          BeamConfig.Matsim.Modules.ParallelEventHandling(
            estimatedNumberOfEvents = if(c.hasPathOrNull("estimatedNumberOfEvents")) c.getInt("estimatedNumberOfEvents") else 1000000000,
            numberOfThreads         = if(c.hasPathOrNull("numberOfThreads")) c.getInt("numberOfThreads") else 1,
            oneThreadPerHandler     = c.hasPathOrNull("oneThreadPerHandler") && c.getBoolean("oneThreadPerHandler"),
            synchronizeOnSimSteps   = c.hasPathOrNull("synchronizeOnSimSteps") && c.getBoolean("synchronizeOnSimSteps")
          )
        }
      }
            
      case class PlanCalcScore(
        BrainExpBeta   : scala.Long,
        earlyDeparture : scala.Long,
        lateArrival    : scala.Long,
        learningRate   : scala.Long,
        parameterset   : scala.List[BeamConfig.Matsim.Modules.PlanCalcScore.Parameterset$Elm],
        performing     : scala.Long,
        traveling      : scala.Long,
        waiting        : scala.Long
      )
      object PlanCalcScore {
        case class Parameterset$Elm(
          activityType                    : java.lang.String,
          priority                        : scala.Int,
          scoringThisActivityAtAll        : scala.Boolean,
          `type`                          : java.lang.String,
          typicalDuration                 : java.lang.String,
          typicalDurationScoreComputation : java.lang.String
        )
        object Parameterset$Elm {
          def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.PlanCalcScore.Parameterset$Elm = {
            BeamConfig.Matsim.Modules.PlanCalcScore.Parameterset$Elm(
              activityType                    = if(c.hasPathOrNull("activityType")) c.getString("activityType") else "Home",
              priority                        = if(c.hasPathOrNull("priority")) c.getInt("priority") else 1,
              scoringThisActivityAtAll        = !c.hasPathOrNull("scoringThisActivityAtAll") || c.getBoolean("scoringThisActivityAtAll"),
              `type`                          = if(c.hasPathOrNull("type")) c.getString("type") else "activityParams",
              typicalDuration                 = if(c.hasPathOrNull("typicalDuration")) c.getString("typicalDuration") else "01:00:00",
              typicalDurationScoreComputation = if(c.hasPathOrNull("typicalDurationScoreComputation")) c.getString("typicalDurationScoreComputation") else "uniform"
            )
          }
        }
              
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.PlanCalcScore = {
          BeamConfig.Matsim.Modules.PlanCalcScore(
            BrainExpBeta   = if(c.hasPathOrNull("BrainExpBeta")) c.getDuration("BrainExpBeta", java.util.concurrent.TimeUnit.MILLISECONDS) else 2,
            earlyDeparture = if(c.hasPathOrNull("earlyDeparture")) c.getDuration("earlyDeparture", java.util.concurrent.TimeUnit.MILLISECONDS) else 0,
            lateArrival    = if(c.hasPathOrNull("lateArrival")) c.getDuration("lateArrival", java.util.concurrent.TimeUnit.MILLISECONDS) else -18,
            learningRate   = if(c.hasPathOrNull("learningRate")) c.getDuration("learningRate", java.util.concurrent.TimeUnit.MILLISECONDS) else 1,
            parameterset   = $_LBeamConfig_Matsim_Modules_PlanCalcScore_Parameterset$Elm(c.getList("parameterset")),
            performing     = if(c.hasPathOrNull("performing")) c.getDuration("performing", java.util.concurrent.TimeUnit.MILLISECONDS) else 6,
            traveling      = if(c.hasPathOrNull("traveling")) c.getDuration("traveling", java.util.concurrent.TimeUnit.MILLISECONDS) else -6,
            waiting        = if(c.hasPathOrNull("waiting")) c.getDuration("waiting", java.util.concurrent.TimeUnit.MILLISECONDS) else 0
          )
        }
        private def $_LBeamConfig_Matsim_Modules_PlanCalcScore_Parameterset$Elm(cl:com.typesafe.config.ConfigList): scala.List[BeamConfig.Matsim.Modules.PlanCalcScore.Parameterset$Elm] = {
          import scala.collection.JavaConverters._  
          cl.asScala.map(cv => BeamConfig.Matsim.Modules.PlanCalcScore.Parameterset$Elm(cv.asInstanceOf[com.typesafe.config.ConfigObject].toConfig)).toList
        }
      }
            
      case class Plans(
        inputPersonAttributesFile : java.lang.String,
        inputPlansFile            : java.lang.String
      )
      object Plans {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Plans = {
          BeamConfig.Matsim.Modules.Plans(
            inputPersonAttributesFile = if(c.hasPathOrNull("inputPersonAttributesFile")) c.getString("inputPersonAttributesFile") else "/test/input/beamville/populationAttributes.xml",
            inputPlansFile            = if(c.hasPathOrNull("inputPlansFile")) c.getString("inputPlansFile") else "/test/input/beamville/population.xml"
          )
        }
      }
            
      case class Qsim(
        endTime        : java.lang.String,
        snapshotperiod : java.lang.String,
        startTime      : java.lang.String
      )
      object Qsim {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Qsim = {
          BeamConfig.Matsim.Modules.Qsim(
            endTime        = if(c.hasPathOrNull("endTime")) c.getString("endTime") else "30:00:00",
            snapshotperiod = if(c.hasPathOrNull("snapshotperiod")) c.getString("snapshotperiod") else "00:00:00",
            startTime      = if(c.hasPathOrNull("startTime")) c.getString("startTime") else "00:00:00"
          )
        }
      }
            
      case class Strategy(
        ModuleProbability_1    : scala.Double,
        ModuleProbability_3    : scala.Double,
        Module_1               : java.lang.String,
        Module_3               : java.lang.String,
        maxAgentPlanMemorySize : scala.Int
      )
      object Strategy {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Strategy = {
          BeamConfig.Matsim.Modules.Strategy(
            ModuleProbability_1    = if(c.hasPathOrNull("ModuleProbability_1")) c.getDouble("ModuleProbability_1") else 0.7,
            ModuleProbability_3    = if(c.hasPathOrNull("ModuleProbability_3")) c.getDouble("ModuleProbability_3") else 0.1,
            Module_1               = if(c.hasPathOrNull("Module_1")) c.getString("Module_1") else "BestScore",
            Module_3               = if(c.hasPathOrNull("Module_3")) c.getString("Module_3") else "TimeAllocationMutator",
            maxAgentPlanMemorySize = if(c.hasPathOrNull("maxAgentPlanMemorySize")) c.getInt("maxAgentPlanMemorySize") else 5
          )
        }
      }
            
      case class Transit(
        transitModes : java.lang.String,
        useTransit   : scala.Boolean,
        vehiclesFile : java.lang.String
      )
      object Transit {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Transit = {
          BeamConfig.Matsim.Modules.Transit(
            transitModes = if(c.hasPathOrNull("transitModes")) c.getString("transitModes") else "pt",
            useTransit   = c.hasPathOrNull("useTransit") && c.getBoolean("useTransit"),
            vehiclesFile = if(c.hasPathOrNull("vehiclesFile")) c.getString("vehiclesFile") else "/test/input/beamville/transitVehicles.xml"
          )
        }
      }
            
      case class Vehicles(
        vehiclesFile : java.lang.String
      )
      object Vehicles {
        def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules.Vehicles = {
          BeamConfig.Matsim.Modules.Vehicles(
            vehiclesFile = if(c.hasPathOrNull("vehiclesFile")) c.getString("vehiclesFile") else "/test/input/beamville/vehicles.xml"
          )
        }
      }
            
      def apply(c: com.typesafe.config.Config): BeamConfig.Matsim.Modules = {
        BeamConfig.Matsim.Modules(
          changeMode            = BeamConfig.Matsim.Modules.ChangeMode(c.getConfig("changeMode")),
          controler             = BeamConfig.Matsim.Modules.Controler(c.getConfig("controler")),
          global                = BeamConfig.Matsim.Modules.Global(c.getConfig("global")),
          households            = BeamConfig.Matsim.Modules.Households(c.getConfig("households")),
          network               = BeamConfig.Matsim.Modules.Network(c.getConfig("network")),
          parallelEventHandling = BeamConfig.Matsim.Modules.ParallelEventHandling(c.getConfig("parallelEventHandling")),
          planCalcScore         = BeamConfig.Matsim.Modules.PlanCalcScore(c.getConfig("planCalcScore")),
          plans                 = BeamConfig.Matsim.Modules.Plans(c.getConfig("plans")),
          qsim                  = BeamConfig.Matsim.Modules.Qsim(c.getConfig("qsim")),
          strategy              = BeamConfig.Matsim.Modules.Strategy(c.getConfig("strategy")),
          transit               = BeamConfig.Matsim.Modules.Transit(c.getConfig("transit")),
          vehicles              = BeamConfig.Matsim.Modules.Vehicles(c.getConfig("vehicles"))
        )
      }
    }
          
    def apply(c: com.typesafe.config.Config): BeamConfig.Matsim = {
      BeamConfig.Matsim(
        modules = BeamConfig.Matsim.Modules(c.getConfig("modules"))
      )
    }
  }
        
  def apply(c: com.typesafe.config.Config): BeamConfig = {
    BeamConfig(
      beam   = BeamConfig.Beam(c.getConfig("beam")),
      matsim = BeamConfig.Matsim(c.getConfig("matsim"))
    )
  }
}
      
