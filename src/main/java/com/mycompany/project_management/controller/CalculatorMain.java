package com.mycompany.project_management.controller;

public class CalculatorMain {
    public static void main(String[] args) {
        CalculatorController cal = new CalculatorController();
        Double result = cal.add(100.4,25.6);
        System.out.println(result);

    }
}
