package edu.ud.juanmanuel.GeoHistoryApp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Sample implementation of a locationProvider, feel free to polish this very basic approach (compare http://goo.gl/pvkXV )
 * Ejemplo de implementación de un locationProvider, siéntase libre de pulir este enfoque muy básico (compare http://goo.gl/pvkXV)
 */
public class LocationProvider implements ArchitectViewHolderInterface.ILocationProvider {

	/** location listener called on each location update */
    /** location listener es llamado en cada actualización de la localización */
	private final LocationListener	locationListener;

	/** system's locationManager allowing access to GPS / Network position */
	/** LocationManager del sistema que permite el acceso a la posición del GPS / de la red */
	private final LocationManager	locationManager;

	/** location updates should fire approximately every second */
	/** las actualizaciones de ubicación deben disparar aproximadamente cada segundo */
	private static final int		LOCATION_UPDATE_MIN_TIME_GPS	= 1000;

	/** location updates should fire, even if last signal is same than current one (0m distance to last location is OK) */
	/** las actualizaciones de ubicación deben disparar, incluso si la última señal es la misma que la actual (la distancia de 0m hasta la última ubicación es correcta) */
	private static final int		LOCATION_UPDATE_DISTANCE_GPS	= 0;

	/** location updates should fire approximately every second */
	/** las actualizaciones de ubicación deben disparar aproximadamente cada segundo */
	private static final int		LOCATION_UPDATE_MIN_TIME_NW		= 1000;

	/** location updates should fire, even if last signal is same than current one (0m distance to last location is OK) */
    /** las actualizaciones de ubicación deben disparar, incluso si la última señal es la misma que la actual (la distancia de 0m hasta la última ubicación es correcta) */
	private static final int		LOCATION_UPDATE_DISTANCE_NW		= 0;

	/** to faster access location, even use 10 minute old locations on start-up */
	/** Para acceder rapido a la ubicacion, se usan 10 minutos de antiguedaden los lugares ejecutados */
	private static final int		LOCATION_OUTDATED_WHEN_OLDER_MS	= 1000 * 60 * 10;

	/** is gpsProvider and networkProvider enabled in system settings */
	/** es gpsProvider y networkProvider habilitado en la configuración del sistema */
	private boolean					gpsProviderEnabled, networkProviderEnabled;

	/** the context in which we're running */
	/** el contexto en el que estamos corriendo */
	private final Context			context;


	public LocationProvider( final Context context, LocationListener locationListener ) {
		super();
		this.locationManager = (LocationManager)context.getSystemService( Context.LOCATION_SERVICE );
		this.locationListener = locationListener;
		this.context = context;
		this.gpsProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
		this.networkProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
	}

	@Override
	public void onPause() {
		if ( this.locationListener != null && this.locationManager != null && (this.gpsProviderEnabled || this.networkProviderEnabled) ) {
			this.locationManager.removeUpdates( this.locationListener );
		}
	}

	@Override
	public void onResume() {
		if ( this.locationManager != null && this.locationListener != null ) {

			// check which providers are available are available
            // comprobar qué proveedores están disponibles
			this.gpsProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
			this.networkProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );

			/** is GPS provider enabled? */
			/** ¿Está habilitado el proveedor de GPS? */
			if ( this.gpsProviderEnabled ) {
				final Location lastKnownGPSLocation = this.locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
				if ( lastKnownGPSLocation != null && lastKnownGPSLocation.getTime() > System.currentTimeMillis() - LOCATION_OUTDATED_WHEN_OLDER_MS ) {
					locationListener.onLocationChanged( lastKnownGPSLocation );
				}
				if (locationManager.getProvider(LocationManager.GPS_PROVIDER)!=null) {
					this.locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, LOCATION_UPDATE_MIN_TIME_GPS, LOCATION_UPDATE_DISTANCE_GPS, this.locationListener );
				}
			}

			/** is Network / WiFi positioning provider available? */
			/** ¿Está disponible el proveedor de posicionamiento de red / WiFi? */
			if ( this.networkProviderEnabled ) {
				final Location lastKnownNWLocation = this.locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
				if ( lastKnownNWLocation != null && lastKnownNWLocation.getTime() > System.currentTimeMillis() - LOCATION_OUTDATED_WHEN_OLDER_MS ) {
					locationListener.onLocationChanged( lastKnownNWLocation );
				}
				if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER)!=null) {
					this.locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_TIME_NW, LOCATION_UPDATE_DISTANCE_NW, this.locationListener );
				}
			}

			/** user didn't check a single positioning in the location settings, recommended: handle this event properly in your app, e.g. forward user directly to location-settings, new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS ) */
			/** El usuario no ha comprobado un solo posicionamiento en la configuración de la ubicación, se recomienda: manejar este evento correctamente en su aplicación, reenvíe el usuario directamente a los ajustes de ubicación, nuevo Intent (Settings.ACTION_LOCATION_SOURCE_SETTINGS) */
			if ( !this.gpsProviderEnabled || !this.networkProviderEnabled ) {
				Toast.makeText( this.context, "Please enable GPS and Network positioning in your Settings ", Toast.LENGTH_LONG ).show();
			}
		}
	}
}