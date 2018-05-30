package beam.utils.scripts.parking;
import beam.agentsim.infrastructure.TAZ;


public class Line {
    private int allJobs, retailJobs, persons, resUnits;
    private double shapeArea;
    private TAZ tID;

    public Line(TAZ tazID, double area, int aJobs,
                int rJobs, int p, int rUnits){

        tID = tazID;
        shapeArea = area;
        allJobs = aJobs;
        retailJobs = rJobs;
        persons = p;
        resUnits = rUnits;
    }

    public TAZ gettID(){ return tID; }


    public void addLines(Line toAdd){

        shapeArea += toAdd.getShapeArea();
        allJobs += toAdd.getAllJobs();
        retailJobs += toAdd.getRetailJobs();
        persons += toAdd.getPersons();
        resUnits += toAdd.getResUnits();
    }

    public double getShapeArea(){
        return shapeArea;
    }

    public int getAllJobs(){
        return allJobs;
    }

    public int getRetailJobs(){
        return retailJobs;
    }

    public int getPersons(){
        return persons;
    }

    public int getResUnits(){
        return resUnits;
    }

    public String toString(){
        return "" + tID.tazId() + "," + tID.coord().getX() + "," + tID.coord().getY() + "," + shapeArea + "," + allJobs + ","
                + retailJobs + "," + persons + "," + resUnits + "\n";
    }
}
