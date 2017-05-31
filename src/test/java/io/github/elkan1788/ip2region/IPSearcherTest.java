package io.github.elkan1788.ip2region;

import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Test the DBSearch
 * @author  凡梦星尘(elkan1788@gmail.com)
 */
public class IPSearcherTest {

    private String strIP = "101.228.248.4";
    private long longIP = 1709504516;
    private IPSearcher searcher;

    @BeforeClass
    public void init() throws Exception {
        searcher = new IPSearcher();
    }

    @Test
    public void testMemorySearch() throws Exception {
        RegionAddress ra = searcher.memorySearch(strIP).getRegionAddr();
        assertCheck(ra);
    }

    @Test
    public void testMemorySearchByLong() throws Exception {
        RegionAddress ra = searcher.memorySearch(longIP).getRegionAddr();
        assertCheck(ra);
    }

    @Test
    public void testBtreeSearch() throws Exception {
        RegionAddress ra = searcher.btreeSearch(strIP).getRegionAddr();
        assertCheck(ra);
    }

    @Test
    public void testBtreeSearchByLong() throws Exception {
        RegionAddress ra = searcher.btreeSearch(longIP).getRegionAddr();
        assertCheck(ra);
    }

    @Test
    public void testBinarySearch() throws Exception {
        RegionAddress ra = searcher.binarySearch(strIP).getRegionAddr();
        assertCheck(ra);
    }

    @Test
    public void testBinarySearchByLong() throws Exception {
        RegionAddress ra = searcher.binarySearch(longIP).getRegionAddr();
        assertCheck(ra);
    }

    public void assertCheck(RegionAddress ra) {
        assertNotNull(ra);
        assertEquals(ra.getCountry(), "中国");
        assertEquals(ra.getProvince(), "上海市");
        assertEquals(ra.getCity(), "上海市");
        assertEquals(ra.getArea(), "华东");
        assertEquals(ra.getISP(), "电信");
    }

}