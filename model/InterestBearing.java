package model;

public interface InterestBearing {
    long getBalance();
    double interestRate();

    default double yearlyInterest() {
        return (interestRate() / 100.0) * getBalance();
    }
}
