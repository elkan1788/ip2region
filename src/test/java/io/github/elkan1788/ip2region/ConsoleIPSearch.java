package io.github.elkan1788.ip2region;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.elkan1788.ip2region.DataBlock;
import io.github.elkan1788.ip2region.IPSearcher;
import io.github.elkan1788.ip2region.IP2RegionUtils;

/**
 * project test script
 * 
 * @author chenxin(chenxin619315@gmail.com)
*/
public class ConsoleIPSearch {

    public static void main(String[] argv) {

        
        int algorithm = IPSearcher.BTREE_ALGORITHM;
        String algoName = "B-tree";
        if ( argv.length > 1 ) {
            if ( argv[1].equalsIgnoreCase("binary")) {
                algoName  = "Binary"; 
                algorithm = IPSearcher.BINARY_ALGORITHM;
            } else if ( argv[1].equalsIgnoreCase("memory") ) {
                algoName  = "Memory";
                algorithm = IPSearcher.MEMORY_ALGORITYM;
            }
        }
        
        try {
            System.out.println("initializing "+algoName+" ... ");
            IPSearcher searcher = new IPSearcher();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            //define the method
            Method method = null;
            switch ( algorithm )  {
            case IPSearcher.BTREE_ALGORITHM:
                method = searcher.getClass().getMethod("btreeSearch", String.class);
                break;
            case IPSearcher.BINARY_ALGORITHM:
                method = searcher.getClass().getMethod("binarySearch", String.class);
                break;
            case IPSearcher.MEMORY_ALGORITYM:
                method = searcher.getClass().getMethod("memorySearch", String.class);
                break;
            }
            
            System.out.println("+----------------------------------+");
            System.out.println("| ip2region test shell             |");
            System.out.println("| Author: chenxin619315@gmail.com  |");
            System.out.println("| Type 'quit' to exit program      |");
            System.out.println("+----------------------------------+");
            
            double sTime = 0, cTime = 0;
            String line = null;
            DataBlock dataBlock = null;
            while ( true ) {
                System.out.print("ip2region>> ");
                line = reader.readLine().trim();
                if ( line.length() < 2 ) continue;
                if ( line.equalsIgnoreCase("quit") ) break;
                if ( IP2RegionUtils.isIpAddress(line) == false ) {
                    System.out.println("Error: Invalid ip address");
                    continue;
                }
                
                sTime = System.nanoTime();
                dataBlock = (DataBlock) method.invoke(searcher, line);
                cTime = (System.nanoTime() - sTime) / 1000000;
                System.out.printf("%s \nSearch finish in %.5f million seconds\n", dataBlock, cTime);
            }
            
            reader.close();
            searcher.close();
            System.out.println("+--Bye");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
