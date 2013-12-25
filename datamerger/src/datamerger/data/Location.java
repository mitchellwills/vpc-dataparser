package datamerger.data;

public class Location {
	public final double lon;
	public final double lat;

	public Location(double lon, double lat){
		this.lon = lon;
		this.lat = lat;
	}
	
	public double distanceTo(Location other){
		double dx = lon - other.lon;
		double dy = lat - other.lat;
		return Math.sqrt(dx*dx+dy*dy);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Location [x=" + lon + ", y=" + lat + "]";
	}
	
	
	
}
