package io.github.elkan1788.ip2region;


import net.jcip.annotations.ThreadSafe;

import java.io.*;

/**
 * ip db searcher class (Not thread safe)
 * 
 * @author chenxin(chenxin619315@gmail.com)
 */
@ThreadSafe
public class IPSearcher {

    public static final int BTREE_ALGORITHM = 1;

    public static final int BINARY_ALGORITHM = 2;

    public static final int MEMORY_ALGORITYM = 3;

    /**
     * db file access handler
     */
    private RandomAccessFile raf = null;

    /**
     * header blocks buffer 
     */
    private long[] HeaderSip = null;

    private int[] HeaderPtr = null;

    private int headerLength;

    /**
     * super blocks info 
     */
    private long firstIndexPtr = 0;

    private long lastIndexPtr = 0;

    private int totalIndexBlocks = 0;

    /**
     * for memory mode
     * the original db binary string
     */
    private byte[] dbBinStr = null;

    /**
     * construct class
     *
     * @throws IPSearcherException a search exception
     */
    public IPSearcher() throws IPSearcherException {
        try {
            File IPDBFile = File.createTempFile("ip2region", ".db");
            InputStream is = IPSearcher.class.getResourceAsStream("/ip2region.db");
            byte[] tmpBuffer = new byte[is.available()];
            is.read(tmpBuffer);
            OutputStream os = new FileOutputStream(IPDBFile);
            os.write(tmpBuffer);
            raf = new RandomAccessFile(IPDBFile, "r");
            this.memorySearch("127.0.0.1");
        } catch (IOException e) {
            throw new IPSearcherException(e);
        }
    }

    /**
     * construct class
     * @param filePath IP database file path
     * @throws IPSearcherException a search exception
     */
    public IPSearcher(String filePath) throws IPSearcherException {
        try {
            raf = new RandomAccessFile(filePath, "r");
            this.memorySearch("127.0.0.1");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IPSearcherException(e);
        }
    }

    /**
     * get the region with a int ip address with memory binary search algorithm
     *
     * @param   ip  ip address
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException an IO read exception
     */
    public DataBlock memorySearch(long ip) throws IOException {
        int blen = IndexBlock.getIndexBlockLength();
        if (dbBinStr == null) {
            dbBinStr = new byte[(int) raf.length()];
            raf.seek(0L);
            raf.readFully(dbBinStr, 0, dbBinStr.length);

            //initialize the global vars
            firstIndexPtr = IP2RegionUtils.getIntLong(dbBinStr, 0);
            lastIndexPtr = IP2RegionUtils.getIntLong(dbBinStr, 4);
            totalIndexBlocks = (int) ((lastIndexPtr - firstIndexPtr) / blen) + 1;
        }

        //search the index blocks to define the data
        int l = 0, h = totalIndexBlocks;
        long sip, eip, dataptr = 0;
        while (l <= h) {
            int m = (l + h) >> 1;
            int p = (int) (firstIndexPtr + m * blen);

            sip = IP2RegionUtils.getIntLong(dbBinStr, p);
            if (ip < sip) {
                h = m - 1;
            } else {
                eip = IP2RegionUtils.getIntLong(dbBinStr, p + 4);
                if (ip > eip) {
                    l = m + 1;
                } else {
                    dataptr = IP2RegionUtils.getIntLong(dbBinStr, p + 8);
                    break;
                }
            }
        }

        //not matched
        if (dataptr == 0)
            return null;

        //get the data
        int dataLen = (int) ((dataptr >> 24) & 0xFF);
        int dataPtr = (int) ((dataptr & 0x00FFFFFF));
        int city_id = (int) IP2RegionUtils.getIntLong(dbBinStr, dataPtr);
        String region = new String(dbBinStr, dataPtr + 4, dataLen - 4, "UTF-8");

        return new DataBlock(city_id, region, dataPtr);
    }

    /**
     * get the region through the ip address with memory binary search algorithm
     *
     * @param   ip  ip address
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException an IO read exception
     */
    public DataBlock memorySearch(String ip) throws IOException {
        return memorySearch(IP2RegionUtils.ip2Long(ip));
    }

    /**
     * get by index ptr
     *

     * @param   ptr  an index
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException an IO read exception
     */
    public DataBlock getByIndexPtr(long ptr) throws IOException {
        raf.seek(ptr);
        byte[] buffer = new byte[12];
        raf.readFully(buffer, 0, buffer.length);
        //long startIp = IP2RegionUtils.getIntLong(buffer, 0);
        //long endIp = IP2RegionUtils.getIntLong(buffer, 4);
        long extra = IP2RegionUtils.getIntLong(buffer, 8);

        int dataLen = (int) ((extra >> 24) & 0xFF);
        int dataPtr = (int) ((extra & 0x00FFFFFF));

        raf.seek(dataPtr);
        byte[] data = new byte[dataLen];
        raf.readFully(data, 0, data.length);

        int city_id = (int) IP2RegionUtils.getIntLong(data, 0);
        String region = new String(data, 4, data.length - 4, "UTF-8");

        return new DataBlock(city_id, region, dataPtr);
    }

    /**
     * get the region with a int ip address with b-tree algorithm
     *
     * @param   ip  ip address
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException an IO read exception
     */
    public DataBlock btreeSearch(long ip) throws IOException {
        //check and load the header
        if (HeaderSip == null) {
            raf.seek(8L); //pass the super block
            //byte[] b = new byte[dbConfig.getTotalHeaderSize()];
            byte[] b = new byte[4096];
            raf.readFully(b, 0, b.length);

            //fill the header
            int len = b.length >> 3, idx = 0; //b.lenght / 8
            HeaderSip = new long[len];
            HeaderPtr = new int[len];
            long startIp, dataPtr;
            for (int i = 0; i < b.length; i += 8) {
                startIp = IP2RegionUtils.getIntLong(b, i);
                dataPtr = IP2RegionUtils.getIntLong(b, i + 4);
                if (dataPtr == 0)
                    break;

                HeaderSip[idx] = startIp;
                HeaderPtr[idx] = (int) dataPtr;
                idx++;
            }

            headerLength = idx;
        }

        //1. define the index block with the binary search
        if (ip == HeaderSip[0]) {
            return getByIndexPtr(HeaderPtr[0]);
        } else if (ip == HeaderSip[headerLength - 1]) {
            return getByIndexPtr(HeaderPtr[headerLength - 1]);
        }

        int l = 0, h = headerLength, sptr = 0, eptr = 0;
        while (l <= h) {
            int m = (l + h) >> 1;

            //perfect matched, just return it
            if (ip == HeaderSip[m]) {
                if (m > 0) {
                    sptr = HeaderPtr[m - 1];
                    eptr = HeaderPtr[m];
                } else {
                    sptr = HeaderPtr[m];
                    eptr = HeaderPtr[m + 1];
                }

                break;
            }

            //less then the middle value
            if (ip < HeaderSip[m]) {
                if (m == 0) {
                    sptr = HeaderPtr[m];
                    eptr = HeaderPtr[m + 1];
                    break;
                } else if (ip > HeaderSip[m - 1]) {
                    sptr = HeaderPtr[m - 1];
                    eptr = HeaderPtr[m];
                    break;
                }
                h = m - 1;
            } else {
                if (m == headerLength - 1) {
                    sptr = HeaderPtr[m - 1];
                    eptr = HeaderPtr[m];
                    break;
                } else if (ip <= HeaderSip[m + 1]) {
                    sptr = HeaderPtr[m];
                    eptr = HeaderPtr[m + 1];
                    break;
                }
                l = m + 1;
            }
        }

        //match nothing just stop it
        if (sptr == 0)
            return null;

        //2. search the index blocks to define the data
        int blockLen = eptr - sptr, blen = IndexBlock.getIndexBlockLength();
        byte[] iBuffer = new byte[blockLen + blen]; //include the right border block
        raf.seek(sptr);
        raf.readFully(iBuffer, 0, iBuffer.length);

        l = 0;
        h = blockLen / blen;
        long sip, eip, dp = 0;
        while (l <= h) {
            int m = (l + h) >> 1;
            int p = m * blen;
            sip = IP2RegionUtils.getIntLong(iBuffer, p);
            if (ip < sip) {
                h = m - 1;
            } else {
                eip = IP2RegionUtils.getIntLong(iBuffer, p + 4);
                if (ip > eip) {
                    l = m + 1;
                } else {
                    dp = IP2RegionUtils.getIntLong(iBuffer, p + 8);
                    break;
                }
            }
        }

        //not matched
        if (dp == 0)
            return null;

        //3. get the data
        int dataLen = (int) ((dp >> 24) & 0xFF);
        int dataPtr = (int) ((dp & 0x00FFFFFF));

        raf.seek(dataPtr);
        byte[] data = new byte[dataLen];
        raf.readFully(data, 0, data.length);

        int city_id = (int) IP2RegionUtils.getIntLong(data, 0);
        String region = new String(data, 4, data.length - 4, "UTF-8");

        return new DataBlock(city_id, region, dataPtr);
    }

    /**
     * get the region throught the ip address with b-tree search algorithm
     * 
     * @param   ip ip address
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException  an IO read exception
     */
    public DataBlock btreeSearch(String ip) throws IOException {
        return btreeSearch(IP2RegionUtils.ip2Long(ip));
    }

    /**
     * get the region with a int ip address with binary search algorithm
     * 
     * @param   ip  ip address
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException an IO read exception
     */
    public DataBlock binarySearch(long ip) throws IOException {
        int blen = IndexBlock.getIndexBlockLength();
        if (totalIndexBlocks == 0) {
            raf.seek(0L);
            byte[] superBytes = new byte[8];
            raf.readFully(superBytes, 0, superBytes.length);
            //initialize the global vars
            firstIndexPtr = IP2RegionUtils.getIntLong(superBytes, 0);
            lastIndexPtr = IP2RegionUtils.getIntLong(superBytes, 4);
            totalIndexBlocks = (int) ((lastIndexPtr - firstIndexPtr) / blen) + 1;
        }

        //search the index blocks to define the data
        int l = 0, h = totalIndexBlocks;
        byte[] buffer = new byte[blen];
        long sip, eip, dataptr = 0;
        while (l <= h) {
            int m = (l + h) >> 1;
            raf.seek(firstIndexPtr + m * blen); //set the file pointer
            raf.readFully(buffer, 0, buffer.length);
            sip = IP2RegionUtils.getIntLong(buffer, 0);
            if (ip < sip) {
                h = m - 1;
            } else {
                eip = IP2RegionUtils.getIntLong(buffer, 4);
                if (ip > eip) {
                    l = m + 1;
                } else {
                    dataptr = IP2RegionUtils.getIntLong(buffer, 8);
                    break;
                }
            }
        }

        //not matched
        if (dataptr == 0)
            return null;

        //get the data
        int dataLen = (int) ((dataptr >> 24) & 0xFF);
        int dataPtr = (int) ((dataptr & 0x00FFFFFF));

        raf.seek(dataPtr);
        byte[] data = new byte[dataLen];
        raf.readFully(data, 0, data.length);

        int city_id = (int) IP2RegionUtils.getIntLong(data, 0);
        String region = new String(data, 4, data.length - 4, "UTF-8");

        return new DataBlock(city_id, region, dataPtr);
    }

    /**
     * get the region throught the ip address with binary search algorithm
     *

     * @param   ip  ip address
     * @return  DataBlock   {@link DataBlock}
     * @throws  IOException an IO read exception
     */
    public DataBlock binarySearch(String ip) throws IOException {
        return binarySearch(IP2RegionUtils.ip2Long(ip));
    }

    /**
     * close the db 
     * @throws IOException an IO read exception
     */
    public void close() throws IOException {
        HeaderSip = null;
        HeaderPtr = null;
        dbBinStr = null;
        raf.close();
    }

}
