package beam.analysis.plots;

import beam.agentsim.agents.RideHailSurgePricingManager;
import beam.agentsim.agents.SurgePriceBin;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import scala.collection.Iterator;
import scala.collection.mutable.ArrayBuffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphSurgePricing {

    // The keys of the outer map represents binNumber
    // The inner map consists of category index to number of occurrence for each category
    // The categories are defined as buckets for occurrences of prices form 0-1, 1-2

    private int iterationNumber = 0;

    private  Map<Double, Map<Integer, Integer>> transformedBins = new HashMap<>();
    private  int binSize;
    private  int numberOfTimeBins;
    private  String graphTitle = "Ride Hail Surge Price Level";
    private  String xAxisLabel = "timebin";
    private  String yAxisLabel = "price level";
    private  int noOfCategories = 0;
    private  Double categorySize = null;
    private   Double max = null;
    private   Double min = null;

    private   List<Double> _categoryKeys;


    private  double[] revenueDataSet;

    private  Set<String> tazIds = new TreeSet<>();

    private  Map<String, double[][]> tazDataset = new TreeMap<>();

    private  String graphImageFile = "";
    private  String surgePricingCsvFileName = "";
    private  String surgePricingAndRevenueWithTaz = "";
    private  String revenueGraphImageFile =  "";
    private  String revenueCsvFileName =  "";
    private RideHailSurgePricingManager surgePricingManager;


    public GraphSurgePricing(RideHailSurgePricingManager surgePricingManager){

        this.surgePricingManager = surgePricingManager;
        noOfCategories = this.surgePricingManager.numberOfCategories();
        iterationNumber = this.surgePricingManager.getIterationNumber();
        //iterationNumber = itNo;
        tazDataset.clear();
        transformedBins.clear();
        max = null;
        min = null;
        tazIds.clear();


        final int iNo = iterationNumber;
        graphImageFile = GraphsStatsAgentSimEventsListener.CONTROLLER_IO.getIterationFilename(iNo, "rideHailSurgePriceLevel.png");
        surgePricingCsvFileName = GraphsStatsAgentSimEventsListener.CONTROLLER_IO.getIterationFilename(iNo, "rideHailSurgePriceLevel.csv");
        surgePricingAndRevenueWithTaz = GraphsStatsAgentSimEventsListener.CONTROLLER_IO.getIterationFilename(iNo, "taz_rideHailSurgePriceLevel.csv");
        revenueGraphImageFile = GraphsStatsAgentSimEventsListener.CONTROLLER_IO.getIterationFilename(iNo, "rideHailRevenue.png");
        revenueCsvFileName = GraphsStatsAgentSimEventsListener.CONTROLLER_IO.getIterationFilename(iNo, "rideHailRevenue.csv");


        binSize = this.surgePricingManager.timeBinSize();
        numberOfTimeBins = this.surgePricingManager.numberOfTimeBins();

        revenueDataSet = new double[numberOfTimeBins];

    }

    public  void createGraphs(){

        processSurgePriceBinsMap(surgePricingManager);

        if(min != max) {

            calculateCateogorySize();
            List<String> categoriesKeys = getCategoriesKeys(transformedBins,true);
            double[][] dataset = getDataset(true);
            writePriceSurgeCsv(dataset, categoriesKeys, true);
            drawGraph(dataset, categoriesKeys, true);
        }

        List<String> categoriesKeys = getCategoriesKeys(transformedBins, false);
        double[][] dataset = getDataset(false);
        writePriceSurgeCsv(dataset, categoriesKeys, false);
        drawGraph(dataset, categoriesKeys, false);

        drawRevenueGraph(revenueDataSet);

        writeTazCsv(tazDataset);

        writeRevenueCsv(revenueDataSet);
    }

    public  List<String> getCategoriesKeys(Map<Double, Map<Integer, Integer>> transformedBins, boolean categorize){

        List<String> categoriesStrings = new ArrayList<>();

        if(!categorize) {
            List<Double> categoriesList = new ArrayList<>();
            categoriesList.addAll(transformedBins.keySet());
            Collections.sort(categoriesList);

//            categoriesStrings = categoriesList.stream().map(String::valueOf).collect(Collectors.toList());
            for (Double price : categoriesList) {
                categoriesStrings.add(price + "");
            }
        }else{

//            categoriesStrings = buildCategoryKeys().stream().map(String::valueOf).collect(Collectors.toList());
            for(Double key : buildCategoryKeys()){
                categoriesStrings.add(getRoundedNumber(key) + "");
            }

        }

        return categoriesStrings;
    }

    public  double[][] getDataset(boolean categorize){

        if(categorize) {

            Map<Integer, Map<Integer, Integer>> finalCategories = processTransformedCategories(transformedBins);
            return buildDatasetFromFinalCategories(finalCategories);
        }else{
            return buildDatasetFromTransformedCategories(transformedBins);
        }
    }

    public  void processSurgePriceBinsMap(RideHailSurgePricingManager surgePricingManager){

        scala.collection.immutable.Map<String, scala.collection.mutable.ArrayBuffer<SurgePriceBin>> surgePriceBinsMap = surgePricingManager.surgePriceBins();
        Iterator mapIter = surgePriceBinsMap.keysIterator();

        while(mapIter.hasNext()) {

            String key = mapIter.next().toString();
            tazIds.add(key);



            ArrayBuffer<SurgePriceBin> bins  = surgePriceBinsMap.get(key).get();
            Iterator iter = bins.iterator();

            double[][] _tazDataset = new double[2][numberOfTimeBins];

            for (int i = 0; iter.hasNext(); i++) {
                SurgePriceBin bin = (SurgePriceBin) iter.next();

                double price = bin.currentIterationSurgePriceLevel();
                double revenue = bin.currentIterationRevenue();

                _tazDataset[0][i] = price;
                _tazDataset[1][i] = revenue;



                processBin(i, bin);
            }

            tazDataset.put(key, _tazDataset);
        }
    }

    public  void processBin(int binNumber, SurgePriceBin surgePriceBin){

        double revenue = surgePriceBin.currentIterationRevenue();
        revenueDataSet[binNumber] += revenue;
        //

        Double price = surgePriceBin.currentIterationSurgePriceLevel();

        Double roundedPrice = getRoundedNumber(price);

        max = (max == null || max < roundedPrice) ? roundedPrice : max;
        min = (min == null || min > roundedPrice) ? roundedPrice : min;

        Map<Integer, Integer> data = transformedBins.get(roundedPrice);

        if(data == null){
            data = new HashMap<>();
            data.put(binNumber, 1);
        }else{

            Integer frequency = data.get(binNumber);
            if(frequency == null){
                data.put(binNumber, 1);
            }else{
                data.put(binNumber, frequency + 1);
            }
        }

        transformedBins.put(roundedPrice, data);
    }



    public  void calculateCateogorySize(){
        categorySize = (max - min)/noOfCategories;
    }

    public  List<Double> buildCategoryKeys(){

        List<Double> _categoryKeys = new ArrayList<>();

        double minPrice = min;
        for(int i=0; i < noOfCategories; i++){

            _categoryKeys.add(minPrice);
            minPrice = minPrice + (categorySize);
        }

        return _categoryKeys;
    }

    public  int getPriceCategory(double price){

        int catIdxFound = -1;

        double startPrice = min;
        for(int i=0; i<noOfCategories; i++){

            double minPrice = startPrice;
            double maxPrice = minPrice + (categorySize);

            if(price >= minPrice && price <= maxPrice ){
                catIdxFound = i;
                break;
            }else{
                startPrice = maxPrice;
            }
        }

        return catIdxFound;
    }


    public  Map<Integer, Map<Integer, Integer>> processTransformedCategories(Map<Double, Map<Integer, Integer>> transformedBins){

        // determine the category based on key,
        // copy data from transformedBins to the final categories collection
        // if for that category we dont have data of bins just copy it
        // otherwise add it

        Map<Integer, Map<Integer, Integer>> finalCategories = new HashMap<>();


        for(double k : transformedBins.keySet()){
            int idx = getPriceCategory(k);

            Map<Integer, Integer> sourceData = transformedBins.get(k);

            Map<Integer, Integer> data = finalCategories.get(idx);

            if(data == null){
                finalCategories.put(idx, sourceData);
            }else{

                for(int i=0; i<numberOfTimeBins; i++){

                    Integer sourceFrequency = sourceData.get(i);

                    Integer targetFrequencey = data.get(i);

                    if(sourceFrequency != null) {
                        if (targetFrequencey == null) {

                                data.put(i, sourceFrequency);
                        } else {
                                data.put(i, sourceFrequency + targetFrequencey);

                        }
                    }
                }



                finalCategories.put(idx, data);
            }


        }
        System.out.println("Done with final categories");
        return finalCategories;
    }

    private  double[][] buildDatasetFromFinalCategories(Map<Integer, Map<Integer, Integer>> finalCategories) {

        double[][] dataset = new double[noOfCategories][numberOfTimeBins];


        for (int i =0 ; i<noOfCategories;i++) {

            Map<Integer, Integer> data = null;

            if(finalCategories.keySet().contains(i)){
                data = finalCategories.get(i);
            }

            if(data == null){
                double arr[] = new double[numberOfTimeBins];
                dataset[i] = arr;
            }else {

                double arr[] = new double[numberOfTimeBins];
                for (int j = 0; j < numberOfTimeBins; j++) {
                    Integer v = data.get(j);
                    if (v == null) {
                        arr[j] = 0;
                    } else {
                        arr[j] = v;
                    }
                }

                dataset[i] = arr;
            }
        }
        System.out.println("built the dataset");
        return dataset;
    }

    private  double[][] buildDatasetFromTransformedCategories(Map<Double, Map<Integer, Integer>> transformedCategories) {

        double[][] dataset = new double[transformedCategories.keySet().size()][numberOfTimeBins];

        List<Double> categoriesList = new ArrayList<>();
        categoriesList.addAll(transformedCategories.keySet());
        Collections.sort(categoriesList);

        int i=0;
        for (double key : categoriesList) {

            Map<Integer, Integer> data = transformedCategories.get(key);
            double arr[] = new double[numberOfTimeBins];
            for(int j=0; j<numberOfTimeBins;j++){
                Integer v = data.get(j);
                if(v == null){
                    arr[j] = 0;
                }else{
                    arr[j] = v;
                }
            }

            dataset[i++] = arr;
        }
        return dataset;
    }

    public  void drawGraph(double[][] _dataset, List<String> categoriesKeys, boolean categorize){

        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Categories ", "", _dataset);

        List<String> _categoriesKeys = new ArrayList<>();
        _categoriesKeys.addAll(categoriesKeys);
        int lastIndex = _categoriesKeys.size() - 1;
        String lastValue = _categoriesKeys.get(lastIndex);
        lastValue = lastValue + "-" + max;
        _categoriesKeys.set(lastIndex, lastValue);

        try {

            boolean legend = true;

            String fileName = graphImageFile;
            if(!categorize)
                fileName = graphImageFile.replace(".png", "_.png");

            final JFreeChart chart = GraphUtils.createStackedBarChartWithDefaultSettings(dataset,graphTitle,xAxisLabel,yAxisLabel,fileName,legend);
            CategoryPlot plot = chart.getCategoryPlot();



            GraphUtils.plotLegendItems(plot, _categoriesKeys, dataset.getRowCount());


            GraphUtils.saveJFreeChartAsPNG(chart, fileName, GraphsStatsAgentSimEventsListener.GRAPH_WIDTH, GraphsStatsAgentSimEventsListener.GRAPH_HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void drawRevenueGraph(double[] data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

        for(int i=0; i < data.length; i++){
            Double revenue = data[i];
            dataset.addValue(revenue, "revenue", "" + i);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Ride Hail Revenue",
                "timebin","revenue($)",
                dataset,
                PlotOrientation.VERTICAL,
                false,true,false);

        try {
            GraphUtils.saveJFreeChartAsPNG(chart, revenueGraphImageFile, GraphsStatsAgentSimEventsListener.GRAPH_WIDTH, GraphsStatsAgentSimEventsListener.GRAPH_HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  Double getRoundedNumber(Double number){
        return Math.round(number * 100.0) / 100.0;
    }

    public  void writePriceSurgeCsv(double[][] dataset, List<String> categoriesList, boolean categorize){


        String fileName = surgePricingCsvFileName;
        if(!categorize){
            fileName = surgePricingCsvFileName.replace(".csv", "_.csv");
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter( new File(fileName)));
            //BufferedWriter out = writer.getBufferedWriter();
            out.write("Categories");
            out.write(",");

            for(int i=0; i<dataset[0].length; i++){
                out.write("bin_" + i);
                out.write(",");
            }
            out.newLine();



            if(categorize) {
                double diff = min;
                if (categoriesList.size() > 1)
                    diff = getRoundedNumber(Math.abs(min - Double.parseDouble(categoriesList.get(1))));

                for (int j = 0; j < categoriesList.size(); j++) {
                    double category = Double.parseDouble(categoriesList.get(j));
                    String strFormat = "";
                    if (diff == category) {
                        strFormat = category + "-" + diff;
                    } else if (j + 1 == categoriesList.size()) {
                        strFormat = category + "-" + (category + diff);
                    } else {
                        strFormat = category + "-" + categoriesList.get(j + 1);
                    }
                    out.write(strFormat);
                    out.write(",");

                    for (int i = 0; i < dataset[j].length; i++) {
                        out.write(dataset[j][i] + "");
                        out.write(",");
                    }
                    out.newLine();
                }
            }else{

                for (int j = 0; j < categoriesList.size(); j++) {
                    double category = Double.parseDouble(categoriesList.get(j));

                    out.write(categoriesList.get(j));
                    out.write(",");

                    for (int i = 0; i < dataset[j].length; i++) {
                        out.write(dataset[j][i] + "");
                        out.write(",");
                    }
                    out.newLine();
                }
            }

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void writeTazCsv(Map<String, double[][]> dataset){

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(surgePricingAndRevenueWithTaz)));

            out.write("TazId");
            out.write(",");

            out.write("DataType");
            out.write(",");


            for(int i = 0; i < numberOfTimeBins; i++){
                out.write("bin_" + i);
                out.write(",");
            }
            out.newLine();



            for(String tazId : dataset.keySet()){
                double[][] data = dataset.get(tazId);

                double[] prices = data[0];
                double[] revenues = data[1];

                out.write(tazId);
                out.write(",");

                out.write("pricelevel");
                out.write(",");

                for(int i = 0; i< numberOfTimeBins; i++){
                    out.write(prices[i] + "");
                    out.write(",");
                }
                out.newLine();

                out.write(tazId);
                out.write(",");

                out.write("revenue");
                out.write(",");

                for(int i = 0; i< numberOfTimeBins; i++){
                    out.write(revenues[i] + "");
                    out.write(",");
                }
                out.newLine();
            }

            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void writeRevenueCsv(double[] revenueDataSet){

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(revenueCsvFileName)));

            for(int i = 0; i < numberOfTimeBins; i++){
                out.write("bin_" + i);
                out.write(",");
            }
            out.newLine();

            for(double revenue : revenueDataSet){
                out.write( revenue + "");
                out.write(",");
            }
            out.newLine();

            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
