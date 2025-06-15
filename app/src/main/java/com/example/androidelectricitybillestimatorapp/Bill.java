package com.example.androidelectricitybillestimatorapp;

import java.io.Serializable; // Implement Serializable to pass objects between activities

public class Bill implements Serializable { // Serializable allows passing Bill objects via Intent

    private int id;
    private String month;
    private double kwhUsed;
    private int rebatePercentage;
    private double totalChargesRm;
    private double finalCostRm;

    // Constructor
    public Bill(int id, String month, double kwhUsed, int rebatePercentage,
                double totalChargesRm, double finalCostRm) {
        this.id = id;
        this.month = month;
        this.kwhUsed = kwhUsed;
        this.rebatePercentage = rebatePercentage;
        this.totalChargesRm = totalChargesRm;
        this.finalCostRm = finalCostRm;
    }

    // Getters for all properties
    public int getId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public double getKwhUsed() {
        return kwhUsed;
    }

    public int getRebatePercentage() {
        return rebatePercentage;
    }

    public double getTotalChargesRm() {
        return totalChargesRm;
    }

    public double getFinalCostRm() {
        return finalCostRm;
    }

    // Optional: Override toString() for easier debugging or simple display
    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", month='" + month + '\'' +
                ", kwhUsed=" + kwhUsed +
                ", rebatePercentage=" + rebatePercentage +
                ", totalChargesRm=" + totalChargesRm +
                ", finalCostRm=" + finalCostRm +
                '}';
    }
}