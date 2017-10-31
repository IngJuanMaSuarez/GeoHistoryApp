var kMarker_AnimationDuration_ChangeDrawable = 500;
var kMarker_AnimationDuration_Resize = 1000;

function Marker(poiData) {

    this.poiData = poiData;
    this.isSelected = false;

    /*
        With AR.PropertyAnimations you are able to animate almost any property of ARchitect objects.
        This sample will animate the opacity of both background drawables so that one will fade out
        while the other one fades in. The scaling is animated too. The marker size changes over time
         so the labels need to be animated too in order to keep them relative to the background drawable.
         AR.AnimationGroups are used to synchronize all animations in parallel or sequentially.

        Con AR.PropertyAnimations puede animar casi cualquier propiedad de los objetos de ARchitect.
        Este ejemplo animará la opacidad de ambos elementos de fondo, de modo que uno se desvanezca
        mientras el otro se desvanece. La escala también se anima. El tamaño del marcador cambia con el tiempo,
        por lo que las etiquetas también deben estar animadas para mantenerlas relativas al fondo.
        AR.AnimationGroups se utilizan para sincronizar todas las animaciones en paralelo o secuencialmente.
    */

    this.animationGroup_idle = null;
    this.animationGroup_selected = null;

    // create the AR.GeoLocation from the poi data
    // crea el AR.GeoLocation a partir de los datos poi
    var markerLocation = new AR.GeoLocation(poiData.latitude, poiData.longitude, poiData.altitude);

    // create an AR.ImageDrawable for the marker in idle state
    // crea un AR.ImageDrawable para el marcador en estado inactivo
    this.markerDrawable_idle = new AR.ImageDrawable(World.markerDrawable_idle, 1.5, {
        zOrder: 0,
        opacity: 1.0,
        /*
            To react on user interaction, an onClick property can be set for each AR.Drawable.
            The property is a function which will be called each time the user taps on the drawable.
            The function called on each tap is returned from the following helper function defined in marker.js.
            The function returns a function which checks the selected state with the help of the variable
            isSelected and executes the appropriate function. The clicked marker is passed as an argument.

            Para reaccionar en la interacción del usuario, se puede establecer una propiedad onClick para cada AR.Drawable.
            La propiedad es una función que se llamará cada vez que el usuario toque en el drawable.
            La función llamada en cada toque es devuelta desde la siguiente función auxiliar definida en marker.js.
            La función devuelve una función que comprueba el estado seleccionado con la ayuda de la variable
            isSelected y ejecuta la función apropiada. El marcador pulsado se pasa como un argumento.
        */
        onClick: Marker.prototype.getOnClickTrigger(this)
    });

    // create an AR.ImageDrawable for the marker in selected state
    // crea un AR.ImageDrawable para el marcador en el estado seleccionado
    this.markerDrawable_selected = new AR.ImageDrawable(World.markerDrawable_selected, 1.5, {
        zOrder: 0,
        opacity: 0.0,
        onClick: null
    });

    // create an AR.Label for the marker's title
    // crea una AR.Label para el título del marcador
    this.titleLabel = new AR.Label(poiData.title.trunc(10), 0.6, {
        zOrder: 1,
        translate: {
            y: 0.3
        },
        style: {
            textColor: '#FFFFFF',
            fontStyle: AR.CONST.FONT_STYLE.BOLD
        }
    });

    // create an AR.Label for the marker's description
    // crea una AR.Label para la descripción del marcador
    this.descriptionLabel = new AR.Label(poiData.description.trunc(15), 0.4, {
        zOrder: 1,
        translate: {
            y: -0.3
        },
        style: {
            textColor: '#FFFFFF'
        }
    });

    /*
        Create an AR.ImageDrawable using the AR.ImageResource for the direction indicator which was created in the World.
        Set options regarding the offset and anchor of the image so that it will be displayed correctly on the edge of the screen.

        Cree un AR.ImageDrawable utilizando el AR.ImageResource para el indicador de dirección que se creó en el mundo.
        Establezca las opciones con respecto al desplazamiento y anclaje de la imagen para que se muestre correctamente en el borde de la pantalla.
    */
    this.directionIndicatorDrawable = new AR.ImageDrawable(World.markerDrawable_directionIndicator, 0.1, {
        enabled: false,
        verticalAnchor: AR.CONST.VERTICAL_ANCHOR.TOP
    });

    /*
        Create the AR.GeoObject with the drawable objects and define the AR.ImageDrawable as an indicator target on the marker AR.GeoObject.
        The direction indicator is displayed automatically when necessary. AR.Drawable subclasses (e.g. AR.Circle) can be used as direction indicators.

        Cree el AR.GeoObject con los objetos dibujables y defina el AR.ImageDrawable como un indicador de destino en el marcador AR.GeoObject.
        El indicador de dirección se visualiza automáticamente cuando es necesario. Las subclases AR.Drawable (por ejemplo, AR.Circle) se pueden usar como indicadores de dirección.
    */
    this.markerObject = new AR.GeoObject(markerLocation, {
        drawables: {
            cam: [this.markerDrawable_idle, this.markerDrawable_selected, this.titleLabel, this.descriptionLabel],
            indicator: this.directionIndicatorDrawable
        }
    });

    return this;
}

Marker.prototype.getOnClickTrigger = function(marker) {

    /*
        The setSelected and setDeselected functions are prototype Marker functions. 
        Both functions perform the same steps but inverted.

        Las funciones setSelected y setDeselected son prototipos de funciones de Marcador.
        Ambas funciones realizan los mismos pasos pero están invertidas.
    */

    return function() {

        if (!Marker.prototype.isAnyAnimationRunning(marker)) {
            if (marker.isSelected) {

                Marker.prototype.setDeselected(marker);

            } else {
                Marker.prototype.setSelected(marker);
                try {
                    World.onMarkerSelected(marker);
                } catch (err) {
                    alert(err);
                }

            }
        } else {
            AR.logger.debug('a animation is already running');
        }


        return true;
    };
};

/*
    Property Animations allow constant changes to a numeric value/property of an object, dependent
    on start-value, end-value and the duration of the animation. Animations can be seen as functions
    defining the progress of the change on the value. The Animation can be parametrized via easing curves.

    Las Animaciones de propiedades permiten cambios constantes en un valor numérico / propiedad de un objeto,
    dependiendo del valor inicial, del valor final y de la duración de la animación. Las animaciones se pueden ver como funciones
    que definen el progreso del cambio en el valor. La animación se puede parametrizar a través de curvas de atenuación.
*/

Marker.prototype.setSelected = function(marker) {

    marker.isSelected = true;

    // New:
    // Nuevo:
    if (marker.animationGroup_selected === null) {

        // create AR.PropertyAnimation that animates the opacity to 0.0 in order to hide the idle-state-drawable
        // create A.Property Animation que anima la opacidad a 0.0 con el fin de ocultar la idle-state-drawable
        var hideIdleDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_idle, "opacity", null, 0.0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the opacity to 1.0 in order to show the selected-state-drawable
        // create A.Property Animation que anima la opacidad a 1.0 para mostrar el estado seleccionable-drawable
        var showSelectedDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_selected, "opacity", null, 1.0, kMarker_AnimationDuration_ChangeDrawable);

        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        // create AR.PropertyAnimation que anima el escalado del idle-state-drawable a 1.2
        var idleDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        // create AR.PropertyAnimation que anima el escalado del estado-drawable seleccionado a 1.2
        var selectedDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the title label to 1.2
        // create AR.PropertyAnimation que anima el escalado de la etiqueta de título a 1.2
        var titleLabelResizeAnimationX = new AR.PropertyAnimation(marker.titleLabel, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        // create AR.PropertyAnimation que anima la escala de la etiqueta de descripción a 1.2
        var descriptionLabelResizeAnimationX = new AR.PropertyAnimation(marker.descriptionLabel, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        // create AR.PropertyAnimation que anima el escalado del idle-state-drawable a 1.2
        var idleDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        // create AR.PropertyAnimation que anima el escalado del estado-drawable seleccionado a 1.2
        var selectedDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the title label to 1.2
        // create AR.PropertyAnimation que anima el escalado de la etiqueta de título a 1.2
        var titleLabelResizeAnimationY = new AR.PropertyAnimation(marker.titleLabel, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        // create AR.PropertyAnimation que anima la escala de la etiqueta de descripción a 1.2
        var descriptionLabelResizeAnimationY = new AR.PropertyAnimation(marker.descriptionLabel, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        /*
            There are two types of AR.AnimationGroups. Parallel animations are running at the same time,
            sequentials are played one after another. This example uses a parallel AR.AnimationGroup.

            Hay dos tipos de AR.AnimationGroups. Las animaciones paralelas se ejecutan al mismo tiempo,
            los secuenciales se reproducen uno tras otro. Este ejemplo utiliza un AR.AnimationGroup paralelo.
        */
        marker.animationGroup_selected = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, [hideIdleDrawableAnimation, showSelectedDrawableAnimation, idleDrawableResizeAnimationX, selectedDrawableResizeAnimationX, titleLabelResizeAnimationX, descriptionLabelResizeAnimationX, idleDrawableResizeAnimationY, selectedDrawableResizeAnimationY, titleLabelResizeAnimationY, descriptionLabelResizeAnimationY]);
    }

    // removes function that is set on the onClick trigger of the idle-state marker
    // elimina la función que se establece en el disparador onClick del marcador de estado inactivo
    marker.markerDrawable_idle.onClick = null;
    // sets the click trigger function for the selected state marker
    // establece la función de disparador de clic para el marcador de estado seleccionado
    marker.markerDrawable_selected.onClick = Marker.prototype.getOnClickTrigger(marker);

    // enables the direction indicator drawable for the current marker
    // permite que el indicador de dirección dibujable para el marcador actual
    marker.directionIndicatorDrawable.enabled = true;
    // starts the selected-state animation
    // inicia la animación de estado seleccionado
    marker.animationGroup_selected.start();
};

Marker.prototype.setDeselected = function(marker) {

    marker.isSelected = false;

    if (marker.animationGroup_idle === null) {

        // create AR.PropertyAnimation that animates the opacity to 1.0 in order to show the idle-state-drawable
        // create A.Property Animation que anima la opacidad a 1.0 para mostrar el estado de reposo-drawable
        var showIdleDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_idle, "opacity", null, 1.0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the opacity to 0.0 in order to hide the selected-state-drawable
        // create A.Property Animation que anima la opacidad a 0.0 para ocultar el estado seleccionable-drawable
        var hideSelectedDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_selected, "opacity", null, 0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.0
        // create AR.PropertyAnimation que anima el escalado del estado inactivo-drawable a 1.0
        var idleDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.0
        // create AR.PropertyAnimation que anima el escalado del estado-drawable seleccionado a 1.0
        var selectedDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the title label to 1.0
        // create AR.PropertyAnimation que anima el escalado de la etiqueta de título a 1.0
        var titleLabelResizeAnimationX = new AR.PropertyAnimation(marker.titleLabel, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.0
        // create AR.PropertyAnimation que anima el escalado de la etiqueta de descripción a 1.0
        var descriptionLabelResizeAnimationX = new AR.PropertyAnimation(marker.descriptionLabel, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.0
        // create AR.PropertyAnimation que anima el escalado del estado inactivo-drawable a 1.0
        var idleDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.0
        // create AR.PropertyAnimation que anima el escalado del estado-drawable seleccionado a 1.0
        var selectedDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the title label to 1.0
        // create AR.PropertyAnimation que anima el escalado de la etiqueta de título a 1.0
        var titleLabelResizeAnimationY = new AR.PropertyAnimation(marker.titleLabel, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.0
        // create AR.PropertyAnimation que anima el escalado de la etiqueta de descripción a 1.0
        var descriptionLabelResizeAnimationY = new AR.PropertyAnimation(marker.descriptionLabel, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        /*
            There are two types of AR.AnimationGroups. Parallel animations are running at the same time,
            sequentials are played one after another. This example uses a parallel AR.AnimationGroup.

            Hay dos tipos de AR.AnimationGroups. Las animaciones paralelas se ejecutan al mismo tiempo,
            los secuenciales se reproducen uno tras otro. Este ejemplo utiliza un AR.AnimationGroup paralelo.
        */
        marker.animationGroup_idle = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, [showIdleDrawableAnimation, hideSelectedDrawableAnimation, idleDrawableResizeAnimationX, selectedDrawableResizeAnimationX, titleLabelResizeAnimationX, descriptionLabelResizeAnimationX, idleDrawableResizeAnimationY, selectedDrawableResizeAnimationY, titleLabelResizeAnimationY, descriptionLabelResizeAnimationY]);
    }

    // sets the click trigger function for the idle state marker
    // establece la función de disparador de clic para el marcador de estado inactivo
    marker.markerDrawable_idle.onClick = Marker.prototype.getOnClickTrigger(marker);
    // removes function that is set on the onClick trigger of the selected-state marker
    // elimina la función que se establece en el disparador onClick del marcador de estado seleccionado
    marker.markerDrawable_selected.onClick = null;

    // disables the direction indicator drawable for the current marker
    // deshabilita el indicador de dirección drawable para el marcador actual
    marker.directionIndicatorDrawable.enabled = false;
    // starts the idle-state animation
    // inicia la animación de estado inactivo
    marker.animationGroup_idle.start();
};

Marker.prototype.isAnyAnimationRunning = function(marker) {

    if (marker.animationGroup_idle === null || marker.animationGroup_selected === null) {
        return false;
    } else {
        if ((marker.animationGroup_idle.isRunning() === true) || (marker.animationGroup_selected.isRunning() === true)) {
            return true;
        } else {
            return false;
        }
    }
};

// will truncate all strings longer than given max-length "n". e.g. "foobar".trunc(3) -> "foo..."
// truncará todas las cadenas más largas que la longitud máxima "n". p.ej. "foobar" .trunc (3) -> "foo ..."
String.prototype.trunc = function(n) {
    return this.substr(0, n - 1) + (this.length > n ? '...' : '');
};