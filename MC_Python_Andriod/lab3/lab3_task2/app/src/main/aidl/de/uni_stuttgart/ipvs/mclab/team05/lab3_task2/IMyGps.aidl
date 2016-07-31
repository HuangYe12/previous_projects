// IMyGps.aidl
package de.uni_stuttgart.ipvs.mclab.team05.lab3_task2;

// Declare any non-default types here with import statements

interface IMyGps {

    double getLatitude();
    double getLongitude();
    double getDistance();
    double getAverageSpeed();

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);
}
