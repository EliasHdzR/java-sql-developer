package edu.upvictoria.poo;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class sortArrayTest extends TestCase {

    public void testSortArray() {
        ArrayList<String> list = new ArrayList<>(List.of("9","6","4","3","3","2","1","0"));
        ArrayList<String> randList = new ArrayList<>(List.of("0","3","4","6","3","1","9","2"));
        assertEquals(list, Function.sortArray(randList));
    }

    public void testSortArray2() {
        ArrayList<Integer> randlist = new ArrayList<>(List.of(59, 47, 34, 88, 19, 76, 95, 42, 54, 92, 72, 61, 18,
                22, 32, 98, 100, 66, 37, 59, 79, 16, 68, 2, 1, 44, 79, 55, 38, 38, 68, 18, 24, 25, 29, 65, 66, 25, 5,
                47, 47, 37, 2, 66, 4, 46, 17, 33, 70, 78, 7, 68, 39, 58, 23, 11, 41, 67, 1, 68, 24, 13, 90, 51, 44,
                70, 78, 4, 83, 61, 0, 23, 32, 9, 8, 3, 56, 7, 89, 71, 3, 37, 4, 2, 33, 95, 70, 16, 65, 2, 99, 60,
                85, 32, 16, 51, 75, 37, 14, 31));
        ArrayList<Integer> list  = new ArrayList<>(List.of(100, 99, 98, 95, 95, 92, 90, 89, 88, 85, 83, 79, 79,
                78, 78, 76, 75, 72, 71, 70, 70, 70, 68, 68, 68, 68, 67, 66, 66, 66, 65, 65, 61, 61, 60, 59, 59, 58, 56,
                55, 54, 51, 51, 47, 47, 47, 46, 44, 44, 42, 41, 39, 38, 38, 37, 37, 37, 37, 34, 33, 33, 32, 32, 32, 31,
                29, 25, 25, 24, 24, 23, 23, 22, 19, 18, 18, 17, 16, 16, 16, 14, 13, 11, 9, 8, 7, 7, 5, 4, 4, 4, 3, 3, 2,
                2, 2, 2, 1, 1, 0));
        assertEquals(list, Function.sortArray(randlist));
    }
}