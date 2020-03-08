package com.leon;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String a = "a";
        byte[] arr = a.getBytes();
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
//        System.out.println(a.getBytes());
    }
}
