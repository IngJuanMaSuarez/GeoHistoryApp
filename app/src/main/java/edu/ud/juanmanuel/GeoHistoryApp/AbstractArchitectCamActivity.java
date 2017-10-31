package edu.ud.juanmanuel.GeoHistoryApp;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.services.camera.CameraLifecycleListener;
import com.wikitude.common.camera.CameraSettings;

/**
 * Abstract activity which handles live-cycle events.
 * Feel free to extend from this activity when setting up your own AR-Activity
 * Actividad abstracta que maneja en vivo los eventos de ciclo.
 * Siéntase libre de extender esta actividad al configurar su propia AR-Actividad
 */
public abstract class AbstractArchitectCamActivity extends Activity implements ArchitectViewHolderInterface{

	/**
	 * Holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
     * Tiene el Wikitude SDK AR-View, aquí es donde la cámara, los marcadores, la brújula, los modelos 3D, etc. se representan
	 */
	protected ArchitectView					architectView;

	/**
	 * Sensor accuracy listener in case you want to display calibration hints
     * Detector de precisión del sensor en caso de que desee mostrar indicaciones de calibración
	 */
	protected SensorAccuracyChangeListener	sensorAccuracyListener;

	/**
	 * Last known location of the user, used internally for content-loading after user location was fetched
     * Ùltima ubicación conocida del usuario, utilizada internamente para la carga de contenido después de que la ubicación del usuario se obtuvo
	 */
	protected Location 						lastKnownLocaton;

	/**
	 * Sample location strategy, you may implement a more sophisticated approach too
     * Ejemplo, puede implementar un enfoque más sofisticado también
	 */
	protected ILocationProvider				locationProvider;

	/**
	 * Location listener receives location updates and must forward them to the architectView
     * Location Listener recibe actualizaciones de ubicación y debe reenviarlas al architectView
	 */
	protected LocationListener 				locationListener;

	/**
	 * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
	 */
	protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

	/**
	 * worldLoadedListener receives calls when the AR world is finished loading or when it failed to laod.
     * worldLoadedListener recibe llamadas cuando el mundo AR termina de cargar o cuando falló.
	 */
	protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;

	protected JSONArray poiData;

	protected boolean isLoading = false;

	/**
     * Called when the activity is first created.
     * Se llama cuando se crea por primera vez la actividad
     */
	@SuppressLint("NewApi")
	@Override
	public void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		/* Pressing volume up/down should cause music volume changes */
		/* Presionando el volumen hacia arriba/abajo debería causar cambios en el volumen de la música */
		this.setVolumeControlStream( AudioManager.STREAM_MUSIC );

		/* Set samples content view */
		/* Conjunto de vistas de ejemplo */
		this.setContentView( this.getContentViewId() );

		this.setTitle( this.getActivityTitle() );

		/*
		 *	this enables remote debugging of a WebView on Android 4.4+ when debugging = true in AndroidManifest.xml
		 *	If you get a compile time error here, ensure to have SDK 19+ used in your ADT/Eclipse.
		 *	You may even delete this block in case you don't need remote debugging or don't have an Android 4.4+ device in place.
		 *	Details: https://developers.google.com/chrome-developer-tools/docs/remote-debugging
		 *
		 *	Esto permite la depuración remota de un WebView en Android 4.4+ al depurar = true en AndroidManifest.xml
		 *	Si obtiene un error de tiempo de compilación aquí, asegúrese de tener SDK 19+ utilizado en su ADT/Eclipse.
		 *	Incluso puede eliminar este bloque en caso de que no necesite depuración remota o no tenga un dispositivo Android 4.4 o posterior instalado.
		 *	Detalles: https://developers.google.com/chrome-developer-tools/docs/remote-debugging
		 */
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		    if ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) ) {
		        WebView.setWebContentsDebuggingEnabled(true);
		    }
		}

		/* Set AR-view for life-cycle notifications etc. */
        /* Establecer AR-view para el ciclo de vida etc*/
        // la ruta del architectView en nuestro XML
		this.architectView = (ArchitectView)this.findViewById( this.getArchitectViewId()  );

		/* Pass SDK key if you have one, this one is only valid for this package identifier and
		must not be used somewhere else */

		/* Pasar SDK key si tiene uno, este sólo es válido para este identificador de paquete y no
		debe utilizarse en otro lugar */
		final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
		config.setLicenseKey(this.getWikitudeSDKLicenseKey());
		config.setFeatures(this.getFeatures());
		config.setCameraPosition(this.getCameraPosition());
		config.setCameraResolution(this.getCameraResolution());
		config.setCamera2Enabled(this.getCamera2Enabled());

		this.architectView.setCameraLifecycleListener(getCameraLifecycleListener());
		try {
			/* First mandatory life-cycle notification */
			/* Primer aviso de ciclo de vida obligatorio */
			this.architectView.onCreate( config );
		} catch (RuntimeException rex) {
			this.architectView = null;
			Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
			Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
		}

		// Set world loaded listener if implemented
        // Establece el loaded listener si se implementa
		this.worldLoadedListener = this.getWorldLoadedListener();

		// Register valid world loaded listener in architectView, ensure this is set before
        // content is loaded to not miss any event

        // Registra el loaded listener en architectView, se asegura de esto y se fija antes de
        // que se cargue el contenido para no faltar cualquier acontecimiento
		if (this.worldLoadedListener != null && this.architectView != null) {
			this.architectView.registerWorldLoadedListener(worldLoadedListener);
		}

		// Set accuracy listener if implemented, you may e.g. show calibration prompt for
        // compass using this listener

        // Establecer un detector de precisión si se implementa, puede, por ejemplo, muestra
        // el mensaje de calibración para la brújula usando este listener
		this.sensorAccuracyListener = this.getSensorAccuracyListener();

		// Set JS interface listener, any calls made in JS like 'AR.platform.sendJSONObject({foo:"bar", bar:123})'
        // is forwarded to this listener, use this to interact between JS and native Android activity/fragment

        // Establece JS interface listener, cualquier llamada hecha en JS como 'AR.platform.sendJSONObject ({foo:"bar", bar:123})'
        // se reenvía a este listener, usa esto para interactuar entre JS y la actividad/fragmento
		this.mArchitectJavaScriptInterfaceListener = this.getArchitectJavaScriptInterfaceListener();

		// Set JS interface listener in architectView, ensure this is set before content is loaded to not miss any event
        // Establece el listener de la interfaz JS en architectView, asegúrese de que esté configurado antes de cargar el contenido para no perder ningún evento
		if (this.mArchitectJavaScriptInterfaceListener != null && this.architectView != null) {
			this.architectView.addArchitectJavaScriptInterfaceListener(mArchitectJavaScriptInterfaceListener);
		}

		if (true) {
			// Listener passed over to locationProvider, any location update is handled here
            // Listener pasa a locationProvider, cualquier actualización de ubicación se maneja aquí
			this.locationListener = new LocationListener() {

				@Override
				public void onStatusChanged( String provider, int status, Bundle extras ) {
				}

				@Override
				public void onProviderEnabled( String provider ) {
				}

				@Override
				public void onProviderDisabled( String provider ) {
				}

				@Override
				public void onLocationChanged( final Location location ) {
					// Forward location updates fired by LocationProvider to architectView, you can set lat/lon from any location-strategy
                    // Adelantan las actualizaciones de location disparadas por LocationProvider a architectView, puede configurar lat/lon desde cualquier estrategia de ubicación
					if (location!=null) {
					// Sore last location as member, in case it is needed somewhere (in e.g. your adjusted project)
                    // Sore última ubicación como miembro, en caso de que sea necesario en algún lugar (por ejemplo, en su proyecto ajustado)
						AbstractArchitectCamActivity.this.lastKnownLocaton = location;
						if ( AbstractArchitectCamActivity.this.architectView != null ) {
							// Check if location has altitude at certain accuracy level & call right architect method (the one with altitude information)
                            // Comprueba si la ubicación tiene altitud en cierto nivel de precisión y llama al método architect (el que tiene información de altitud)
							if ( location.hasAltitude() && location.hasAccuracy() && location.getAccuracy()<7) {
								AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
							} else {
								AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
							}
						}
					}
				}
			};

			// locationProvider used to fetch user position
            // locationProvider utilizado para obtener la posición del usuario
			this.locationProvider = getLocationProvider( this.locationListener );
		} else {
			this.locationProvider = null;
			this.locationListener = null;
		}
	}

    /*
	    CICLO DE VIDA DE LA APLICACION
	*/

    @Override
    protected void onResume() {
        super.onResume();

        // Call mandatory live-cycle method of architectView
        // Llamar al método de ciclo de vida de architectView
        if ( this.architectView != null ) {
            this.architectView.onResume();

            // Register accuracy listener in architectView, if set
            // Detector de exactitud de registros en architectView, si está configurado
            if (this.sensorAccuracyListener!=null) {
                this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }

        // Tell locationProvider to resume, usually location is then (again) fetched, so the GPS indicator appears in status bar
        // Dice a locationProvider que reanude, normalmente la ubicación es entonces (de nuevo) buscada, por lo que el indicador GPS aparece en la barra de estado
        if ( this.locationProvider != null ) {
            this.locationProvider.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Call mandatory live-cycle method of architectView
        // Llamar al método de ciclo de vida de architectView
        if ( this.architectView != null ) {
            this.architectView.onPause();

            // Unregister accuracy listener in architectView, if set
            // Anular el registro de la precisión en architectView, si se establece
            if ( this.sensorAccuracyListener != null ) {
                this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }

        // Tell locationProvider to pause, usually location is then no longer fetched, so the GPS indicator disappears in status bar
        // Dice a locationProvider que pausa, por lo general la ubicación ya no se recupera, por lo que el indicador GPS desaparece en la barra de estado
        if ( this.locationProvider != null ) {
            this.locationProvider.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Call mandatory live-cycle method of architectView
        // Llamar al método de ciclo de vida de architectView
        if ( this.architectView != null ) {
            this.architectView.clearCache();
            this.architectView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // Call mandatory live-cycle method of architectView
        // Llamar al método de ciclo de vida de architectView
        if ( this.architectView != null ) {
            this.architectView.onLowMemory();
        }
    }

    @Override
    protected void onPostCreate( final Bundle savedInstanceState ) {
        super.onPostCreate( savedInstanceState );

        // Call mandatory live-cycle method of architectView
        // Llamar al método de ciclo de vida de architectView
        if ( this.architectView != null ) {
            this.architectView.onPostCreate();

            try {

                // Load content via url in architectView, ensure '<script src="architect://architect.js"></script>'
                // is part of this HTML file, have a look at wikitude.com's developer section for API references

                // Cargamos el ARchitect worlds (codigo web: HTML CSS JavaScript), asegúrese de que '<script src = "architect: //architect.js"> </ script>'
                // es parte de este archivo HTML, eche un vistazo a la sección de desarrolladores de wikitude.com para referencias de API
                this.architectView.load( this.getARchitectWorldPath() );

                if (this.getInitialCullingDistanceMeters() != ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS) {
                    // set the culling distance - meaning: the maximum distance to render geo-content
                    // establece la distancia - lo que significa: la distancia máxima para renderizar geo-contenido
                    this.architectView.setCullingDistance( this.getInitialCullingDistanceMeters() );
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

	protected abstract CameraSettings.CameraPosition getCameraPosition();

	private int getFeatures() {
		int features = (hasGeo() ? ArchitectStartupConfiguration.Features.Geo : 0) |
				(hasIR() ? ArchitectStartupConfiguration.Features.ImageTracking : 0) |
				(hasInstant() ? ArchitectStartupConfiguration.Features.InstantTracking : 0) ;
		return features;
	}

	protected abstract boolean hasGeo();
	protected abstract boolean hasIR();
	protected abstract boolean hasInstant();

	protected CameraLifecycleListener getCameraLifecycleListener() {
		return null;
	}

	protected CameraSettings.CameraResolution getCameraResolution(){
		return CameraSettings.CameraResolution.SD_640x480;
	}
	protected boolean getCamera2Enabled() {
		return false;
	}

	/**
	 * Title shown in activity
     * Titulo mostrado en la actividad
	 * @return
	 */
	public abstract String getActivityTitle();

	/**
	 * Path to the architect-file (AR-Experience HTML) to launch
     * Ruta al archivo architect (AR-Experiencia HTML) para lanzar
	 * @return
	 */
	@Override
	public abstract String getARchitectWorldPath();

	/**
	 * JS interface listener fired once e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' is called in JS
     * JS interface listener disparado una vez, 'AR.platform.sendJSONObject ({foo: "bar", bar: 123})' se llama en JS
	 */
	@Override
	public abstract ArchitectJavaScriptInterfaceListener getArchitectJavaScriptInterfaceListener();

	/**
	 * @return layout id of your layout.xml that holds an ARchitect View, e.g. R.layout.camview
     * layout id de diseño de su layout.xml que contiene una vista de ARchitect, p. R.layout.camview
	 */
	@Override
	public abstract int getContentViewId();

	/**
	 * @return Wikitude SDK license key, checkout www.wikitude.com for details
     * Clave de licencia de Wikile SDK, consulta www.wikitude.com para más detalles
	 */
	@Override
	public abstract String getWikitudeSDKLicenseKey();

	/**
	 * @return layout-id of architectView, e.g. R.id.architectView
     * layout-id de architectView, p. R.id.architectView
	 */
	@Override
	public abstract int getArchitectViewId();

	/**
	 *
	 * @return Implementation of a Location
     * Implementación de una ubicación
	 */
	@Override
	public abstract ILocationProvider getLocationProvider(final LocationListener locationListener);

	/**
	 * @return Implementation of Sensor-Accuracy-Listener. That way you can e.g. show prompt to calibrate compass
     * Implementación de Sensor-Accuracy-Listener. De esta manera puede mostrar aviso para calibrar la brújula
	 */
	@Override
	public abstract ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener();

	/**
	 * @return Implementation of ArchitectWorldLoadedListener. That way you know when a AR world is finished loading or when it failed to load.
     * Implementación de ArchitectWorldLoadedListener. De esa manera usted sabe cuando un mundo AR ha terminado de cargar o cuando no se pudo cargar.
	 */
	@Override
	public abstract ArchitectView.ArchitectWorldLoadedListener getWorldLoadedListener();

	/**
	 * helper to check if video-drawables are supported by this device. recommended to check before launching ARchitect Worlds with videodrawables
	 * @return true if AR.VideoDrawables are supported, false if fallback rendering would apply (= show video fullscreen)
     *
     * Asistente para comprobar si el dispositivo admite dispositivos de vídeo. Se recomienda comprobar antes de lanzar ARchitect Worlds con videodrawables
     * true si se soporta AR.VideoDrawables, false si se aplica la representación de fallback (= mostrar video a pantalla completa)
	 */
	public static final boolean isVideoDrawablesSupported() {
		String extensions = GLES20.glGetString( GLES20.GL_EXTENSIONS );
		return extensions != null && extensions.contains( "GL_OES_EGL_image_external" );
	}

	protected void injectData() {
		if (!isLoading) {
			final Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					isLoading = true;

					final int WAIT_FOR_LOCATION_STEP_MS = 2000;

					while (lastKnownLocaton==null && !isFinishing()) {

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(AbstractArchitectCamActivity.this, R.string.location_fetching, Toast.LENGTH_SHORT).show();
							}
						});

						try {
							Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
						} catch (InterruptedException e) {
							break;
						}
					}

					if (lastKnownLocaton!=null && !isFinishing()) {
						// TODO: you may replace this dummy implementation and instead load POI information e.g. from your database
						poiData = getPoiInformation(lastKnownLocaton, 20);
						callJavaScript("World.loadPoisFromJsonData", new String[] { poiData.toString() });
					}

					isLoading = false;
				}
			});
			t.start();
		}
	}

	/**
	 * Call JavaScript in architectView
     * Llama JavaScript en architectView
	 * @param methodName
	 * @param arguments
	 */
	private void callJavaScript(final String methodName, final String[] arguments) {
		final StringBuilder argumentsString = new StringBuilder("");
		for (int i= 0; i<arguments.length; i++) {
			argumentsString.append(arguments[i]);
			if (i<arguments.length-1) {
				argumentsString.append(", ");
			}
		}

		if (this.architectView!=null) {
			final String js = ( methodName + "( " + argumentsString.toString() + " );" );
			this.architectView.callJavascript(js);
		}
	}

	/**
	 * Loads poiInformation and returns them as JSONArray. Ensure attributeNames of JSON POIs are well known in JavaScript, so you can parse them easily
	 * @param userLocation the location of the user
	 * @param numberOfPlaces number of places to load (at max)
	 * @return POI information in JSONArray
     *
     * Carga poiInformation y las devuelve como JSONArray. Asegúrese de que los nombres de atributos de los POI JSON sean bien conocidos en JavaScript, por lo que puede analizarlos fácilmente
     * userLocation la ubicación del usuario
     * numberOfPlaces número de lugares a cargar (al máximo)
     * Información de POI en JSONArray
	 */
	public static JSONArray getPoiInformation(final Location userLocation, final int numberOfPlaces) {

		if (userLocation==null) {
			return null;
		}

		final JSONArray pois = new JSONArray();

		// Ensure these attributes are also used in JavaScript when extracting POI data
        // Asegura que estos atributos también se usen en JavaScript al extraer datos de POI
		final String ATTR_ID = "id";
		final String ATTR_NAME = "name";
		final String ATTR_DESCRIPTION = "description";
		final String ATTR_LATITUDE = "latitude";
		final String ATTR_LONGITUDE = "longitude";
		final String ATTR_ALTITUDE = "altitude";

		for (int i=1;i <= numberOfPlaces; i++) {
			final HashMap<String, String> poiInformation = new HashMap<String, String>();
			poiInformation.put(ATTR_ID, String.valueOf(i));
			poiInformation.put(ATTR_NAME, "POI#" + i);
			poiInformation.put(ATTR_DESCRIPTION, "This is the description of POI#" + i);
			double[] poiLocationLatLon = getRandomLatLonNearby(userLocation.getLatitude(), userLocation.getLongitude());
			poiInformation.put(ATTR_LATITUDE, String.valueOf(poiLocationLatLon[0]));
			poiInformation.put(ATTR_LONGITUDE, String.valueOf(poiLocationLatLon[1]));
			final float UNKNOWN_ALTITUDE = -32768f;  // equals "AR.CONST.UNKNOWN_ALTITUDE" in JavaScript (compare AR.GeoLocation specification)

			// Use "AR.CONST.UNKNOWN_ALTITUDE" to tell ARchitect that altitude of places should be
            // on user level. Be aware to handle altitude properly in locationManager in case you
            // use valid POI altitude value (e.g. pass altitude only if GPS accuracy is <7m).

            // Utilice "AR.CONST.UNKNOWN_ALTITUDE" para indicarle a ARchitect que la altitud de los
            // lugares debe estar al nivel del usuario. Tenga en cuenta que debe manejar
            // correctamente la altitud en locationManager en caso de que utilice un valor válido
            // de altitud POI (por ejemplo, pase la altitud sólo si la precisión del GPS es <7m).
			poiInformation.put(ATTR_ALTITUDE, String.valueOf(UNKNOWN_ALTITUDE));
			pois.put(new JSONObject(poiInformation));
		}

		return pois;
	}

	/**
	 * Helper for creation of dummy places.
     * @param lat center latitude
     * @param lon center longitude
     * @return lat/lon values in given position's vicinity
     *
     * Asistente para la creación de lugares ficticios.
     * lat latitud del punto
     * lon longitud del punto
     * lat/lon valores en la vecindad de la posición dada
	 */
	private static double[] getRandomLatLonNearby(final double lat, final double lon) {
		return new double[] { lat + Math.random()/5-0.1 , lon + Math.random()/5-0.1};
	}
}