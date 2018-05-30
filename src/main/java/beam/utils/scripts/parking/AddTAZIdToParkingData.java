package beam.utils.scripts.parking;
import beam.agentsim.infrastructure.TAZ;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;


import java.io.*;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.Double;
import java.io.FileWriter;



public class AddTAZIdToParkingData {

    public static void main(String[] args) {
        try {
            if (args.length < 10)
                return;
            // args[0] contains taz assignment data
            ArrayList<TAZ> ids = setTAZIDs(args[0]);

            // args[1] contains urbanSim data, args[2] is output with appended TAZids and removed duplicates
            removeDuplicateTAZIDs(getUrbanSimData(ids, args[1]), args[2]);
            // args[3] is onstreet parking data, args[4] is output with appended lon, lat, and TAZids
            updateParkingData(ids, args[3], args[4]);
            // args[5] is offstreet parking data, args[6] is output with appended lon, lat, and TAZids
            updateParkingData(ids, args[5], args[6]);
            // args[7] is urbansim data with appended onstreet parking
            addOnStreetParkingData(args[2], args[4], args[7]);
            // args[8] is urbansim data with appended offstreet parking
            addOffStreetParkingData(args[7], args[6], args[8]);
            // args[9] is urbanSim data without shape area
            removeAColumn(3, args[8], args[9]); 

            System.out.println("Final output file written");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    public static void removeAColumn(int colToRemove, String finalAggregateDataPath, String finalAggregateDataOutput) throws FileNotFoundException{
        try {
            File file = new File(finalAggregateDataPath);
            BufferedReader br = new BufferedReader(new FileReader(file));

            FileWriter outFile = new FileWriter(finalAggregateDataOutput);
            String urbanSimLine;
            while ((urbanSimLine = br.readLine()) != null) {

                String[] urbanSimLineEntries = urbanSimLine.split(",");
                for (int i = 0; i < urbanSimLineEntries.length; i++){
                    if (i != colToRemove){
                        outFile.append(urbanSimLineEntries[i] + ",");
                    }
                }
                outFile.append("\n");

            }
            outFile.flush();
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addOnStreetParkingData(String urbanSimFilePath, String streetParkingFilePath, String outputFilePath)throws FileNotFoundException{
        try {
            File file = new File(urbanSimFilePath);
            File streetParkingFile = new File (streetParkingFilePath);
            BufferedReader br = new BufferedReader(new FileReader(file));

            FileWriter outFile = new FileWriter(outputFilePath);
            String urbanSimLine = br.readLine();
            outFile.append(urbanSimLine + ",STRT_PRKG_SPLY\n");
            while ((urbanSimLine = br.readLine()) != null) {


                String[] urbanSimLineEntries = urbanSimLine.split(",");
                int parkingSupply = 0;
                BufferedReader streetBr = new BufferedReader(new FileReader(streetParkingFile));
                String streetParkingLine = streetBr.readLine();

                while ((streetParkingLine = streetBr.readLine())!= null){
                    while (!streetParkingLine.contains("}")){
                        streetParkingLine += streetBr.readLine();
                    }
                    String[] streetParkingEntries = streetParkingLine.split(",");
                if (streetParkingEntries[streetParkingEntries.length-1].equals(urbanSimLineEntries[0])){
                    parkingSupply += Integer.parseInt(streetParkingEntries[streetParkingEntries.length-11]);
                }

                }

                outFile.append(urbanSimLine + "," + parkingSupply + "\n");
            }
            outFile.flush();
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addOffStreetParkingData(String urbanSimFilePath, String offStreetParkingFilePath, String outputFilePath)throws FileNotFoundException{
        try {
        File file = new File(urbanSimFilePath);
        File streetParkingFile = new File (offStreetParkingFilePath);
        BufferedReader br = new BufferedReader(new FileReader(file));

        FileWriter outFile = new FileWriter(outputFilePath);
        String urbanSimLine = br.readLine();
        outFile.append(urbanSimLine + ",REGCAP_1,VALETCAP_1,MC_CAP_1\n");
        while ((urbanSimLine = br.readLine()) != null) {


            String[] urbanSimLineEntries = urbanSimLine.split(",");
            int REG_CAP = 0, VALET_CAP = 0, MC_CAP = 0;
            BufferedReader offStreetBr = new BufferedReader(new FileReader(streetParkingFile));
            String offStreetParkingLine = offStreetBr.readLine();

            while ((offStreetParkingLine = offStreetBr.readLine())!= null){
                while (!offStreetParkingLine.contains("}")){
                    offStreetParkingLine += offStreetBr.readLine();
                }
                String[] offStreetParkingEntries = offStreetParkingLine.split(",");
                if (offStreetParkingEntries[offStreetParkingEntries.length-1].equals(urbanSimLineEntries[0])){
                    // if no G or L indicator, it will be treated as normal
                    // if there is, use it as reference point
                    int indexOfG_L_1 = 0;
                    for (int indexOfGarageOrLot = 0; indexOfGarageOrLot < offStreetParkingEntries.length; indexOfGarageOrLot++){
                        if ((offStreetParkingEntries[indexOfGarageOrLot].contains("L") ||
                            offStreetParkingEntries[indexOfGarageOrLot].contains("G")) && offStreetParkingEntries[indexOfGarageOrLot].length() < 3){
                            indexOfG_L_1 = indexOfGarageOrLot;
                            break;
                        }
                    }
                    if (indexOfG_L_1 == 0){
                        REG_CAP += Integer.parseInt(offStreetParkingEntries[13]);
                        VALET_CAP += Integer.parseInt(offStreetParkingEntries[14]);
                        MC_CAP += Integer.parseInt(offStreetParkingEntries[15]);
                    }
                    else{
                        REG_CAP += Integer.parseInt(offStreetParkingEntries[indexOfG_L_1 + 4]);
                        VALET_CAP += Integer.parseInt(offStreetParkingEntries[indexOfG_L_1 + 5]);
                        MC_CAP += Integer.parseInt(offStreetParkingEntries[indexOfG_L_1 + 6]);
                    }

                }

            }

            outFile.append(urbanSimLine + "," + REG_CAP + "," + VALET_CAP + "," + MC_CAP + "\n");
        }
        outFile.flush();
        outFile.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public static void removeDuplicateTAZIDs(ArrayList<Line> lines, String outputFilePath)throws FileNotFoundException{
        String st = "TAZid,x,y," +
                "shape_area,all_jobs,retail_jobs,persons,residential_units\n";

        try {
            FileWriter outFile = new FileWriter(outputFilePath);
            outFile.append(st);

            for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++){
                for (int lineDuplicate = lineNumber + 1; lineDuplicate < lines.size(); lineDuplicate++){
                    if (lines.get(lineNumber).gettID().equals(lines.get(lineDuplicate).gettID())){
                        lines.get(lineNumber).addLines(lines.get(lineDuplicate));
                        lines.remove(lineDuplicate);
                        lineDuplicate--;
                    }

                }
                outFile.append(lines.get(lineNumber).toString());
            }

            outFile.flush();
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public static void updateParkingData(ArrayList<TAZ> ids, String inputFilePath, String outputFilePath)throws FileNotFoundException{

         try {
             File file = new File(inputFilePath);
             BufferedReader br = new BufferedReader(new FileReader(file));
             FileWriter outFile = new FileWriter(outputFilePath);
             String st = br.readLine();
             outFile.append(st + ",,lat,tazID\n");
             while ((st = br.readLine()) != null) {
                 while (!st.contains("}")){
                     st += br.readLine();
                 }
                 //String locData = st.substring(0, st.lastIndexOf(')')+1);
                 //System.out.println(locData+"\n");
                 String allCoordinates = "";
                 String[] entries = st.split(",");
                 for (int entryIndex = 0; entryIndex < entries.length && !allCoordinates.contains(")"); entryIndex++){
                     allCoordinates += entries[entryIndex] + ",";
                 }

                 Coord lineCoord = parseLocation(allCoordinates);
                 if (lineCoord != null) {
                     TAZ tID = calcTAZ(ids, lineCoord);
                     outFile.append(st + "," + lineCoord.getX() + "," + lineCoord.getY() + "," + tID.tazId() + "\n");
                 }
                 else{
                     outFile.append(st);
                 }
             }
             outFile.flush();
             outFile.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }


     public static ArrayList<Line> getUrbanSimData(ArrayList<TAZ> ids, String inputFilePath)throws FileNotFoundException{


        try {
            ArrayList<Line> lines = new ArrayList<Line>();
            File file = new File(inputFilePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st = br.readLine();
            while ((st = br.readLine()) != null) {
                String[] entries = st.split(",");
                TAZ tazid = calcTAZ(ids, new Coord(Double.parseDouble(entries[6]),Double.parseDouble(entries[7])));

                Line l = new Line(tazid, Double.parseDouble(entries[8]),
                        (int)Double.parseDouble(entries[9]), (int)Double.parseDouble(entries[10]),
                        (int)Double.parseDouble(entries[11]), (int)Double.parseDouble(entries[12]));
                lines.add(l);
            }


            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TAZ calcTAZ(ArrayList<TAZ> ids, Coord loc) {
        int n = 0;
        int smallestDistIndex = -1;
        double dist = 100000000;

        while (n < ids.size() && ids.get(n) != null) {
            double currDist = Math.pow(ids.get(n).coord().getX() - loc.getX(), 2) +
                    Math.pow(ids.get(n).coord().getY() - loc.getY(), 2);
            currDist = Math.sqrt(currDist);
            if (dist > currDist){
                dist = currDist;
                smallestDistIndex = n;
            }
            n++;
        }
        return ids.get(smallestDistIndex);
    }

    public static Coord parseLocation(String toParse){
        if (toParse.length() == 0 || (!toParse.contains("POINT") && !toParse.contains("MULTILINESTRING")))
            return null;
        toParse = toParse.substring(toParse.lastIndexOf("(") + 1);
        if (toParse.contains(")")){
            toParse = toParse.substring(0, toParse.indexOf(")"));
        }
        String[] coords = toParse.split(", ");
        double aveX = 0, aveY = 0;

        for (int index = 0; index < coords.length; index++){
            if (coords[index].contains(" ")){
            aveX += Double.parseDouble(coords[index].substring(0, coords[index].indexOf(" ") + 1));
            aveY += Double.parseDouble(coords[index].substring(coords[index].lastIndexOf(" ")));
            }
        }
        aveX /= coords.length;
        aveY /= coords.length;

        return new Coord(aveX, aveY);
    }

    // create object with fields: long(x), lat(y)	shape_area	all_jobs	retail_jobs	persons	residential_units	tazId
// don't have to write out long and lat, as tazId contains its own
    public static ArrayList<TAZ> setTAZIDs(String TAZFile){
        ArrayList<TAZ> IDs = new ArrayList<TAZ>();

        try {
            File file = new File(TAZFile);

            BufferedReader br = new BufferedReader(new FileReader(file));

            String st = br.readLine();
            while ((st = br.readLine()) != null){
                String[] entries = st.split(",");
                IDs.add(new TAZ(Id.create(entries[0], TAZ.class),
                        new Coord(Double.parseDouble(entries[1]), Double.parseDouble(entries[2]))));
            }
        } catch (IOException e) {

        }

        return IDs;

    }
}
