/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.utils;

/**
 *
 * @author Gabriel
 */
public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void update(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Getter methods
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Vector addition
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    // Vector subtraction
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    // Scalar multiplication
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    // Dot product
    public double dotProduct(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    // Magnitude (length) of the vector
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    // Normalize the vector (return a unit vector in the same direction)
    public Vector2D normalize() {
        double mag = magnitude();
        return new Vector2D(x / mag, y / mag);
    }

    // Check if two vectors are equal
    public boolean equals(Vector2D other) {
        return this.x == other.x && this.y == other.y;
    }

    // Override toString() for better representation
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

