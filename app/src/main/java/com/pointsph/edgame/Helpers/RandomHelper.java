package com.pointsph.edgame.Helpers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class RandomHelper {

    public static ArrayList<Integer> arl = new ArrayList<Integer>();

    public static void randomize(int n)
    {
        int[] range = new int[n];


        // Creating a object for Random class
        Random r = new Random();

        // Start from the last element and swap one by one. We don't
        // need to run for the first element that's why i > 0
        for (int i = n-1; i > 0; i--) {

            // Pick a random index from 0 to i
            int j = r.nextInt(i+1);

            // Swap arr[i] with the element at random index
            int temp = range[i];
            range[i] = range[j];
            range[j] = temp;
        }
        // Prints the random array
        Log.d("Numbers",Arrays.toString(range));
    }


    public static void rebaseListNumber()
    {
        arl = new ArrayList<Integer>();
    }

    public static int generateRandomNumber(int size)
    {
        Random rnd = new Random();
        int id = rnd.nextInt(size);
        if (arl.contains(id)) {
            return generateRandomNumber(size);
        }  else {
            arl.add(id);
        }

        if (size == arl.size()) {
            Log.d("Numbers","ArrayList reach the no of questions , we need to rebase");
            arl = new ArrayList<Integer>();
        }

        Log.d("Numbers",arl.toString());
        return id;

    }





}
