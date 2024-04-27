/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.utils;

/**
 *
 * @author Gabriel
 */
public class Vector3D {
    private double x;
    private double y;
    private double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getter methods
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    // Vector addition
    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    // Vector subtraction
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    // Scalar multiplication
    public Vector3D multiply(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    // Dot product
    public double dotProduct(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    // Cross product
    public Vector3D crossProduct(Vector3D other) {
        double newX = this.y * other.z - this.z * other.y;
        double newY = this.z * other.x - this.x * other.z;
        double newZ = this.x * other.y - this.y * other.x;
        return new Vector3D(newX, newY, newZ);
    }

    // Magnitude (length) of the vector
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    // Normalize the vector (return a unit vector in the same direction)
    public Vector3D normalize() {
        double mag = magnitude();
        return new Vector3D(x / mag, y / mag, z / mag);
    }

    // Override toString() for better representation
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}

