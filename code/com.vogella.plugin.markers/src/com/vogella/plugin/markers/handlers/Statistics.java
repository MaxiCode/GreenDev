package com.vogella.plugin.markers.handlers;

public class Statistics {
	 
    double mean;
    double standardDeviation;
    double variance;

    double sumOfSquares;
    double sum;
    
    double maxVal = 0d;

    int count;

    public void update(double value) {
       if (maxVal<value)maxVal=value;
       count++;
       this.sum += value;
       this.sumOfSquares += value * value;
       this.mean += (value - mean) / count;
       this.standardDeviation = Math.sqrt((count * sumOfSquares - sum * sum) / (count * (count - 1)));
       this.variance = this.standardDeviation * this.standardDeviation;
    }

    public double getMean() {
       return mean;
    }

    public void setMean(double mean) {
       this.mean = mean;
    }

    public double getStandardDeviation() {
       return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
       this.standardDeviation = standardDeviation;
    }
    
    public double getMaxVal() {
    	return maxVal;
    }

    @Override
    public String toString() {

       StringBuilder stringBuilder = new StringBuilder();
       stringBuilder.append("sum: ");
       stringBuilder.append(sum);
       stringBuilder.append("\n");

       stringBuilder.append("sum of x²: ");
       stringBuilder.append(sumOfSquares);
       stringBuilder.append("\n");

       stringBuilder.append("mean: ");
       stringBuilder.append(mean);
       stringBuilder.append("\n");

       stringBuilder.append("stddev: ");
       stringBuilder.append(standardDeviation);
       stringBuilder.append("\n");

       stringBuilder.append("variance: ");
       stringBuilder.append(variance);
       stringBuilder.append("\n");

       return stringBuilder.toString();
    }
 }
