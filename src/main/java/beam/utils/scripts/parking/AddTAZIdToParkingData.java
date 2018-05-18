package beam.utils.scripts.parking;

public class AddTAZIdToParkingData {

    public static void main(String[] args) {
        // read urbanSim

        // read taz file

        // write updated urbanSim data with tazId (closest one for each line)


        /*

// read taz file: TAZTreeMap.fromCsv(beamConfig.beam.agentsim.taz.file) -> TAZTreeMap

        for (line:urbanSimFileLines){

            line.tazId=taz.get(line.lon, line.lat)



        }

        // merge duplicate tazIds, sum up fields



         */




// =======================================================
        // read street parking data
        // read taz file
        // write updated street parking data (calculate average of coordinates - provide lon,lat and add taz column)

        // repeat same for garage parking data

        // master output: tazId, x,y, all_jobs,	retail_jobs,	persons,	residential_units, steet_parking_capacity, garage_parking_capacity


    }


    // create object with fields: long(x), lat(y)	shape_area	all_jobs	retail_jobs	persons	residential_units	tazId
// don't have to write out long and lat, as tazId contains its own

}
