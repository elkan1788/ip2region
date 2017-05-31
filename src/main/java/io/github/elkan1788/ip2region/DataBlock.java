package io.github.elkan1788.ip2region;

/**
 * data block class
 * 
 * @author	chenxin(chenxin619315@gmail.com)
 * @author 凡梦星尘(elkan1788@gmail.com)
*/
public class DataBlock {
	/**
	 * city id 
	*/
	private int cityId;
	
	/**
	 * region address
	*/
	private String region;

	/**
	 * region ptr in the db file
	*/
	private int dataPtr;
	/**
	 * region address info
	 */
	private RegionAddress regionAddr;
	
	/**
	 * construct method
	 * 
	 * @param  cityId  city id (not care)
	 * @param  region  region string
	 * @param  dataPtr data ptr
	*/
	public DataBlock(int cityId, String region, int dataPtr) {
		this.cityId = cityId;
		this.region  = region;
		this.regionAddr = new RegionAddress(this.region.split("\\|"));
		this.dataPtr = dataPtr;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public int getDataPtr() {
		return dataPtr;
	}

	public void setDataPtr(int dataPtr) {
		this.dataPtr = dataPtr;
	}

	public RegionAddress getRegionAddr() {
		return regionAddr;
	}

	public void setRegionAddr(RegionAddress regionAddr) {
		this.regionAddr = regionAddr;
	}

	@Override
	public String toString() {
		return cityId + "|" + region + "|" + dataPtr;
	}
}
