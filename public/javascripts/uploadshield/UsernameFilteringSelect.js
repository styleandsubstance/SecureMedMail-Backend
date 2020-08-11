/**
 * Created by sdoshi on 8/30/2014.
 */
define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin",
        "dijit/form/FilteringSelect", "dojo/on", "dojo/_base/lang", "dojo/_base/xhr", "dojo/store/Memory"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, FilteringSelect, on, lang, xhr, Memory){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

            /**
             * The username filtering select box
             */
            usernameFilteringSelectBox: null,

            get: function(value) {
              return this.usernameFilteringSelectBox.get(value);
            },

            setDisabled: function(isDisabled) {
                this.usernameFilteringSelectBox.set("disabled", isDisabled);
            },

            setupUsernames: function() {

                xhr.get({
                    // The URL to request
                    url: "/account/members/listAccountMembers",
                    handleAs: "json",
                    load: dojo.hitch(this, function (result) {
                        console.debug(result);


                        var data = [];

                        for ( var i = 0; i < result.children.length; i++)
                        {
                            this.usernameFilteringSelectBox.store.put({
                                name: result.children[i].username,
                                //value: result.children[i].username,
                                id: result.children[i].username
                            });

                        }
                    })
                });
            },

            isValid: function() {
                return this.usernameFilteringSelectBox.isValid();
            },

            postCreate: function() {
                this.setupUsernames();
            },

            templatePath: require.toUrl("uploadshield/templates/UsernameFilteringSelect.htm")
        });
    });