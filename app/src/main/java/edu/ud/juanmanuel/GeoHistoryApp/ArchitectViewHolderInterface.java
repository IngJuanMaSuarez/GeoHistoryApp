package edu.ud.juanmanuel.GeoHistoryApp;

import android.location.LocationListener;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;

public interface ArchitectViewHolderInterface {
	
	/**
	 * 50km = architectView's default cullingDistance, return this value in "getInitialCullingDistanceMeters()" to not change cullingDistance.
	 * 50km = el cullingDistance predeterminado de architectView, devuelve este valor en "getInitialCullingDistanceMeters ()" para no cambiar cullingDistance.
	 */
	public static final int CULLING_DISTANCE_DEFAULT_METERS = 50 * 1000;
	
	/**
	 * path to the architect-file (AR-Experience HTML) to launch
	 * @return
     * ruta al arquitecto-archivo (AR-Experiencia HTML) para lanzar
	 */
	public String getARchitectWorldPath();
	
	/**
	 * JS interface listener fired once e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' is called in JS
     * Interfaz JS listener disparado una vez, p. 'AR.platform.sendJSONObject ({foo: "bar", bar: 123})' se llama en JS
	 */
	ArchitectJavaScriptInterfaceListener getArchitectJavaScriptInterfaceListener();
	
	/**
	 * @return layout id of your layout.xml that holds an ARchitect View, e.g. R.layout.camview
     Layout id de diseño de su layout.xml que contiene una vista de ARchitect, p. R.layout.camview
	 */
	public int getContentViewId();
	
	/**
	 * @return Wikitude SDK license key, checkout www.wikitude.com for details
     * Clave de licencia de Wikitude SDK, consulta www.wikitude.com para más detalles
	 */
	public String getWikitudeSDKLicenseKey();
	
	/**
	 * @return layout-id of architectView, e.g. R.id.architectView
     * layout-id de architectView, p. R.id.architectView
	 */
	public int getArchitectViewId();

	/**
	 * 
	 * @return Implementation of a Location
     * Implementacion de Location
	 */
	public ILocationProvider getLocationProvider(final LocationListener locationListener);
	
	/**
	 * @return Implementation of Sensor-Accuracy-Listener. That way you can e.g. show prompt to calibrate compass
     * Implementación de Sensor-Accuracy-Listener. De esta manera puede, mostrar aviso para calibrar la brújula
	 */
	public SensorAccuracyChangeListener getSensorAccuracyListener();
	
	/**
	 * sets maximum distance to render places. In case your places are more than 50km away from the user you must adjust this value (compare 'AR.context.scene.cullingDistance').
	 * Return ArchitectViewHolder.CULLING_DISTANCE_DEFAULT_METERS to not change default behavior (50km range) or any positive float to set cullingDistance on architectView start.
	 * @return
     *
     * Establece la distancia máxima para los lugares. En caso de que sus lugares estén a más de 50 km del usuario, debe ajustar este valor (compare 'AR.context.scene.cullingDistance').
     * Devuelve ArchitectViewHolder.CULLING_DISTANCE_DEFAULT_METERS para no cambiar el comportamiento predeterminado (rango de 50km) o cualquier float positivo para establecer cullingDistance en architectView start.
	 */
	public float getInitialCullingDistanceMeters();
	
	/**
	 * Interface for a location-provider implementation
	 * feel free to implement your very own Location-Service, that handles GPS/Network positions more sophisticated but still takes care of
	 * life-cycle events
     *
     * Interface para una implementación de proveedores de ubicaciones
     * siéntete libre de implementar tu propia ubicación-servicio, que maneja las posiciones de GPS / Red más sofisticadas pero que aún se encarga de los
     * eventos del ciclo de vida
	 */
	public static interface ILocationProvider {

		/**
		 * Call when host-activity is resumed (usually within systems life-cycle method)
         * Llamar cuando se reanuda la actividad del host (normalmente dentro del método del ciclo de vida del sistema)
		 */
		public void onResume();

		/**
		 * Call when host-activity is paused (usually within systems life-cycle method)
         * Llamar cuando se suspende la actividad del host (normalmente dentro del método del ciclo de vida del sistema)
		 */
		public void onPause();

	}

	/**
	 * @return Implementation of ArchitectWorldLoadedListener. That way you know when a AR world is finished loading or when it failed to load.
     * Implementación de ArchitectWorldLoadedListener. De esa manera usted sabe cuando un mundo AR ha terminado de cargar o cuando no se pudo cargar.
     */
	ArchitectView.ArchitectWorldLoadedListener getWorldLoadedListener();
}