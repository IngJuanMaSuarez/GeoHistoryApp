package edu.ud.juanmanuel.GeoHistoryApp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;

import java.util.Arrays;

/**
 * This shows how to create a simple activity with a map, a marker on the map and a button.
 * Esto muestra como crear una actividad simple con un mapa, un marcador en el mapa y un boton
 */
public class Principal extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private Button btnAbrirRa;
    private PermissionManager mPermissionManager;

    /**
     * Request code for location permission request.
     * Código de solicitud de solicitud de permiso de ubicación.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * Bandera que indica si un permiso solicitado ha sido denegado después de regresar en
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Obtener el SupportMapFragment y ser notificado cuando el mapa está listo para ser utilizado.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPermissionManager = ArchitectView.getPermissionManager();
        btnAbrirRa = (Button) findViewById(R.id.btnAbrirRA);
        btnAbrirRa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] permissions =   new String[]{Manifest.permission.CAMERA};
                mPermissionManager.checkPermissions(Principal.this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {

                    //Si los permisos se dan adecuadamente por parte del usuario, se llama al metodo loadExample que carga la actividad de RA
                    @Override
                    public void permissionsGranted(int i) {
                        loadExample();
                    }

                    //Si los permisos no se dan muestra un mensaje informando al usuario
                    @Override
                    public void permissionsDenied(@NonNull String[] deniedPermissions) {
                        Toast.makeText(Principal.this, "GeoHistoryApp necesita los siguientes permisos para habilitar la experiencia de Realidad Aumentada: " + Arrays.toString(deniedPermissions), Toast.LENGTH_SHORT).show();
                    }

                    //Si los permisos se cambian despues de otorgarlos, muestra un mensaje informando al usuario y solicitandolos nuevamente
                    @Override
                    public void showPermissionRationale(final int requestCode, final String[] permissions) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Principal.this);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("GeoHistoryApp Permisos"); //Titulo de la ventana
                        alertBuilder.setMessage("GeoHistoryApp necesita los siguientes permisos para habilitar la experiencia de Realidad Aumentada: " + Arrays.toString(permissions)); //Mensaje de la ventana con el listado de permisos requeridos
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            //Solicita nuevamente permisos
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPermissionManager.positiveRationaleResult(requestCode, permissions);
                            }
                        });

                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                    }
                });
            }
        });
    }

    //Metodo que llama la actividad donde esta la realidad aumentada, MainActivity
    private void loadExample(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

     @Override
    public void onMapReady(GoogleMap map) {

         mMap = map;

         mMap.setOnMyLocationButtonClickListener(this);
         mMap.setOnMyLocationClickListener(this);
         enableMyLocation();

         // Add a marker
         // Agregar Marcadores

         LatLng Punto1 = new LatLng(4.598960750874818, -74.075488457862548);
         map.addMarker(new MarkerOptions().position(Punto1).title("Toma y Retoma del Palacio de Justicia"));

         LatLng Punto2 = new LatLng(4.60241111696342, -74.072341622055674);
         map.addMarker(new MarkerOptions().position(Punto2).title("Incendio Edificio de Avianca"));

         LatLng Punto3 = new LatLng(4.602427872297512, -74.063022148958311);
         map.addMarker(new MarkerOptions().position(Punto3).title("Robo de la Espada de Simón Bolivar"));

         LatLng Punto4 = new LatLng(4.60143426075101, -74.07336007389344);
         map.addMarker(new MarkerOptions().position(Punto4).title("Visita del Papa Pablo VI"));

         LatLng Punto5 = new LatLng(4.598263442394059, -74.075256003373454);
         map.addMarker(new MarkerOptions().position(Punto5).title("Casa del Florero"));

         LatLng Punto6 = new LatLng(4.598044083817242, -74.075345305871565);
         map.addMarker(new MarkerOptions().position(Punto6).title("Catedral Primada"));

         LatLng Punto7 = new LatLng(4.611312026654603, -74.069893866766748);
         map.addMarker(new MarkerOptions().position(Punto7).title("Calle 26 Cra 7"));

         LatLng Punto8 = new LatLng(4.594312767637836, -74.078039878519405);
         map.addMarker(new MarkerOptions().position(Punto8).title("Iglesia San Agustin"));

         LatLng Punto9 = new LatLng(4.601350350853769, -74.073434870286803);
         map.addMarker(new MarkerOptions().position(Punto9).title("Drogueria Granada"));

         LatLng Punto10 = new LatLng(4.600162479029661, -74.073452604405801);
         map.addMarker(new MarkerOptions().position(Punto10).title("Universidad del Rosario"));

         LatLng Punto11 = new LatLng(4.600863445223137, -74.071754155445831);
         map.addMarker(new MarkerOptions().position(Punto11).title("Hotel Regina"));

         LatLng Punto12 = new LatLng(4.598386382082285, -74.076490952739448);
         map.addMarker(new MarkerOptions().position(Punto12).title("Incendio de las Galerias de Arrubla"));

         LatLng Punto13 = new LatLng(4.597681809459496, -74.073911114985009);
         map.addMarker(new MarkerOptions().position(Punto13).title("Fondo de Cultura Económica"));

         LatLng Punto14 = new LatLng(4.601375376251742, -74.068916474359696);
         map.addMarker(new MarkerOptions().position(Punto14).title("Rio San Francisco"));

         LatLng Punto15 = new LatLng(4.602749989411618, -74.060950252777602);
         map.addMarker(new MarkerOptions().position(Punto15).title("Monserrate"));

         LatLng Punto16 = new LatLng(4.598115726909802, -74.076040119898252);
         map.addMarker(new MarkerOptions().position(Punto16).title("Plaza de Bolivar"));

         LatLng Punto17 = new LatLng(4.602099299667179, -74.078618262222122);
         map.addMarker(new MarkerOptions().position(Punto17).title("San Victorino"));

         LatLng Punto18 = new LatLng(4.602179403890773, -74.068512031191858);
         map.addMarker(new MarkerOptions().position(Punto18).title("Puente de Colón"));

         LatLng Punto19 = new LatLng(4.601987191262809, -74.072594078077714);
         map.addMarker(new MarkerOptions().position(Punto19).title("Parque Santander"));

         LatLng Punto20 = new LatLng(4.611260318649037, -74.068781404327524);
         map.addMarker(new MarkerOptions().position(Punto20).title("Parque de la Independencia"));

         LatLng Punto21 = new LatLng(4.606137185597832, -74.071313506043126);
         map.addMarker(new MarkerOptions().position(Punto21).title("Primera Casa de Teja"));

         LatLng Punto22 = new LatLng(4.6053868539403, -74.071555790527171);
         map.addMarker(new MarkerOptions().position(Punto22).title("Construccion Tranvia"));

         LatLng Punto23 = new LatLng(4.601888248595084, -74.073757910448577);
         map.addMarker(new MarkerOptions().position(Punto23).title("Tranvia"));

         LatLng Punto24 = new LatLng(4.609644985991324, -74.067878689320864);
         map.addMarker(new MarkerOptions().position(Punto24).title("Vista Monserrate"));

         LatLng Punto25 = new LatLng(4.612967598242026, -74.068071930037618);
         map.addMarker(new MarkerOptions().position(Punto25).title("Marcha del Silencio"));

         LatLng Punto26 = new LatLng(4.606180476938452, -74.074385011358331);
         map.addMarker(new MarkerOptions().position(Punto26).title("Trancon sobre la carrera 10"));

         LatLng Punto27 = new LatLng(4.601407541051215, -74.074122147929543);
         map.addMarker(new MarkerOptions().position(Punto27).title("Un Mar de Gente"));

         LatLng Punto28 = new LatLng(4.595532239651766, -74.076910444488703);
         map.addMarker(new MarkerOptions().position(Punto28).title("Calle Real"));

         LatLng Punto29 = new LatLng(4.597078729436239, -74.076461228459834);
         map.addMarker(new MarkerOptions().position(Punto29).title("Firma de la Constitucion de 1991"));

         LatLng Punto30 = new LatLng(4.601227777777780, -74.073538888888900);
         map.addMarker(new MarkerOptions().position(Punto30).title("Asesinato de Jorge Eliecer Gaitan"));
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     * Habilita la capa Mi ubicación si se ha concedido el permiso de ubicación fine.
     */

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            // Falta el permiso para acceder a la ubicación.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            // Se ha concedido acceso a la ubicación a la aplicación.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            // Habilita la capa de mi ubicación si se ha concedido el permiso.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            // Mostrar el cuadro de diálogo de error de permiso que falta cuando se reanudan los fragmentos.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            // No se ha concedido permiso, muestra el diálogo de error.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     * Muestra un cuadro de diálogo con mensaje de error que explica que falta el permiso de ubicación.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}