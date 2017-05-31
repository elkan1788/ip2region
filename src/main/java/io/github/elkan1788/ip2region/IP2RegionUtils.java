package io.github.elkan1788.ip2region;

/**
 * Useful method collect methods
 * 
 * @author chenxin(chenxin619315@gmail.com)
*/
public class IP2RegionUtils {

    /**
     * write a int to a byte array
     * 
     * @param   bytes  byte array
     * @param   offset offset options
     * @param   v  value
    */
    public static void writeIntLong( byte[] bytes, int offset, long v ) {
        bytes[offset++] = (byte)((v >>  0) & 0xFF);
        bytes[offset++] = (byte)((v >>  8) & 0xFF);
        bytes[offset++] = (byte)((v >> 16) & 0xFF);
        bytes[offset  ] = (byte)((v >> 24) & 0xFF);
    }
    
    /**
     * get a int from a byte array start from the specifiled offset
     * 
     * @param    bytes  byte array
     * @param    offset offset options
     * @return long
    */
    public static long getIntLong( byte[] bytes, int offset) {
        return (
            ((bytes[offset++] & 0x000000FFL)) |
            ((bytes[offset++] <<  8) & 0x0000FF00L) |
            ((bytes[offset++] << 16) & 0x00FF0000L) |
            ((bytes[offset  ] << 24) & 0xFF000000L)
        );
    }
    
    /**
     * string ip to long ip
     * 
     * @param    ip ip address
     * @return    long ip long value
    */
    public static long ip2Long(String ip){
        String[] p = ip.split("\\.");
        if ( p.length != 4 ) return 0;
        
        int p1 = ((Integer.valueOf(p[0]) << 24) & 0xFF000000);
        int p2 = ((Integer.valueOf(p[1]) << 16) & 0x00FF0000);
        int p3 = ((Integer.valueOf(p[2]) <<  8) & 0x0000FF00);
        int p4 = ((Integer.valueOf(p[3]) <<  0) & 0x000000FF);
        
        return ((p1 | p2 | p3 | p4) & 0xFFFFFFFFL);
    }
    
    /**
     * int to ip string 
     * 
     * @param    ip ip long value
     * @return    string    ip address
    */
    public static String long2IP(long ip) {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append((ip >> 24) & 0xFF).append('.')
            .append((ip >> 16) & 0xFF).append('.')
            .append((ip >>  8) & 0xFF).append('.')
            .append((ip >>  0) & 0xFF);
        
        return sb.toString();
    }
    
    /**
     * check the validate of the specifeld ip address
     * 
     * @param    ip ip address
     * @return    boolean
    */
    public static boolean isIpAddress(String ip) {
        String[] p = ip.split("\\.");
        if (p.length != 4){
            return false;
        }

        for (String pp : p) {
            if (pp.length() > 3) {
                return false;
            }
            int val = Integer.valueOf(pp);
            if (val > 255) {
                return false;
            }
        }
        
        return true;
    }
}
