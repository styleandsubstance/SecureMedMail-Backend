/**
 * Created by sdoshi on 9/25/2014.
 */
define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin",
        "dijit/form/FilteringSelect", "dojo/on", "dojo/_base/lang", "dojo/_base/xhr", "dojo/store/Memory",
        "uploadshield/util/ajax", "dojo/dom-class"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, FilteringSelect,
             on, lang, xhr, Memory, uploadshield, domClass){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

            plans: [],

            loadPlans: function() {

                uploadshield.get(
                    "/account/plans/listAvailablePlans",
                    dojo.hitch(this, function(response) {

                        if ( response.success == false ) {
                            console.debug(response.error);
                            alert("There has been an error");
                        }

                        //Issue with Dojo where addOption will not set the
                        //selected option correctly unless all options
                        //are added at once.  So we create an array with
                        //the AJAX response and add all at once
                        var plansForSelect = [];
                        for ( var i = 0; i < response.data.length; i++) {
                            var item = response.data[i];
                            this.plans[item.id] = item;
                            console.debug(item);

                            plansForSelect.push({
                                label: item.name,
                                value: item.id,
                                selected: item.default
                            });
                        }

                        this.planSelectBox.addOption(plansForSelect);

                        var selectedPlan = this.plans[this.planSelectBox.get("value")];
                        this.planDescriptionDiv.innerHTML = selectedPlan.description;

                    })
                )
            },

            postCreate: function() {
                this.loadPlans();

                on(this.planSelectBox, "change", dojo.hitch(this, function() {
                    console.debug("Plan changed");
                    console.debug(this.planSelectBox.get("value"));
                    var selectedPlan = this.plans[this.planSelectBox.get("value")];
                    this.planDescriptionDiv.innerHTML = selectedPlan.description;
                }));
            },

            templatePath: require.toUrl("uploadshield/templates/PlanSelector.htm")
        });
    });