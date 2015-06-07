package tours_app_client;

public class GeoQuery {
	
	private String city;
	private String region;
	private String country;

	public GeoQuery(String city, String region, String country) {
		this.city = city;
		this.region = region;
		this.country = country;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getRegion() {
		return region;
	}
	
	public String getCountry() {
		return country;
	}

}
