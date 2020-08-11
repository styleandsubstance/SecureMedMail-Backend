/**
 * Created by Sachin Doshi on 7/23/2014.
 */


/*
  Command to Build Dojo:
  cd R:\SecureMedMail\public\javascripts
  dojo-release-1.9.3-src\util\buildscripts\build.bat --profile SecureMedMail.profile.js
*/

var profile = (function(){
    return {
        basePath: "./dojo-release-1.9.3-src",
        releaseDir: "../dojo-release-1.9.3",
        releaseName: "",
        action: "release",
        layerOptimize: "closure",
        optimize: "closure",
        cssOptimize: "comments",
        mini: true,
        stripConsole: "warn",
        selectorEngine: "lite",

        packages:[{
            name: "dojo",
            location: "dojo"
        },{
            name: "dijit",
            location: "dijit"
        },{
            name: "dojox",
            location: "dojox"
        }],

        layers: {
            "dojo/dojo": {
                include: [
                    "dojo/_base/declare",
                    "dijit/_WidgetBase",
                    "dijit/_TemplatedMixin",
                    "dijit/_WidgetsInTemplateMixin",
                    "dojox/grid/DataGrid",
                    "dojox/widget/Standby",
                    "dojo/store/Cache",
                    "dojo/store/JsonRest",
                    "dojo/store/Memory",
                    "dojo/store/Observable",
                    "dojo/data/ObjectStore",
                    "dojo/date/stamp",
                    "dojo/date/locale",
                    "dijit/Calendar",
                    "dijit/Dialog",
                    "dijit/TooltipDialog",
                    "dijit/form/Button",
                    "dijit/form/Checkbox",
                    "dijit/form/ComboButton",
                    "dijit/form/DateTextBox",
                    "dijit/form/Form",
                    "dijit/form/RadioButton",
                    "dijit/form/TextBox",
                    "dijit/form/ToggleButton",
                    "dijit/form/Select",
                    "dijit/layout/ContentPane",
                    "dijit/layout/BorderContainer",
                    "dijit/layout/LayoutContainer",
                    "dijit/layout/StackContainer",
                    "dijit/Tree",
                    "dijit/tree/TreeStoreModel",
                    "dijit/tree/ForestStoreModel",
                    "dijit/tree/ObjectStoreModel",
                    "dojo/on",
                    "dojo/_base/lang",
                    "dojo/_base/xhr",
                    "dojo/html",
                    "dojo/json",
                    "dojo/fx",
                    "dojo/fx/Toggler"
                ]
            }
        }
    };
})();